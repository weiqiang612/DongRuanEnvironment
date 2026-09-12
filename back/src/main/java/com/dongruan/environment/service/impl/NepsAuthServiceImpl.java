package com.dongruan.environment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.auth.Pbkdf2PasswordHasher;
import com.dongruan.environment.dto.NepsLoginRequest;
import com.dongruan.environment.dto.NepsLoginResponse;
import com.dongruan.environment.entity.Supervisor;
import com.dongruan.environment.mapper.SupervisorMapper;
import com.dongruan.environment.service.INepsAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dongruan.environment.auth.SupervisorAlreadyExistsException;
import com.dongruan.environment.dto.NepsRegisterRequest;

@Service
@RequiredArgsConstructor
public class NepsAuthServiceImpl implements INepsAuthService {

    private final SupervisorMapper supervisorMapper;
    private final Pbkdf2PasswordHasher passwordHasher;

    @Override
    public NepsLoginResponse authenticate(final NepsLoginRequest request) {
        final Supervisor supervisor = supervisorMapper.selectOne(
                new LambdaQueryWrapper<Supervisor>().eq(Supervisor::getTelId, request.telId()));
        if (supervisor == null || !passwordHasher.matches(request.password(), supervisor.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!passwordHasher.isHash(supervisor.getPassword())) {
            supervisor.setPassword(passwordHasher.hash(request.password()));
            supervisorMapper.updateById(supervisor);
        }
        return new NepsLoginResponse(supervisor.getTelId(), supervisor.getRealName());
    }

    @Override
    public void register(final NepsRegisterRequest request) {
        final Supervisor existing = supervisorMapper.selectById(request.telId());
        if (existing != null) {
            throw new SupervisorAlreadyExistsException();
        }
        final Supervisor supervisor = new Supervisor();
        supervisor.setTelId(request.telId());
        supervisor.setPassword(passwordHasher.hash(request.password()));

        final String suffix = request.telId().length() >= 4
                ? request.telId().substring(request.telId().length() - 4)
                : request.telId();
        supervisor.setRealName("环保监督员_" + suffix);
        supervisor.setBirthday("2000-01-01");
        supervisor.setSex(1);
        supervisor.setRemarks("自主注册账号");

        supervisorMapper.insert(supervisor);
    }
}
