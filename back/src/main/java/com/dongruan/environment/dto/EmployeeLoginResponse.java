package com.dongruan.environment.dto;

import com.dongruan.environment.auth.EmployeeRole;

public record EmployeeLoginResponse(String accountCode, String displayName, EmployeeRole role) {
}
