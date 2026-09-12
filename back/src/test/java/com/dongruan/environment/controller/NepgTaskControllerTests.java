package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.dto.NepgTaskResponse;
import com.dongruan.environment.service.INepgTaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NepgTaskController.class)
class NepgTaskControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private INepgTaskService taskService;

    @Test
    void rejectsAnonymousTaskRequest() throws Exception {
        mockMvc.perform(get("/nepg/tasks")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectsOtherEmployeeRole() throws Exception {
        mockMvc.perform(get("/nepg/tasks").sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void returnsOnlyGridMemberTasks() throws Exception {
        when(taskService.findMyTasks("grid-code")).thenReturn(List.of(new NepgTaskResponse(8, "北京市", "北京市", "建设路8号", "异味", 4, 1, false, null, null)));
        mockMvc.perform(get("/nepg/tasks").sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPG_GRID_MEMBER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "grid-code"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].afId").value(8));
    }

    @Test
    void mapsRepeatSubmissionToConflict() throws Exception {
        when(taskService.submitMeasurement(any(), any(), any())).thenThrow(new IllegalStateException("该任务已完成"));
        mockMvc.perform(post("/nepg/tasks/8/measurements").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"so2Value\":68,\"coValue\":1.2,\"spmValue\":156}")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPG_GRID_MEMBER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "grid-code"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value(409));
        verify(taskService).submitMeasurement(any(), any(), any());
    }
}
