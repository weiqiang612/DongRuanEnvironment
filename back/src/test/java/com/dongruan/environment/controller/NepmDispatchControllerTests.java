package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.service.INepmDispatchService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NepmDispatchController.class)
class NepmDispatchControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private INepmDispatchService dispatchService;

    @Test
    void rejectsAnonymousManagementRequest() throws Exception {
        mockMvc.perform(get("/nepm/dashboard"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectsNonAdministratorManagementRequest() throws Exception {
        mockMvc.perform(get("/nepm/dashboard")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPG_GRID_MEMBER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "grid-01"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void returnsOperationalOverviewForAdministrator() throws Exception {
        when(dispatchService.overview(any(), any(), any(), any()))
                .thenReturn(Map.of("total", 5L, "pending", 2L, "assigned", 2L, "completed", 1L, "timeout", 1L));

        mockMvc.perform(get("/nepm/analytics/overview")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(5));
    }

    @Test
    void translatesStateConflictFromDispatchService() throws Exception {
        when(dispatchService.dispatch(9, "GM-NEW", "admin"))
                .thenThrow(new IllegalStateException("反馈状态已变化，请刷新后重试"));

        mockMvc.perform(post("/nepm/feedbacks/9/dispatch")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gridMemberId\":\"GM-NEW\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));

        verify(dispatchService).dispatch(9, "GM-NEW", "admin");
    }
}
