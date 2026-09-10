package com.dongruan.environment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.auth.Pbkdf2PasswordHasher;
import com.dongruan.environment.dto.EmployeeLoginRequest;
import com.dongruan.environment.dto.EmployeeLoginResponse;
import com.dongruan.environment.entity.Admin;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.mapper.AdminMapper;
import com.dongruan.environment.mapper.GridMemberMapper;
import com.dongruan.environment.service.IEmployeeAuthService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeAuthServiceImpl implements IEmployeeAuthService {

    private final GridMemberMapper gridMemberMapper;
    private final AdminMapper adminMapper;
    private final Pbkdf2PasswordHasher passwordHasher;

    @Override
    public EmployeeLoginResponse authenticateGridMember(final EmployeeLoginRequest request) {
        final GridMember gridMember = gridMemberMapper.selectOne(
                new LambdaQueryWrapper<GridMember>().eq(GridMember::getGmCode, request.accountCode()));
        if (gridMember == null || !passwordHasher.matches(request.password(), gridMember.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!passwordHasher.isHash(gridMember.getPassword())) {
            gridMember.setPassword(passwordHasher.hash(request.password()));
            gridMemberMapper.updateById(gridMember);
        }
        return new EmployeeLoginResponse(gridMember.getGmCode(), gridMember.getGmName(),
                EmployeeRole.NEPG_GRID_MEMBER);
    }

    @Override
    public EmployeeLoginResponse authenticateAdmin(
            final EmployeeLoginRequest request,
            final EmployeeRole expectedRole) {
        final Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getAdminCode, request.accountCode()));
        if (admin == null
                || !passwordHasher.matches(request.password(), admin.getPassword())
                || !Objects.equals(expectedRole.name(), admin.getRoleCode())) {
            throw new InvalidCredentialsException();
        }
        if (!passwordHasher.isHash(admin.getPassword())) {
            admin.setPassword(passwordHasher.hash(request.password()));
            adminMapper.updateById(admin);
        }
        return new EmployeeLoginResponse(admin.getAdminCode(), admin.getAdminCode(), expectedRole);
    }
}
