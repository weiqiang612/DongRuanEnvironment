package com.dongruan.environment.service;

import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.dto.NepmDetectionStatsVO;
import com.dongruan.environment.dto.NepmPageResponse;
import java.time.LocalDate;
import java.util.Optional;

public interface INepmDetectionService {

    NepmPageResponse<NepmDetectionResultVO> findDetectionResults(
            Integer provinceId,
            Integer cityId,
            Integer aqiId,
            LocalDate submittedFrom,
            LocalDate submittedTo,
            String keyword,
            Integer page,
            Integer pageSize);

    Optional<NepmDetectionResultVO> findDetectionResultById(Integer id);

    NepmPageResponse<NepmAqiAlertVO> findAqiAlerts(
            String status,
            Integer provinceId,
            Integer cityId,
            Integer page,
            Integer pageSize);

    NepmAqiAlertVO handleAlert(Long alertId);

    NepmDetectionStatsVO getDetectionStats(
            Integer provinceId,
            Integer cityId,
            LocalDate submittedFrom,
            LocalDate submittedTo);
}
