package com.dongruan.environment.controller;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.service.INepmDetectionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/nepm")
public class NepmDetectionController {

    private final INepmDetectionService detectionService;

    @GetMapping("/detection-results")
    public ResponseEntity<ResultVO<?>> detectionResults(
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false) final Integer aqiId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedTo,
            @RequestParam(required = false) final String keyword,
            @RequestParam(required = false, defaultValue = "1") @Min(1) final Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) final Integer pageSize,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功",
                    detectionService.findDetectionResults(provinceId, cityId, aqiId, submittedFrom, submittedTo, keyword, page, pageSize)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    @GetMapping("/detection-results/{id}")
    public ResponseEntity<ResultVO<?>> detectionResult(
            @PathVariable @NotNull final Integer id,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        return detectionService.findDetectionResultById(id)
                .map(item -> ResponseEntity.<ResultVO<?>>ok(new ResultVO<>(200, "查询成功", item)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "检测结果不存在", null)));
    }

    @GetMapping("/aqi-alerts")
    public ResponseEntity<ResultVO<?>> aqiAlerts(
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false, defaultValue = "1") @Min(1) final Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(100) final Integer pageSize,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功",
                    detectionService.findAqiAlerts(status, provinceId, cityId, page, pageSize)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    @PostMapping("/aqi-alerts/{alertId}/handle")
    public ResponseEntity<ResultVO<?>> handleAlert(
            @PathVariable @NotNull final Long alertId,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "处置成功", detectionService.handleAlert(alertId)));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, exception.getMessage(), null));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultVO<>(409, exception.getMessage(), null));
        }
    }

    @GetMapping("/analytics/stats")
    public ResponseEntity<ResultVO<?>> stats(
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate submittedTo,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功",
                    detectionService.getDetectionStats(provinceId, cityId, submittedFrom, submittedTo)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    private boolean hasAdminRole(final HttpSession session) {
        return EmployeeRole.NEPM_ADMIN.name().equals(session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE))
                && session.getAttribute(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE) instanceof String accountCode
                && !accountCode.isBlank();
    }

    private ResponseEntity<ResultVO<?>> forbidden(final HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "无管理端权限", null));
    }
}
