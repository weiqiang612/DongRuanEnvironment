package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.auth.EmployeeRole;
import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.dto.NepmDetectionStatsVO;
import com.dongruan.environment.dto.NepmPageResponse;
import com.dongruan.environment.service.INepmDetectionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NepmDetectionController.class)
class NepmDetectionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INepmDetectionService detectionService;

    @Test
    void rejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/nepm/detection-results"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/nepm/detection-results/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/nepm/aqi-alerts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/nepm/aqi-alerts/1/handle"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/nepm/analytics/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void rejectsNonAdminEmployeeRole() throws Exception {
        mockMvc.perform(get("/nepm/detection-results")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPG_GRID_MEMBER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "grid-01"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(post("/nepm/aqi-alerts/1/handle")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPV_DECISION_MAKER.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "leader"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void returnsDetectionResultsForAdministrator() throws Exception {
        final NepmDetectionResultVO item = sampleDetection(1, 10, 2);
        when(detectionService.findDetectionResults(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new NepmPageResponse<>(List.of(item), 1, 1, 10));

        mockMvc.perform(get("/nepm/detection-results")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].cityName").value("北京市"))
                .andExpect(jsonPath("$.data.items[0].aqiId").value(2))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void translatesIllegalArgumentExceptionToBadRequestInDetectionResults() throws Exception {
        when(detectionService.findDetectionResults(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("提交开始日期不能晚于结束日期"));

        mockMvc.perform(get("/nepm/detection-results")
                        .param("submittedFrom", "2026-09-15")
                        .param("submittedTo", "2026-09-10")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("提交开始日期不能晚于结束日期"));
    }

    @Test
    void returnsDetectionResultDetailWhenFound() throws Exception {
        final NepmDetectionResultVO item = sampleDetection(1, 10, 2);
        when(detectionService.findDetectionResultById(1)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/nepm/detection-results/1")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.feedbackId").value(10));
    }

    @Test
    void returnsNotFoundWhenDetectionResultDoesNotExist() throws Exception {
        when(detectionService.findDetectionResultById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/nepm/detection-results/999")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void returnsAqiAlertsForAdministrator() throws Exception {
        final NepmAqiAlertVO alert = sampleAlert(1L, 10, 1, 4, "PENDING");
        when(detectionService.findAqiAlerts(any(), any(), any(), any(), any()))
                .thenReturn(new NepmPageResponse<>(List.of(alert), 1, 1, 10));

        mockMvc.perform(get("/nepm/aqi-alerts")
                        .param("status", "PENDING")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].alertStatus").value("PENDING"));
    }

    @Test
    void translatesIllegalArgumentExceptionToBadRequestInAqiAlerts() throws Exception {
        when(detectionService.findAqiAlerts(eq("BAD"), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("预警状态必须为 PENDING 或 HANDLED"));

        mockMvc.perform(get("/nepm/aqi-alerts")
                        .param("status", "BAD")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void handlesAlertSuccessfully() throws Exception {
        final NepmAqiAlertVO handled = sampleAlert(1L, 10, 1, 4, "HANDLED");
        when(detectionService.handleAlert(1L)).thenReturn(handled);

        mockMvc.perform(post("/nepm/aqi-alerts/1/handle")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("处置成功"))
                .andExpect(jsonPath("$.data.alertStatus").value("HANDLED"));

        verify(detectionService).handleAlert(1L);
    }

    @Test
    void handleAlertReturnsNotFoundWhenAlertDoesNotExist() throws Exception {
        when(detectionService.handleAlert(999L)).thenThrow(new NoSuchElementException("预警记录不存在"));

        mockMvc.perform(post("/nepm/aqi-alerts/999/handle")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void handleAlertReturnsConflictWhenAlreadyHandledOrRaceCondition() throws Exception {
        when(detectionService.handleAlert(1L)).thenThrow(new IllegalStateException("预警已被处置或状态不符"));

        mockMvc.perform(post("/nepm/aqi-alerts/1/handle")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("预警已被处置或状态不符"));
    }

    @Test
    void returnsAnalyticsStatsForAdministrator() throws Exception {
        final NepmDetectionStatsVO stats = new NepmDetectionStatsVO(
                120L,
                List.of(new NepmDetectionStatsVO.AqiDistributionItem(1, "一级（优）", 40L)),
                List.of(new NepmDetectionStatsVO.MonthlyTrendItem("2026-09", 70L, 15L)),
                25L, 5L, 20L
        );
        when(detectionService.getDetectionStats(any(), any(), any(), any())).thenReturn(stats);

        mockMvc.perform(get("/nepm/analytics/stats")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalDetections").value(120))
                .andExpect(jsonPath("$.data.highAlertCount").value(25))
                .andExpect(jsonPath("$.data.pendingAlertCount").value(5))
                .andExpect(jsonPath("$.data.handledAlertCount").value(20));
    }

    @Test
    void translatesIllegalArgumentExceptionInAnalyticsStats() throws Exception {
        when(detectionService.getDetectionStats(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("提交开始日期不能晚于结束日期"));

        mockMvc.perform(get("/nepm/analytics/stats")
                        .param("submittedFrom", "2026-09-15")
                        .param("submittedTo", "2026-09-10")
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ROLE, EmployeeRole.NEPM_ADMIN.name())
                        .sessionAttr(AuthController.SESSION_EMPLOYEE_ACCOUNT_CODE, "admin"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    private NepmDetectionResultVO sampleDetection(final int id, final int feedbackId, final int aqiId) {
        return new NepmDetectionResultVO(
                id, feedbackId, 110000, "北京市", 110100, "北京市",
                "中关村南大街1号", "异味", 3, "13900000001", "张三",
                new BigDecimal("45.00"), 2, new BigDecimal("8.50"), 3,
                new BigDecimal("110.00"), aqiId, aqiId,
                LocalDateTime.of(2026, 9, 12, 10, 0),
                LocalDateTime.of(2026, 9, 12, 9, 0)
        );
    }

    private NepmAqiAlertVO sampleAlert(final long id, final int feedbackId, final int resultId, final int level, final String status) {
        return new NepmAqiAlertVO(
                id, feedbackId, resultId, level, status,
                LocalDateTime.of(2026, 9, 12, 10, 0),
                "HANDLED".equals(status) ? LocalDateTime.of(2026, 9, 12, 11, 0) : null,
                110000, "北京市", 110100, "北京市", "中关村南大街1号", level, "张三"
        );
    }
}
