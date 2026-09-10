package com.dongruan.environment.controller;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.EmployeeLoginRequest;
import com.dongruan.environment.dto.EmployeeLoginResponse;
import com.dongruan.environment.dto.NepsLoginRequest;
import com.dongruan.environment.dto.NepsLoginResponse;
import com.dongruan.environment.service.IEmployeeAuthService;
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

/**
 * Provides the four portal login HTTP endpoints. Each endpoint retains its own
 * authentication service and credential contract.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    public static final String SESSION_TEL_ID = "nepsSupervisorTelId";
    public static final String SESSION_REAL_NAME = "nepsSupervisorRealName";
    public static final String SESSION_EMPLOYEE_ACCOUNT_CODE = "employeeAccountCode";
    public static final String SESSION_EMPLOYEE_DISPLAY_NAME = "employeeDisplayName";
    public static final String SESSION_EMPLOYEE_ROLE = "employeeRole";

    private final INepsAuthService nepsAuthService;
    private final IEmployeeAuthService employeeAuthService;

    @PostMapping("/neps/login")
    public ResponseEntity<ResultVO<?>> loginSupervisor(
            @RequestBody final NepsLoginRequest request,
            final HttpSession session) {
        final Map<String, String> errors = validateNeps(request);
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

    @PostMapping("/neps/logout")
    public ResponseEntity<ResultVO<Boolean>> logoutSupervisor(final HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ResultVO<>(200, "退出登录成功", true));
    }

    @PostMapping("/nepg/login")
    public ResponseEntity<ResultVO<?>> loginGridMember(
            @RequestBody final EmployeeLoginRequest request,
            final HttpSession session) {
        return authenticateEmployee(request, session, EmployeeRole.NEPG_GRID_MEMBER);
    }

    @PostMapping("/nepm/login")
    public ResponseEntity<ResultVO<?>> loginAdmin(
            @RequestBody final EmployeeLoginRequest request,
            final HttpSession session) {
        return authenticateEmployee(request, session, EmployeeRole.NEPM_ADMIN);
    }

    @PostMapping("/nepv/login")
    public ResponseEntity<ResultVO<?>> loginDecisionMaker(
            @RequestBody final EmployeeLoginRequest request,
            final HttpSession session) {
        return authenticateEmployee(request, session, EmployeeRole.NEPV_DECISION_MAKER);
    }

    private ResponseEntity<ResultVO<?>> authenticateEmployee(
            final EmployeeLoginRequest request,
            final HttpSession session,
            final EmployeeRole expectedRole) {
        final Map<String, String> errors = validateEmployee(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        try {
            final EmployeeLoginResponse response = expectedRole == EmployeeRole.NEPG_GRID_MEMBER
                    ? employeeAuthService.authenticateGridMember(request)
                    : employeeAuthService.authenticateAdmin(request, expectedRole);
            writeEmployeeSession(session, response);
            return ResponseEntity.ok(new ResultVO<>(200, "登录成功", response));
        } catch (InvalidCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResultVO<>(401, "账号或密码错误", null));
        }
    }

    private Map<String, String> validateNeps(final NepsLoginRequest request) {
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

    private Map<String, String> validateEmployee(final EmployeeLoginRequest request) {
        final Map<String, String> errors = new LinkedHashMap<>();
        if (request.accountCode() == null || request.accountCode().isBlank()) {
            errors.put("accountCode", "账号不能为空");
        } else if (request.accountCode().length() > 20) {
            errors.put("accountCode", "账号长度不能超过20位");
        }
        if (request.password() == null || request.password().isBlank()) {
            errors.put("password", "密码不能为空");
        }
        return errors;
    }

    private void writeEmployeeSession(final HttpSession session, final EmployeeLoginResponse response) {
        session.setAttribute(SESSION_EMPLOYEE_ACCOUNT_CODE, response.accountCode());
        session.setAttribute(SESSION_EMPLOYEE_DISPLAY_NAME, response.displayName());
        session.setAttribute(SESSION_EMPLOYEE_ROLE, response.role().name());
    }
}
