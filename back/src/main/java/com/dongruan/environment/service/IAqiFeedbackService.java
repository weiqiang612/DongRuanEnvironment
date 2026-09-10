package com.dongruan.environment.service;

import com.dongruan.environment.entity.AqiFeedback;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author weiqiang
 * @since 2026-09-03
 */
public interface IAqiFeedbackService extends IService<AqiFeedback> {
    List<AqiFeedback> findBySupervisorTelId(String telId);

    Optional<AqiFeedback> findBySupervisorTelIdAndId(Integer afId, String telId);

    boolean saveFeedback(AqiFeedback feedback);

    boolean updateOwnedPendingFeedback(AqiFeedback feedback, String telId);

    boolean deleteOwnedPendingFeedback(Integer afId, String telId);
}
