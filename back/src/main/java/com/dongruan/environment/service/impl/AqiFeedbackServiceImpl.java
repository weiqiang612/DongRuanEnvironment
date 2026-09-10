package com.dongruan.environment.service.impl;

import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author weiqiang
 * @since 2026-09-03
 */
@Service
public class AqiFeedbackServiceImpl extends ServiceImpl<AqiFeedbackMapper, AqiFeedback> implements IAqiFeedbackService {
    @Override
    public List<AqiFeedback> findBySupervisorTelId(final String telId) {
        return baseMapper.findByTelId(telId);
    }

    @Override
    public Optional<AqiFeedback> findBySupervisorTelIdAndId(final Integer afId, final String telId) {
        return Optional.ofNullable(baseMapper.findByIdAndTelId(afId, telId));
    }

    @Override
    public boolean saveFeedback(final AqiFeedback feedback) {
        return save(feedback);
    }

    @Override
    public boolean updateOwnedPendingFeedback(final AqiFeedback feedback, final String telId) {
        return update(feedback, new LambdaUpdateWrapper<AqiFeedback>()
                .eq(AqiFeedback::getAfId, feedback.getAfId())
                .eq(AqiFeedback::getTelId, telId)
                .eq(AqiFeedback::getState, 0)
                .eq(AqiFeedback::getTimeoutFlag, false));
    }

    @Override
    public boolean deleteOwnedPendingFeedback(final Integer afId, final String telId) {
        return remove(new LambdaUpdateWrapper<AqiFeedback>()
                .eq(AqiFeedback::getAfId, afId)
                .eq(AqiFeedback::getTelId, telId)
                .eq(AqiFeedback::getState, 0)
                .eq(AqiFeedback::getTimeoutFlag, false));
    }
}
