package com.dongruan.environment.controller;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.MeasurementRequest;
import com.dongruan.environment.service.INepgTaskService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/nepg/tasks")
public class NepgTaskController {
    private final INepgTaskService taskService;

    @GetMapping
    public ResponseEntity<ResultVO<?>> tasks(final HttpSession session) {
        final String accountCode = accountCode(session);
        return accountCode == null ? forbidden(session)
                : ResponseEntity.ok(new ResultVO<>(200, "查询成功", taskService.findMyTasks(accountCode)));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<ResultVO<?>> task(@PathVariable @NotNull final Integer feedbackId, final HttpSession session) {
        final String accountCode = accountCode(session);
        if (accountCode == null) return forbidden(session);
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "查询成功", taskService.findMyTask(feedbackId, accountCode)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "任务不存在", null));
        } catch (SecurityException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "无权访问该任务", null));
        }
    }

    @PostMapping("/{feedbackId}/measurements")
    public ResponseEntity<ResultVO<?>> submit(@PathVariable @NotNull final Integer feedbackId,
                                               @Valid @RequestBody final MeasurementRequest request,
                                               final HttpSession session) {
        final String accountCode = accountCode(session);
        if (accountCode == null) return forbidden(session);
        try {
            return ResponseEntity.ok(new ResultVO<>(200, "实测提交成功", taskService.submitMeasurement(feedbackId, accountCode, request)));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new ResultVO<>(400, exception.getMessage(), null));
        } catch (SecurityException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "无权提交该任务", null));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResultVO<>(409, exception.getMessage(), null));
        }
    }

    private String accountCode(final HttpSession session) {
        if (!EmployeeRole.NEPG_GRID_MEMBER.name().equals(session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE))) {
            return null;
        }
        final Object account = session.getAttribute(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE);
        return account instanceof String value && !value.isBlank() ? value : null;
    }

    private ResponseEntity<ResultVO<?>> forbidden(final HttpSession session) {
        if (session.getAttribute(AuthController.SESSION_EMPLOYEE_ROLE) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "无网格员权限", null));
    }
}
