package com.dongruan.environment.controller;

import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.controller.AuthController;
import com.dongruan.environment.dto.AqiFeedbackRequest;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
@RequestMapping("/aqiFeedback")
public class AqiFeedbackController {

    private final IAqiFeedbackService aqiFeedbackService;

    @GetMapping("/list")
    public ResponseEntity<ResultVO<?>> getAqiFeedbackList(final HttpSession session) {
        final String telId = getSessionTelId(session);
        if (telId == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(new ResultVO<>(200, "查询成功", aqiFeedbackService.findBySupervisorTelId(telId)));
    }

    @GetMapping("/find/{afId}")
    public ResponseEntity<ResultVO<?>> findById(
            @PathVariable @NotNull final Integer afId,
            final HttpSession session) {
        final String telId = getSessionTelId(session);
        if (telId == null) {
            return unauthorized();
        }
        return aqiFeedbackService.findBySupervisorTelIdAndId(afId, telId)
                .map(feedback -> ResponseEntity.<ResultVO<?>>ok(new ResultVO<>(200, "查询成功", feedback)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResultVO<>(404, "反馈不存在", null)));
    }

    @GetMapping("/delete/{afId}")
    public ResponseEntity<ResultVO<?>> delete(
            @PathVariable @NotNull final Integer afId,
            final HttpSession session) {
        final String telId = getSessionTelId(session);
        if (telId == null) {
            return unauthorized();
        }
        if (!aqiFeedbackService.deleteOwnedPendingFeedback(afId, telId)) {
            return forbidden();
        }
        return ResponseEntity.ok(new ResultVO<>(200, "删除成功", true));
    }

    @PostMapping("/save")
    public ResponseEntity<ResultVO<?>> save(
            @Valid @RequestBody final AqiFeedbackRequest request,
            final HttpSession session) {
        final String telId = getSessionTelId(session);
        if (telId == null) {
            return unauthorized();
        }
        final Map<String, String> errors = request.validateFields();
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        return ResponseEntity.ok(new ResultVO<>(200, "保存成功", aqiFeedbackService.saveFeedback(toEntity(request, telId, true))));
    }

    @PostMapping("/update")
    public ResponseEntity<ResultVO<?>> update(
            @Valid @RequestBody final AqiFeedbackRequest request,
            final HttpSession session) {
        final String telId = getSessionTelId(session);
        if (telId == null) {
            return unauthorized();
        }
        final Map<String, String> errors = request.validateFields();
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        if (request.afId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResultVO<>(400, "请求参数不合法", Map.of("afId", "反馈编号不能为空")));
        }
        if (!aqiFeedbackService.updateOwnedPendingFeedback(toEntity(request, telId, false), telId)) {
            return forbidden();
        }
        return ResponseEntity.ok(new ResultVO<>(200, "修改成功", true));
    }

    private AqiFeedback toEntity(final AqiFeedbackRequest request, final String telId, final boolean isCreate) {
        final AqiFeedback feedback = new AqiFeedback();
        feedback.setAfId(request.afId());
        feedback.setTelId(telId);
        feedback.setProvinceId(request.provinceId());
        feedback.setCityId(request.cityId());
        feedback.setAddress(request.address());
        feedback.setInformation(request.information());
        feedback.setEstimatedGrade(request.estimatedGrade());
        if (isCreate) {
            feedback.setAfDate(LocalDate.now().toString());
            feedback.setAfTime(LocalTime.now().withNano(0).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }
        return feedback;
    }

    private String getSessionTelId(final HttpSession session) {
        final Object sessionTelId = session.getAttribute(AuthController.SESSION_TEL_ID);
        return sessionTelId instanceof String telId && !telId.isBlank() ? telId : null;
    }

    private ResponseEntity<ResultVO<?>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResultVO<>(401, "请先登录", null));
    }

    private ResponseEntity<ResultVO<?>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResultVO<>(403, "该反馈当前不可修改", null));
    }
}
