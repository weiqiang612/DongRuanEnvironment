package com.dongruan.environment.dto;

import java.time.LocalDate;

public record NepmDetectionResultQuery(
        Integer provinceId,
        Integer cityId,
        Integer aqiId,
        LocalDate submittedFrom,
        LocalDate submittedTo,
        String keyword,
        Integer page,
        Integer pageSize
) {
}
