package com.dongruan.environment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "公众监督员极简注册请求体")
public record NepsRegisterRequest(
        @Schema(description = "手机号", example = "13800000000")
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String telId,

        @Schema(description = "登录密码", example = "123456")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度必须在6-32位之间")
        String password
) {
}
