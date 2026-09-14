package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.dto.NepvDashboardVO;
import com.dongruan.environment.service.INepvDashboardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NepvDashboardController.class)
class NepvDashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INepvDashboardService dashboardService;

    @Test
    void rejectsAnonymousAndNonDecisionSessions() throws Exception {
        mockMvc.perform(get("/nepv/dashboard"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value(401));
        mockMvc.perform(get("/nepv/dashboard")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void returnsDashboardForDecisionMaker() throws Exception {
        when(dashboardService.getDashboard(any(), any(), any(), any())).thenReturn(new NepvDashboardVO(
                4, 2, 2, 1, new NepvDashboardVO.GridCoverage(3, 2, 66.7),
                List.of(new NepvDashboardVO.AqiDistributionItem(1, "一级（优）", 2)),
                List.of(new NepvDashboardVO.MonthlyTrendItem("2026-09", 4, 2)),
                List.of(new NepvDashboardVO.ProvinceRiskItem(1, "北京市", 4, 2, 1)), List.of(), List.of()));
        mockMvc.perform(get("/nepv/dashboard")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPV_DECISION_MAKER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "leader"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalDetections").value(4))
                .andExpect(jsonPath("$.data.gridCoverage.coverageRate").value(66.7))
                .andExpect(jsonPath("$.data.provinceRisks[0].provinceName").value("北京市"));
    }

    @Test
    void translatesInvalidDatesToBadRequest() throws Exception {
        when(dashboardService.getDashboard(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("提交开始日期不能晚于结束日期"));
        mockMvc.perform(get("/nepv/dashboard").param("submittedFrom", "2026-09-15").param("submittedTo", "2026-09-10")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPV_DECISION_MAKER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "leader"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(400));
    }
}
