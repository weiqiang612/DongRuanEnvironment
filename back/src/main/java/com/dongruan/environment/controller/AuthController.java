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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.dongruan.environment.auth.SupervisorAlreadyExistsException;
import com.dongruan.environment.dto.NepsRegisterRequest;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

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

    @PostMapping("/neps/register")
    public ResponseEntity<ResultVO<?>> registerSupervisor(
            @RequestBody final NepsRegisterRequest request) {
        final Map<String, String> errors = validateNepsRegister(request);
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        try {
            nepsAuthService.register(request);
            return ResponseEntity.ok(new ResultVO<>(200, "注册成功", true));
        } catch (SupervisorAlreadyExistsException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResultVO<>(409, exception.getMessage(), null));
        }
    }

    @PostMapping("/neps/logout")
    public ResponseEntity<ResultVO<Boolean>> logoutSupervisor(final HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ResultVO<>(200, "退出登录成功", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResultVO<Boolean>> logout(final HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ResultVO<>(200, "退出登录成功", true));
    }

    @GetMapping("/session")
    public ResponseEntity<ResultVO<?>> sessionStatus(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
        }
        final Object telId = session.getAttribute(SESSION_TEL_ID);
        final Object realName = session.getAttribute(SESSION_REAL_NAME);
        if (telId instanceof String value && !value.isBlank()) {
            final Map<String, Object> data = new LinkedHashMap<>();
            data.put("portal", "NEPS_SUPERVISOR");
            data.put("telId", value);
            data.put("displayName", realName instanceof String name ? name : "公众监督员");
            return ResponseEntity.ok(new ResultVO<>(200, "会话有效", data));
        }
        final Object role = session.getAttribute(SESSION_EMPLOYEE_ROLE);
        final Object account = session.getAttribute(SESSION_EMPLOYEE_ACCOUNT_CODE);
        final Object displayName = session.getAttribute(SESSION_EMPLOYEE_DISPLAY_NAME);
        if (role instanceof String roleValue && account instanceof String accountCode && !accountCode.isBlank()
                && isEmployeeRole(roleValue)) {
            final Map<String, Object> data = new LinkedHashMap<>();
            data.put("portal", roleValue);
            data.put("accountCode", accountCode);
            data.put("displayName", displayName instanceof String name ? name : "");
            return ResponseEntity.ok(new ResultVO<>(200, "会话有效", data));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
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
        if (request == null) {
            errors.put("request", "请求体不能为空");
            return errors;
        }
        if (request.telId() == null || request.telId().isBlank()) {
            errors.put("telId", "手机号不能为空");
        } else if (!PHONE_PATTERN.matcher(request.telId()).matches()) {
            errors.put("telId", "手机号格式不正确");
        }
        if (request.password() == null || request.password().isBlank()) {
            errors.put("password", "密码不能为空");
        }
        return errors;
    }

    private Map<String, String> validateNepsRegister(final NepsRegisterRequest request) {
        final Map<String, String> errors = new LinkedHashMap<>();
        if (request == null) {
            errors.put("request", "请求体不能为空");
            return errors;
        }
        if (request.telId() == null || request.telId().isBlank()) {
            errors.put("telId", "手机号不能为空");
        } else if (!PHONE_PATTERN.matcher(request.telId()).matches()) {
            errors.put("telId", "手机号格式不正确");
        }
        if (request.password() == null || request.password().isBlank()) {
            errors.put("password", "密码不能为空");
        } else if (request.password().length() < 6 || request.password().length() > 32) {
            errors.put("password", "密码长度必须在6-32位之间");
        }
        return errors;
    }

    private Map<String, String> validateEmployee(final EmployeeLoginRequest request) {
        final Map<String, String> errors = new LinkedHashMap<>();
        if (request == null) {
            errors.put("request", "请求体不能为空");
            return errors;
        }
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

    private boolean isEmployeeRole(final String role) {
        return EmployeeRole.NEPG_GRID_MEMBER.name().equals(role)
                || EmployeeRole.NEPM_ADMIN.name().equals(role)
                || EmployeeRole.NEPV_DECISION_MAKER.name().equals(role);
    }
}
