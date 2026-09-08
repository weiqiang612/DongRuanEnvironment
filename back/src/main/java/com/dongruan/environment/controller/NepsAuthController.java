package com.dongruan.environment.controller;

import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.NepsLoginRequest;
import com.dongruan.environment.dto.NepsLoginResponse;
import com.dongruan.environment.service.INepsAuthService;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/neps")
public class NepsAuthController {

    public static final String SESSION_TEL_ID = "nepsSupervisorTelId";
    public static final String SESSION_REAL_NAME = "nepsSupervisorRealName";

    private final INepsAuthService nepsAuthService;

    @PostMapping("/login")
    public ResponseEntity<ResultVO<?>> login(
            @RequestBody final NepsLoginRequest request,
            final HttpSession session) {
        final Map<String, String> errors = validate(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        try {
            final NepsLoginResponse response = nepsAuthService.authenticate(request);
            session.setAttribute(SESSION_TEL_ID, response.telId());
            session.setAttribute(SESSION_REAL_NAME, response.realName());
            return ResponseEntity.ok(new ResultVO<>(200, "登录成功", response));
        } catch (InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResultVO<>(401, "手机号或密码错误", null));
        }
    }

    private Map<String, String> validate(final NepsLoginRequest request) {
        final Map<String, String> errors = new LinkedHashMap<>();
        if (request.telId() == null || request.telId().isBlank()) {
            errors.put("telId", "手机号不能为空");
        } else if (!request.telId().matches("^1\\d{10}$")) {
            errors.put("telId", "手机号格式不正确");
        }
        if (request.password() == null || request.password().isBlank()) {
            errors.put("password", "密码不能为空");
        }
        return errors;
    }
}
