package com.dongruan.environment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dongruan.environment.dto.MeasurementRequest;
import com.dongruan.environment.dto.NepgTaskDetailResponse;
import com.dongruan.environment.dto.NepgTaskResponse;
import com.dongruan.environment.entity.AlertRecord;
import com.dongruan.environment.entity.Aqi;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.entity.DetectionResult;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.mapper.AlertRecordMapper;
import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.mapper.AqiMapper;
import com.dongruan.environment.mapper.DetectionResultMapper;
import com.dongruan.environment.mapper.GridMemberMapper;
import com.dongruan.environment.service.INepgTaskService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NepgTaskServiceImpl implements INepgTaskService {
    private static final int ASSIGNED = 1;
    private static final int COMPLETED = 2;
    private static final int ALERT_THRESHOLD = 4;

    private final GridMemberMapper gridMemberMapper;
    private final AqiFeedbackMapper feedbackMapper;
    private final AqiMapper aqiMapper;
    private final DetectionResultMapper resultMapper;
    private final AlertRecordMapper alertRecordMapper;

    @Override
    public List<NepgTaskResponse> findMyTasks(final String accountCode) {
        final String gridMemberId = resolveGridMemberId(accountCode);
        return feedbackMapper.findForGridMember(gridMemberId).stream().map(this::toTask).toList();
    }

    @Override
    public NepgTaskDetailResponse findMyTask(final Integer feedbackId, final String accountCode) {
        final AqiFeedback feedback = requireOwnedTask(feedbackId, resolveGridMemberId(accountCode));
        return toDetail(feedback);
    }

    @Override
    @Transactional
    public NepgTaskDetailResponse submitMeasurement(
            final Integer feedbackId, final String accountCode, final MeasurementRequest request) {
        final String gridMemberId = resolveGridMemberId(accountCode);
        final AqiFeedback feedback = requireOwnedTask(feedbackId, gridMemberId);
        if (!Integer.valueOf(ASSIGNED).equals(feedback.getState())) {
            throw new IllegalStateException("该任务已完成或状态已变化，请刷新后重试");
        }
        if (resultMapper.selectCount(new QueryWrapper<DetectionResult>().eq("feedback_id", feedbackId)) > 0) {
            throw new IllegalStateException("该任务已提交检测结果，请勿重复提交");
        }

        final List<Aqi> dictionary = aqiMapper.selectList(null);
        final Aqi so2 = match(dictionary, request.so2Value(), Aqi::getSo2Min, Aqi::getSo2Max, "SO2");
        final Aqi co = match(dictionary, request.coValue(), Aqi::getCoMin, Aqi::getCoMax, "CO");
        final Aqi spm = match(dictionary, request.spmValue(), Aqi::getSpmMin, Aqi::getSpmMax, "PM2.5");
        final Aqi finalAqi = List.of(so2, co, spm).stream()
                .max(Comparator.comparing(Aqi::getAqiId))
                .orElseThrow();
        final LocalDateTime detectedAt = LocalDateTime.now();

        final int completed = feedbackMapper.update(null, new UpdateWrapper<AqiFeedback>()
                .eq("af_id", feedbackId).eq("gm_id", gridMemberId).eq("state", ASSIGNED)
                .set("state", COMPLETED).set("completed_at", detectedAt));
        if (completed != 1) {
            throw new IllegalStateException("该任务状态已变化，请刷新后重试");
        }

        final DetectionResult result = new DetectionResult();
        result.setFeedbackId(feedbackId);
        result.setGmId(gridMemberId);
        result.setSo2Value(request.so2Value());
        result.setSo2Level(so2.getAqiId());
        result.setCoValue(request.coValue());
        result.setCoLevel(co.getAqiId());
        result.setSpmValue(request.spmValue());
        result.setSpmLevel(spm.getAqiId());
        result.setAqiId(finalAqi.getAqiId());
        result.setDetectedAt(detectedAt);
        resultMapper.insert(result);
        if (finalAqi.getAqiId() >= ALERT_THRESHOLD) {
            final AlertRecord alert = new AlertRecord();
            alert.setFeedbackId(feedbackId);
            alert.setResultId(result.getId());
            alert.setAlertLevel(finalAqi.getAqiId());
            alert.setAlertStatus("PENDING");
            alertRecordMapper.insert(alert);
        }
        return toDetail(feedbackMapper.findDetailForNepm(feedbackId));
    }

    private String resolveGridMemberId(final String accountCode) {
        final GridMember member = gridMemberMapper.selectOne(new QueryWrapper<GridMember>().eq("gm_code", accountCode));
        if (member == null) {
            throw new SecurityException("无网格员权限");
        }
        return member.getGmId();
    }

    private AqiFeedback requireOwnedTask(final Integer feedbackId, final String gridMemberId) {
        final AqiFeedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!gridMemberId.equals(feedback.getGmId())) {
            throw new SecurityException("无权访问该任务");
        }
        return feedback;
    }

    private NepgTaskResponse toTask(final AqiFeedback feedback) {
        return new NepgTaskResponse(feedback.getAfId(), feedback.getProvinceName(), feedback.getCityName(),
                feedback.getAddress(), feedback.getInformation(), feedback.getEstimatedGrade(), feedback.getState(),
                feedback.getTimeoutFlag(), feedback.getAssignedAt(), feedback.getCompletedAt());
    }

    private NepgTaskDetailResponse toDetail(final AqiFeedback feedback) {
        final DetectionResult result = resultMapper.selectOne(new QueryWrapper<DetectionResult>()
                .eq("feedback_id", feedback.getAfId()));
        if (result == null) {
            return new NepgTaskDetailResponse(feedback.getAfId(), feedback.getProvinceName(), feedback.getCityName(),
                    feedback.getAddress(), feedback.getInformation(), feedback.getEstimatedGrade(), feedback.getState(),
                    feedback.getTimeoutFlag(), feedback.getAssignedAt(), feedback.getCompletedAt(), null, null,
                    null, null, null, null, null, null, null, false);
        }
        final Aqi finalAqi = aqiMapper.selectById(result.getAqiId());
        final boolean alertGenerated = alertRecordMapper.selectCount(new QueryWrapper<AlertRecord>()
                .eq("result_id", result.getId())) > 0;
        return new NepgTaskDetailResponse(feedback.getAfId(), feedback.getProvinceName(), feedback.getCityName(),
                feedback.getAddress(), feedback.getInformation(), feedback.getEstimatedGrade(), feedback.getState(),
                feedback.getTimeoutFlag(), feedback.getAssignedAt(), feedback.getCompletedAt(), result.getAqiId(),
                finalAqi == null ? null : finalAqi.getAqiExplain(), result.getSo2Value(), result.getSo2Level(),
                result.getCoValue(), result.getCoLevel(), result.getSpmValue(), result.getSpmLevel(),
                result.getDetectedAt(), alertGenerated);
    }

    private Aqi match(final List<Aqi> dictionary, final BigDecimal value,
                      final java.util.function.Function<Aqi, Integer> min,
                      final java.util.function.Function<Aqi, Integer> max, final String pollutant) {
        final List<Aqi> matches = dictionary.stream().filter(item -> min.apply(item) != null && max.apply(item) != null
                && value.compareTo(BigDecimal.valueOf(min.apply(item))) >= 0
                && value.compareTo(BigDecimal.valueOf(max.apply(item))) <= 0).toList();
        if (matches.size() != 1) {
            throw new IllegalArgumentException(pollutant + "浓度未匹配到唯一AQI等级");
        }
        return matches.get(0);
    }
}
