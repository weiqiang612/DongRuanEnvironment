package com.dongruan.environment.service;

import com.dongruan.environment.entity.AqiFeedback;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author weiqiang
 * @since 2026-09-03
 */
public interface IAqiFeedbackService extends IService<AqiFeedback> {
    public List<AqiFeedback> findAll();
}
