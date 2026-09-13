package com.dongruan.environment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record NepmAqiAlertVO(
        Long id,
        Integer feedbackId,
        Integer resultId,
        Integer alertLevel,
        String alertStatus,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime handledAt,
        Integer provinceId,
        String provinceName,
        Integer cityId,
        String cityName,
        String address,
        Integer aqiId,
        String gmName
) {
}
