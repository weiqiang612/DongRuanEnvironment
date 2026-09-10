package com.dongruan.environment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

public record AqiFeedbackRequest(
        Integer afId,
        @NotNull(message = "省份不能为空") Integer provinceId,
        @NotNull(message = "城市不能为空") Integer cityId,
        @NotBlank(message = "详细地址不能为空") @Size(max = 255, message = "详细地址长度不能超过255") String address,
        @NotBlank(message = "反馈内容不能为空") @Size(max = 1000, message = "反馈内容长度不能超过1000") String information,
        @NotNull(message = "预估等级不能为空") @Min(value = 0, message = "预估等级必须在0至6之间")
        @Max(value = 6, message = "预估等级必须在0至6之间") Integer estimatedGrade) {

    public Map<String, String> validateFields() {
        final Map<String, String> errors = new LinkedHashMap<>();
        if (provinceId == null) {
            errors.put("provinceId", "省份不能为空");
        }
        if (cityId == null) {
            errors.put("cityId", "城市不能为空");
        }
        validateText(errors, "address", address, 255, "详细地址不能为空", "详细地址长度不能超过255");
        validateText(errors, "information", information, 1000, "反馈内容不能为空", "反馈内容长度不能超过1000");
        if (estimatedGrade == null || estimatedGrade < 0 || estimatedGrade > 6) {
            errors.put("estimatedGrade", "预估等级必须在0至6之间");
        }
        return errors;
    }

    private static void validateText(final Map<String, String> errors, final String field, final String value,
            final int maximumLength, final String emptyMessage, final String lengthMessage) {
        if (value == null || value.isBlank()) {
            errors.put(field, emptyMessage);
        } else if (value.length() > maximumLength) {
            errors.put(field, lengthMessage);
        }
    }
}
