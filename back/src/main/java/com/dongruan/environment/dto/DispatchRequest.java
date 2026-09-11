package com.dongruan.environment.dto;

import jakarta.validation.constraints.NotBlank;

public record DispatchRequest(@NotBlank(message = "网格员不能为空") String gridMemberId) {
}
