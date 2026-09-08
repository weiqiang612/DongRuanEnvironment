package com.dongruan.environment.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("手机号或密码错误");
    }
}
