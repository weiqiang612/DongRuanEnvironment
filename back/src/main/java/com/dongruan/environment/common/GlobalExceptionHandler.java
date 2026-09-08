package com.dongruan.environment.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author weiqiang
 * @version 1.0
 * @Date 2026/9/3 15:18
 */

@RestControllerAdvice
public class GlobalExceptionHandler {
    //处理所有的异常
    @ExceptionHandler(Exception.class)
    public ResultVO handleException(Exception e){
        return new ResultVO(500,"操作失败",e.getMessage());
    }
    //处理参数错误的异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResultVO handleIllegalArgumentException(IllegalArgumentException e){
        return new ResultVO(400,"参数错误",e.getMessage());
    }
}
