package com.dongruan.environment.dto;

public record NepmAqiAlertQuery(
        String status,
        Integer provinceId,
        Integer cityId,
        Integer page,
        Integer pageSize
) {
}
