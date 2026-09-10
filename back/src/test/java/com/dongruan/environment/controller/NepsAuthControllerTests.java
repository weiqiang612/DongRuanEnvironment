package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.InvalidCredentialsException;
import com.dongruan.environment.dto.NepsLoginResponse;
import com.dongruan.environment.service.IEmployeeAuthService;
import com.dongruan.environment.service.INepsAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class NepsAuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INepsAuthService nepsAuthService;

    @MockBean
    private IEmployeeAuthService employeeAuthService;

    @Test
    void logsInAndCreatesSessionForValidCredentials() throws Exception {
        when(nepsAuthService.authenticate(any()))
                .thenReturn(new NepsLoginResponse("13800000000", "测试监督员"));

        mockMvc.perform(post("/auth/neps/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"13800000000\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.telId").value("13800000000"))
                .andExpect(jsonPath("$.data.realName").value("测试监督员"))
                .andExpect(request().sessionAttribute(AuthController.SESSION_TEL_ID, "13800000000"));
    }

    @Test
    void rejectsMissingCredentials() throws Exception {
        mockMvc.perform(post("/auth/neps/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.telId").value("手机号不能为空"))
                .andExpect(jsonPath("$.data.password").value("密码不能为空"));
    }

    @Test
    void rejectsUnknownPhoneOrWrongPasswordWithSameMessage() throws Exception {
        doThrow(new InvalidCredentialsException()).when(nepsAuthService).authenticate(any());

        mockMvc.perform(post("/auth/neps/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"13800000000\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("手机号或密码错误"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void logsOutAndInvalidatesCurrentSession() throws Exception {
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_TEL_ID, "13800000000");

        mockMvc.perform(post("/auth/neps/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
    }
}
