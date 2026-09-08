package com.dongruan.environment.common;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author weiqiang
 * @version 1.0
 * @Date 2026/9/3 15:18
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultVO<Map<String, String>>> handleValidationException(
            final MethodArgumentNotValidException exception) {
        final Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            final String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResultVO<>(400, "请求参数不合法", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultVO<Void>> handleException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResultVO<>(500, "操作失败", null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResultVO<Void>> handleIllegalArgumentException(final IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResultVO<>(400, "参数错误", null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResultVO<Void>> handleMissingParameter(
            final MissingServletRequestParameterException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResultVO<>(400, "请求参数不合法", null));
    }
}
