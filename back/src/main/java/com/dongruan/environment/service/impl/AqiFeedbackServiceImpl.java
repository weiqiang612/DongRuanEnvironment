package com.dongruan.environment.service.impl;

import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
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
    public List<AqiFeedback> findAll() {
        return baseMapper.findAll();
    }

    @Override
    public Optional<AqiFeedback> findById(final Integer afId) {
        return Optional.ofNullable(getById(afId));
    }

    @Override
    public boolean saveFeedback(final AqiFeedback feedback) {
        return save(feedback);
    }

    @Override
    public boolean updateFeedback(final AqiFeedback feedback) {
        return updateById(feedback);
    }

    @Override
    public boolean deleteFeedback(final Integer afId) {
        return removeById(afId);
    }
}
