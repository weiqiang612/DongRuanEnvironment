package com.dongruan.environment.auth;

public class SupervisorAlreadyExistsException extends RuntimeException {
    public SupervisorAlreadyExistsException() {
        super("该手机号已被注册");
    }

    public SupervisorAlreadyExistsException(final String message) {
        super(message);
    }
}
