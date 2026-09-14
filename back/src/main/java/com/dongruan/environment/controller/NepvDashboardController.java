package com.dongruan.environment.controller;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.service.INepvDashboardService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/nepv")
public class NepvDashboardController {

    private final INepvDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ResultVO<?>> dashboard(
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedTo,
            final HttpSession session) {
        if (!hasDecisionRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功",
                    dashboardService.getDashboard(provinceId, cityId, submittedFrom, submittedTo)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    private boolean hasDecisionRole(final HttpSession session) {
        return EmployeeRole.NEPV_DECISION_MAKER.name().equals(session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE))
                && session.getAttribute(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE) instanceof String accountCode
                && !accountCode.isBlank();
    }

    private ResponseEntity<ResultVO<?>> forbidden(final HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "无决策端权限", null));
    }
}
