package com.dongruan.environment.service.impl;

import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.service.IAqiFeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<AqiFeedback> findAll(){
        return baseMapper.findAll();
    }
}
