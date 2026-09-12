package com.dongruan.environment.dto;

import java.time.LocalDateTime;

public record NepgTaskResponse(
        Integer afId, String provinceName, String cityName, String address, String information, Integer estimatedGrade,
        Integer state, Boolean timeoutFlag, LocalDateTime assignedAt, LocalDateTime completedAt) {
}
