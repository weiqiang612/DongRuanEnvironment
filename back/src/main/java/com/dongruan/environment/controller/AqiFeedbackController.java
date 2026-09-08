package com.dongruan.environment.controller;

import com.dongruan.environment.common.ResultVO;
import com.dongruan.environment.dto.AqiFeedbackRequest;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
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
    public ResultVO<?> getAqiFeedbackList() {
        return new ResultVO<>(200, "查询成功", aqiFeedbackService.findAll());
    }

    @GetMapping("/find/{afId}")
    public ResponseEntity<ResultVO<AqiFeedback>> findById(@PathVariable @NotNull final Integer afId) {
        return aqiFeedbackService.findById(afId)
                .map(feedback -> ResponseEntity.ok(new ResultVO<>(200, "查询成功", feedback)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResultVO<>(404, "反馈不存在", null)));
    }

    @GetMapping("/delete/{afId}")
    public ResponseEntity<ResultVO<Boolean>> delete(@PathVariable @NotNull final Integer afId) {
        if (aqiFeedbackService.findById(afId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "反馈不存在", false));
        }
        return ResponseEntity.ok(new ResultVO<>(200, "删除成功", aqiFeedbackService.deleteFeedback(afId)));
    }

    @PostMapping("/save")
    public ResponseEntity<ResultVO<?>> save(@Valid @RequestBody final AqiFeedbackRequest request) {
        final Map<String, String> errors = request.validateFields();
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        return ResponseEntity.ok(new ResultVO<>(200, "保存成功", aqiFeedbackService.saveFeedback(toEntity(request))));
    }

    @PostMapping("/update")
    public ResponseEntity<ResultVO<?>> update(@Valid @RequestBody final AqiFeedbackRequest request) {
        final Map<String, String> errors = request.validateFields();
        if (!errors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResultVO<>(400, "请求参数不合法", errors));
        }
        if (request.afId() == null || aqiFeedbackService.findById(request.afId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResultVO<>(404, "反馈不存在", false));
        }
        return ResponseEntity.ok(new ResultVO<>(200, "修改成功", aqiFeedbackService.updateFeedback(toEntity(request))));
    }

    private AqiFeedback toEntity(final AqiFeedbackRequest request) {
        final AqiFeedback feedback = new AqiFeedback();
        feedback.setAfId(request.afId());
        feedback.setTelId(request.telId());
        feedback.setProvinceId(request.provinceId());
        feedback.setCityId(request.cityId());
        feedback.setAddress(request.address());
        feedback.setInformation(request.information());
        feedback.setEstimatedGrade(request.estimatedGrade());
        feedback.setAfDate(request.afDate());
        feedback.setAfTime(request.afTime());
        return feedback;
    }
}
