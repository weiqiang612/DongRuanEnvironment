package com.dongruan.environment.controller;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.DispatchRequest;
import com.dongruan.environment.service.INepmDispatchService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/nepm")
public class NepmDispatchController {

    private final INepmDispatchService dispatchService;

    @GetMapping("/dashboard")
    public ResponseEntity<ResultVO<?>> dashboard(final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.dashboard()));
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<ResultVO<?>> feedbacks(
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false) final List<Integer> states,
            @RequestParam(required = false) final Boolean timeoutOnly,
            @RequestParam(required = false) final Integer estimatedGrade,
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
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.findFeedbacks(provinceId, cityId,
                    states, timeoutOnly, estimatedGrade, submittedFrom, submittedTo, keyword, page, pageSize)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    @GetMapping("/timeout-alerts")
    public ResponseEntity<ResultVO<?>> timeoutAlerts(
            @RequestParam(required = false) final Integer provinceId,
            @RequestParam(required = false) final Integer cityId,
            @RequestParam(required = false) final List<Integer> states,
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
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.findFeedbacks(provinceId, cityId,
                    states, true, null, submittedFrom, submittedTo, keyword, page, pageSize)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    @GetMapping("/analytics/overview")
    public ResponseEntity<ResultVO<?>> overview(
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
                    dispatchService.overview(provinceId, cityId, submittedFrom, submittedTo)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        }
    }

    @GetMapping("/analytics/recent-trend")
    public ResponseEntity<ResultVO<?>> recentTrend(final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.recentTrend()));
    }

    @GetMapping("/feedbacks/{feedbackId}")
    public ResponseEntity<ResultVO<?>> feedback(@PathVariable @NotNull final Integer feedbackId, final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        return dispatchService.findFeedback(feedbackId)
                .map(item -> ResponseEntity.<ResultVO<?>>ok(new ResultVO<>(200, "查询成功", item)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "反馈不存在", null)));
    }

    @GetMapping("/feedbacks/{feedbackId}/candidates")
    public ResponseEntity<ResultVO<?>> candidates(@PathVariable @NotNull final Integer feedbackId, final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.findCandidates(feedbackId)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "反馈不存在", null));
        }
    }

    @GetMapping("/feedbacks/{feedbackId}/logs")
    public ResponseEntity<ResultVO<?>> logs(@PathVariable @NotNull final Integer feedbackId, final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        return ResponseEntity.ok(new ResultVO<>(200, "查询成功", dispatchService.findLogs(feedbackId)));
    }

    @PostMapping("/feedbacks/{feedbackId}/dispatch")
    public ResponseEntity<ResultVO<?>> dispatch(
            @PathVariable @NotNull final Integer feedbackId,
            @Valid @RequestBody final DispatchRequest request,
            final HttpSession session) {
        if (!hasAdminRole(session)) {
            return forbidden(session);
        }
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "调度成功", dispatchService.dispatch(
                    feedbackId, request.gridMemberId(), (String) session.getAttribute(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE))));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "反馈不存在", null));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultVO<>(409, exception.getMessage(), null));
        } catch (SecurityException exception) {
            return forbidden(session);
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
