package com.dongruan.environment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.dongruan.environment.dto.AlertRecordStatItem;
import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.dto.NepvDashboardVO;
import com.dongruan.environment.dto.NepvGridCoverageStat;
import com.dongruan.environment.mapper.AlertRecordMapper;
import com.dongruan.environment.mapper.DetectionResultMapper;
import com.dongruan.environment.mapper.RegionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NepvDashboardServiceImplTests {

    @Mock
    private DetectionResultMapper detectionResultMapper;

    @Mock
    private AlertRecordMapper alertRecordMapper;

    @Mock
    private RegionMapper regionMapper;

    @InjectMocks
    private NepvDashboardServiceImpl service;

    @Test
    void selectedProvinceReturnsSortedCityRisksAndCurrentScopeMetrics() {
        final LocalDateTime september = LocalDateTime.of(2026, 9, 10, 10, 0);
        final NepmDetectionResultVO cityAHigh = sampleDetection(1, 130100, "石家庄市", 4, september);
        final NepmDetectionResultVO cityAExcellent = sampleDetection(2, 130100, "石家庄市", 1, september.plusDays(1));
        final NepmDetectionResultVO cityBSevere = sampleDetection(3, 130200, "唐山市", 6, september.plusDays(2));
        final NepmAqiAlertVO pendingAlert = sampleAlert(10L, 130200, "唐山市", 6, "PENDING", september.plusDays(2));

        when(detectionResultMapper.findForNepm(eq(1), eq(null), eq(null), any(), any(), eq(null)))
                .thenReturn(List.of(cityAHigh, cityAExcellent, cityBSevere));
        when(alertRecordMapper.findAlertsForStats(eq(1), eq(null), any(), any()))
                .thenReturn(List.of(new AlertRecordStatItem(10L, "PENDING", 6, september.plusDays(2))));
        when(alertRecordMapper.findForNepm(eq(null), eq(1), eq(null))).thenReturn(List.of(pendingAlert));
        when(regionMapper.findGridCoverage(eq(1), eq(null))).thenReturn(new NepvGridCoverageStat(2L, 1L));

        final NepvDashboardVO dashboard = service.getDashboard(1, null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        assertEquals(3L, dashboard.totalDetections());
        assertEquals(2L, dashboard.highPollutionDetections());
        assertEquals(50.0, dashboard.gridCoverage().coverageRate());
        assertEquals(2, dashboard.cityRisks().size());
        assertEquals("石家庄市", dashboard.cityRisks().get(0).cityName());
        assertEquals(1L, dashboard.cityRisks().get(0).highPollutionCount());
        assertEquals(0L, dashboard.cityRisks().get(0).pendingAlertCount());
        assertEquals(4, dashboard.cityRisks().get(0).dominantAqiId());
        assertEquals("唐山市", dashboard.cityRisks().get(1).cityName());
        assertEquals(6, dashboard.cityRisks().get(1).dominantAqiId());
        assertEquals(1L, dashboard.cityRisks().get(1).pendingAlertCount());
    }

    @Test
    void nationalFilterDoesNotReturnProvinceDrilldownData() {
        when(detectionResultMapper.findForNepm(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(alertRecordMapper.findAlertsForStats(any(), any(), any(), any())).thenReturn(List.of());
        when(alertRecordMapper.findForNepm(any(), any(), any())).thenReturn(List.of());
        when(regionMapper.findGridCoverage(any(), any())).thenReturn(new NepvGridCoverageStat(0L, 0L));

        assertTrue(service.getDashboard(null, null, null, null).cityRisks().isEmpty());
        assertEquals(0.0, service.getDashboard(null, null, null, null).gridCoverage().coverageRate());
    }

    @Test
    void selectedCityKeepsItsCityRiskRowForTheProvinceMap() {
        final LocalDateTime september = LocalDateTime.of(2026, 9, 10, 10, 0);
        final NepmDetectionResultVO city = sampleDetection(1, 130100, "石家庄市", 4, september);
        when(detectionResultMapper.findForNepm(eq(1), eq(130100), eq(null), any(), any(), eq(null)))
                .thenReturn(List.of(city));
        when(alertRecordMapper.findAlertsForStats(eq(1), eq(130100), any(), any())).thenReturn(List.of());
        when(alertRecordMapper.findForNepm(eq(null), eq(1), eq(130100))).thenReturn(List.of());
        when(regionMapper.findGridCoverage(eq(1), eq(130100))).thenReturn(new NepvGridCoverageStat(1L, 1L));

        final NepvDashboardVO dashboard = service.getDashboard(1, 130100, null, null);

        assertEquals(1, dashboard.cityRisks().size());
        assertEquals(130100, dashboard.cityRisks().get(0).cityId());
        assertEquals("石家庄市", dashboard.cityRisks().get(0).cityName());
    }

    @Test
    void rejectsInvertedDateRangeBeforeReadingData() {
        assertThrows(IllegalArgumentException.class, () -> service.getDashboard(1, null,
                LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1)));
    }

    private NepmDetectionResultVO sampleDetection(final int id, final int cityId, final String cityName,
            final int aqiId, final LocalDateTime submittedAt) {
        return new NepmDetectionResultVO(id, id, 1, "河北省", cityId, cityName, "演示路" + id + "号", "异味", 4,
                "1390000000" + id, "网格员", new BigDecimal("10.00"), 1, new BigDecimal("2.00"), 1,
                new BigDecimal("30.00"), 1, aqiId, submittedAt, submittedAt);
    }

    private NepmAqiAlertVO sampleAlert(final long id, final int cityId, final String cityName, final int level,
            final String status, final LocalDateTime createdAt) {
        return new NepmAqiAlertVO(id, (int) id, (int) id, level, status, createdAt, null,
                1, "河北省", cityId, cityName, "演示路", level, "网格员");
    }
}
