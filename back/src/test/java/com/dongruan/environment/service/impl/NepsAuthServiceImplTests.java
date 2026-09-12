package com.dongruan.environment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.auth.Pbkdf2PasswordHasher;
import com.dongruan.environment.dto.NepsLoginRequest;
import com.dongruan.environment.dto.NepsLoginResponse;
import com.dongruan.environment.entity.Supervisor;
import com.dongruan.environment.mapper.SupervisorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NepsAuthServiceImplTests {

    @Mock
    private SupervisorMapper supervisorMapper;

    @Mock
    private Pbkdf2PasswordHasher passwordHasher;

    @InjectMocks
    private NepsAuthServiceImpl service;

    @Test
    void upgradesLegacyPasswordAfterSuccessfulAuthentication() {
        final Supervisor supervisor = supervisor("legacy-password");
        when(supervisorMapper.selectOne(any())).thenReturn(supervisor);
        when(passwordHasher.matches("legacy-password", "legacy-password")).thenReturn(true);
        when(passwordHasher.isHash("legacy-password")).thenReturn(false);
        when(passwordHasher.hash("legacy-password")).thenReturn("pbkdf2$210000$salt$hash");

        final NepsLoginResponse result = service.authenticate(new NepsLoginRequest("13800000000", "legacy-password"));

        assertEquals("13800000000", result.telId());
        assertEquals("测试监督员", result.realName());
        assertEquals("pbkdf2$210000$salt$hash", supervisor.getPassword());
        verify(supervisorMapper).updateById(supervisor);
    }

    @Test
    void acceptsStoredPbkdf2PasswordWithoutRehashing() {
        final Supervisor supervisor = supervisor("pbkdf2$210000$salt$hash");
        when(supervisorMapper.selectOne(any())).thenReturn(supervisor);
        when(passwordHasher.matches("correct-password", supervisor.getPassword())).thenReturn(true);
        when(passwordHasher.isHash(supervisor.getPassword())).thenReturn(true);

        service.authenticate(new NepsLoginRequest("13800000000", "correct-password"));

        verify(supervisorMapper, never()).updateById(any(Supervisor.class));
    }

    @Test
    void rejectsUnknownPhoneAndWrongPassword() {
        when(supervisorMapper.selectOne(any())).thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate(new NepsLoginRequest("13800000000", "password")));

        final Supervisor supervisor = supervisor("pbkdf2$hash");
        when(supervisorMapper.selectOne(any())).thenReturn(supervisor);
        when(passwordHasher.matches("password", supervisor.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate(new NepsLoginRequest("13800000000", "password")));
        verify(supervisorMapper, never()).updateById(any(Supervisor.class));
    }

    @Test
    void registersNewSupervisorWithPbkdf2HashAndDefaultValues() {
        when(supervisorMapper.selectById("13912345678")).thenReturn(null);
        when(passwordHasher.hash("securePassword")).thenReturn("pbkdf2$600000$salt$hash");

        service.register(new com.dongruan.environment.dto.NepsRegisterRequest("13912345678", "securePassword"));

        final org.mockito.ArgumentCaptor<Supervisor> captor = org.mockito.ArgumentCaptor.forClass(Supervisor.class);
        verify(supervisorMapper).insert(captor.capture());
        final Supervisor inserted = captor.getValue();
        assertEquals("13912345678", inserted.getTelId());
        assertEquals("pbkdf2$600000$salt$hash", inserted.getPassword());
        assertEquals("环保监督员_5678", inserted.getRealName());
        assertEquals("2000-01-01", inserted.getBirthday());
        assertEquals(1, inserted.getSex());
    }

    @Test
    void rejectsRegisterWhenPhoneAlreadyExists() {
        when(supervisorMapper.selectById("13800000000")).thenReturn(new Supervisor());

        assertThrows(com.dongruan.environment.auth.SupervisorAlreadyExistsException.class,
                () -> service.register(new com.dongruan.environment.dto.NepsRegisterRequest("13800000000", "pass1234")));

        verify(supervisorMapper, never()).insert(any(Supervisor.class));
    }

    private Supervisor supervisor(final String password) {
        final Supervisor supervisor = new Supervisor();
        supervisor.setTelId("13800000000");
        supervisor.setRealName("测试监督员");
        supervisor.setPassword(password);
        return supervisor;
    }
}
