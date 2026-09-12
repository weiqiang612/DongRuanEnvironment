package com.dongruan.environment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MeasurementRequest(
        @NotNull(message = "SO2浓度不能为空") @DecimalMin(value = "0.0", message = "SO2浓度不能小于0")
        @Digits(integer = 8, fraction = 2, message = "SO2浓度最多保留2位小数") BigDecimal so2Value,
        @NotNull(message = "CO浓度不能为空") @DecimalMin(value = "0.0", message = "CO浓度不能小于0")
        @Digits(integer = 8, fraction = 2, message = "CO浓度最多保留2位小数") BigDecimal coValue,
        @NotNull(message = "PM2.5浓度不能为空") @DecimalMin(value = "0.0", message = "PM2.5浓度不能小于0")
        @Digits(integer = 8, fraction = 2, message = "PM2.5浓度最多保留2位小数") BigDecimal spmValue) {
}
