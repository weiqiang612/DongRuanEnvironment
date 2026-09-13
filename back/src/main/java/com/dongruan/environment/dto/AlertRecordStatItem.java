package com.dongruan.environment.dto;

import java.time.LocalDateTime;

public record AlertRecordStatItem(
        Long id,
        String alertStatus,
        Integer alertLevel,
        LocalDateTime submittedAt
) {
}
