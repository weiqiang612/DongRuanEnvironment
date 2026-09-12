package com.dongruan.environment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NepgTaskDetailResponse(
        Integer afId, String provinceName, String cityName, String address, String information, Integer estimatedGrade,
        Integer state, Boolean timeoutFlag, LocalDateTime assignedAt, LocalDateTime completedAt,
        Integer finalGrade, String finalGradeName, BigDecimal so2Value, Integer so2Level,
        BigDecimal coValue, Integer coLevel, BigDecimal spmValue, Integer spmLevel,
        LocalDateTime detectedAt, Boolean alertGenerated) {
}
