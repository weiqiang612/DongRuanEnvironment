package com.dongruan.environment.service;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.dto.EmployeeLoginRequest;
import com.dongruan.environment.dto.EmployeeLoginResponse;

public interface IEmployeeAuthService {

    EmployeeLoginResponse authenticateGridMember(EmployeeLoginRequest request);

    EmployeeLoginResponse authenticateAdmin(EmployeeLoginRequest request, EmployeeRole expectedRole);
}
