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
}
