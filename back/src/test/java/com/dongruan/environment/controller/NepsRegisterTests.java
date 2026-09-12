package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.SupervisorAlreadyExistsException;
import com.dongruan.environment.dto.NepsRegisterRequest;
import com.dongruan.environment.service.IEmployeeAuthService;
import com.dongruan.environment.service.INepsAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class NepsRegisterTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INepsAuthService nepsAuthService;

    @MockBean
    private IEmployeeAuthService employeeAuthService;

    @Test
    void registersSupervisorSuccessfully() throws Exception {
        doNothing().when(nepsAuthService).register(any(NepsRegisterRequest.class));

        mockMvc.perform(post("/auth/neps/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"13812345678\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data").value(true));

        verify(nepsAuthService).register(new NepsRegisterRequest("13812345678", "password123"));
    }

    @Test
    void rejectsDuplicatePhoneWithConflict() throws Exception {
        doThrow(new SupervisorAlreadyExistsException("该手机号已被注册"))
                .when(nepsAuthService).register(any(NepsRegisterRequest.class));

        mockMvc.perform(post("/auth/neps/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"13812345678\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("该手机号已被注册"));
    }

    @Test
    void rejectsInvalidPhoneOrShortPassword() throws Exception {
        mockMvc.perform(post("/auth/neps/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"12345\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.telId").value("手机号格式不正确"))
                .andExpect(jsonPath("$.data.password").value("密码长度必须在6-32位之间"));
    }
}
