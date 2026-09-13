package com.dongruan.environment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NepmDetectionResultVO(
        Integer id,
        Integer feedbackId,
        Integer provinceId,
        String provinceName,
        Integer cityId,
        String cityName,
        String address,
        String information,
        Integer estimatedGrade,
        String gmId,
        String gmName,
        BigDecimal so2Value,
        Integer so2Level,
        BigDecimal coValue,
        Integer coLevel,
        BigDecimal spmValue,
        Integer spmLevel,
        Integer aqiId,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime detectedAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime submittedAt
) {
}
