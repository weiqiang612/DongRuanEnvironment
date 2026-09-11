package com.dongruan.environment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dongruan.environment.entity.Admin;
import com.dongruan.environment.dto.NepmPageResponse;
import com.dongruan.environment.dto.NepmCandidateResponse;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.entity.TaskAssignLog;
import com.dongruan.environment.mapper.AdminMapper;
import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.mapper.GridMemberMapper;
import com.dongruan.environment.mapper.TaskAssignLogMapper;
import com.dongruan.environment.service.INepmDispatchService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NepmDispatchServiceImpl implements INepmDispatchService {

    private static final int PENDING = 0;
    private static final int ASSIGNED = 1;
    private static final int COMPLETED = 2;
    private final AqiFeedbackMapper feedbackMapper;
    private final GridMemberMapper gridMemberMapper;
    private final AdminMapper adminMapper;
    private final TaskAssignLogMapper taskAssignLogMapper;

    @Override
    public NepmPageResponse<AqiFeedback> findFeedbacks(final Integer provinceId, final Integer cityId,
                                                        final List<Integer> states, final Boolean timeoutOnly,
                                                        final Integer estimatedGrade, final LocalDate submittedFrom,
                                                        final LocalDate submittedTo, final String keyword,
                                                        final Integer page, final Integer pageSize) {
        if (submittedFrom != null && submittedTo != null && submittedFrom.isAfter(submittedTo)) {
            throw new IllegalArgumentException("提交开始日期不能晚于结束日期");
        }
        final int normalizedPage = page == null ? 1 : page;
        final int normalizedPageSize = pageSize == null ? 10 : pageSize;
        final List<AqiFeedback> filtered = feedbackMapper.findForNepm(provinceId, cityId, states, timeoutOnly,
                estimatedGrade, submittedFrom == null ? null : submittedFrom.atStartOfDay(),
                submittedTo == null ? null : submittedTo.plusDays(1).atStartOfDay(),
                keyword == null ? null : keyword.trim());
        final int fromIndex = Math.min((normalizedPage - 1) * normalizedPageSize, filtered.size());
        final int toIndex = Math.min(fromIndex + normalizedPageSize, filtered.size());
        return new NepmPageResponse<>(filtered.subList(fromIndex, toIndex), filtered.size(), normalizedPage,
                normalizedPageSize);
    }

    @Override
    public Optional<AqiFeedback> findFeedback(final Integer feedbackId) {
        return Optional.ofNullable(feedbackMapper.findDetailForNepm(feedbackId));
    }

    @Override
    public List<NepmCandidateResponse> findCandidates(final Integer feedbackId) {
        final AqiFeedback feedback = requireFeedback(feedbackId);
        return gridMemberMapper.findAvailableForNepm().stream()
                .filter(member -> feedback.getProvinceId().equals(member.getProvinceId()))
                .sorted(Comparator.comparing((GridMember member) -> !feedback.getCityId().equals(member.getCityId())))
                .map(member -> new NepmCandidateResponse(member.getGmId(), member.getGmName(), member.getProvinceId(),
                        member.getCityId(), member.getCityName(), feedback.getCityId().equals(member.getCityId())
                        ? "SAME_CITY" : "SAME_PROVINCE"))
                .toList();
    }

    @Override
    public List<TaskAssignLog> findLogs(final Integer feedbackId) {
        return taskAssignLogMapper.selectList(new LambdaQueryWrapper<TaskAssignLog>()
                .eq(TaskAssignLog::getFeedbackId, feedbackId)
                .orderByDesc(TaskAssignLog::getCreatedAt));
    }

    @Override
    @Transactional
    public AqiFeedback dispatch(final Integer feedbackId, final String gridMemberId, final String accountCode) {
        final AqiFeedback feedback = requireFeedback(feedbackId);
        if (COMPLETED == feedback.getState()) {
            throw new IllegalStateException("已完成反馈不可调度");
        }
        final Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getAdminCode, accountCode));
        if (admin == null) {
            throw new SecurityException("无管理端权限");
        }
        final GridMember member = gridMemberMapper.selectById(gridMemberId);
        if (member == null || member.getState() == null || member.getState() != 0
                || !feedback.getProvinceId().equals(member.getProvinceId())) {
            throw new IllegalStateException("网格员当前不可指派");
        }
        final boolean sameMember = gridMemberId.equals(feedback.getGmId());
        final String action = sameMember ? "CONTINUE" : feedback.getState() == PENDING ? "ASSIGN" : "REASSIGN";
        if (!sameMember) {
            final int updated = feedbackMapper.update(null, new LambdaUpdateWrapper<AqiFeedback>()
                    .eq(AqiFeedback::getAfId, feedbackId)
                    .eq(AqiFeedback::getState, feedback.getState())
                    .set(AqiFeedback::getGmId, gridMemberId)
                    .set(AqiFeedback::getState, ASSIGNED)
                    .set(AqiFeedback::getAssignedAt, LocalDateTime.now()));
            if (updated != 1) {
                throw new IllegalStateException("反馈状态已变化，请刷新后重试");
            }
        }
        final TaskAssignLog log = new TaskAssignLog();
        log.setFeedbackId(feedbackId);
        log.setOperatorId(admin.getAdminId());
        log.setFromGmId(sameMember ? gridMemberId : feedback.getGmId());
        log.setToGmId(gridMemberId);
        log.setActionType(action);
        taskAssignLogMapper.insert(log);
        return feedbackMapper.selectById(feedbackId);
    }

    @Override
    public Map<String, Long> dashboard() {
        final List<AqiFeedback> items = feedbackMapper.findAll();
        final Map<String, Long> result = new LinkedHashMap<>();
        result.put("pending", count(items, PENDING));
        result.put("assigned", count(items, ASSIGNED));
        result.put("completed", count(items, COMPLETED));
        result.put("timeout", items.stream().filter(item -> Boolean.TRUE.equals(item.getTimeoutFlag())).count());
        return result;
    }

    @Override
    public Map<String, Long> overview(final Integer provinceId, final Integer cityId, final LocalDate submittedFrom,
                                      final LocalDate submittedTo) {
        if (submittedFrom != null && submittedTo != null && submittedFrom.isAfter(submittedTo)) {
            throw new IllegalArgumentException("提交开始日期不能晚于结束日期");
        }
        final List<AqiFeedback> items = feedbackMapper.findForNepm(provinceId, cityId, null, null, null,
                submittedFrom == null ? null : submittedFrom.atStartOfDay(),
                submittedTo == null ? null : submittedTo.plusDays(1).atStartOfDay(), null);
        final Map<String, Long> result = new LinkedHashMap<>();
        result.put("total", (long) items.size());
        result.put("pending", count(items, PENDING));
        result.put("assigned", count(items, ASSIGNED));
        result.put("completed", count(items, COMPLETED));
        result.put("timeout", items.stream().filter(item -> Boolean.TRUE.equals(item.getTimeoutFlag())).count());
        return result;
    }

    private AqiFeedback requireFeedback(final Integer feedbackId) {
        return findFeedback(feedbackId).orElseThrow(() -> new IllegalArgumentException("反馈不存在"));
    }

    private long count(final List<AqiFeedback> items, final int state) {
        return items.stream().filter(item -> Integer.valueOf(state).equals(item.getState())).count();
    }
}
