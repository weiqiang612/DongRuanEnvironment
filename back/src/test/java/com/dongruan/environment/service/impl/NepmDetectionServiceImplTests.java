package com.dongruan.environment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongruan.environment.dto.AlertRecordStatItem;
import com.dongruan.environment.dto.NepmAqiAlertVO;
import com.dongruan.environment.dto.NepmDetectionResultVO;
import com.dongruan.environment.dto.NepmDetectionStatsVO;
import com.dongruan.environment.dto.NepmPageResponse;
import com.dongruan.environment.entity.AlertRecord;
import com.dongruan.environment.mapper.AlertRecordMapper;
import com.dongruan.environment.mapper.DetectionResultMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NepmDetectionServiceImplTests {

    @Mock
    private DetectionResultMapper detectionResultMapper;

    @Mock
    private AlertRecordMapper alertRecordMapper;

    @Mock
    private com.dongruan.environment.mapper.AqiFeedbackMapper aqiFeedbackMapper;

    @InjectMocks
    private NepmDetectionServiceImpl service;

    @Test
    void findDetectionResults_happyPathAndPagination() {
        final NepmDetectionResultVO item1 = sampleDetection(1, 10, 2, LocalDateTime.of(2026, 9, 12, 10, 0));
        final NepmDetectionResultVO item2 = sampleDetection(2, 11, 4, LocalDateTime.of(2026, 9, 12, 11, 0));
        when(detectionResultMapper.findForNepm(eq(110000), eq(110100), eq(4), any(), any(), eq("海淀")))
                .thenReturn(List.of(item1, item2));

        final NepmPageResponse<NepmDetectionResultVO> response = service.findDetectionResults(
                110000, 110100, 4,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 12),
                "海淀", 1, 1);

        assertEquals(2, response.total());
        assertEquals(1, response.items().size());
        assertEquals(1, response.page());
        assertEquals(1, response.pageSize());
        assertEquals(1, response.items().get(0).id());
    }

    @Test
    void findDetectionResults_rejectsInvalidDateRange() {
        assertThrows(IllegalArgumentException.class, () -> service.findDetectionResults(
                null, null, null,
                LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 10),
                null, 1, 10));
    }

    @Test
    void findDetectionResults_rejectsInvalidAqiId() {
        assertThrows(IllegalArgumentException.class, () -> service.findDetectionResults(
                null, null, 7,
                null, null, null, 1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.findDetectionResults(
                null, null, 0,
                null, null, null, 1, 10));
    }

    @Test
    void findDetectionResultById_foundAndNotFound() {
        when(detectionResultMapper.findDetailForNepm(99)).thenReturn(null);
        assertTrue(service.findDetectionResultById(99).isEmpty());

        final NepmDetectionResultVO detail = sampleDetection(1, 10, 2, LocalDateTime.now());
        when(detectionResultMapper.findDetailForNepm(1)).thenReturn(detail);
        final Optional<NepmDetectionResultVO> found = service.findDetectionResultById(1);
        assertTrue(found.isPresent());
        assertEquals(1, found.get().id());
    }

    @Test
    void findAqiAlerts_filtersByStatusAndRegion() {
        final NepmAqiAlertVO alert = sampleAlert(1L, 10, 1, 4, "PENDING", LocalDateTime.of(2026, 9, 12, 10, 0));
        when(alertRecordMapper.findForNepm(eq("PENDING"), eq(110000), eq(110100)))
                .thenReturn(List.of(alert));

        final NepmPageResponse<NepmAqiAlertVO> response = service.findAqiAlerts(
                "pending", 110000, 110100, 1, 10);

        assertEquals(1, response.total());
        assertEquals("PENDING", response.items().get(0).alertStatus());
    }

    @Test
    void findAqiAlerts_rejectsInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> service.findAqiAlerts(
                "INVALID_STATUS", null, null, 1, 10));
    }

    @Test
    void handleAlert_success() {
        final AlertRecord existing = new AlertRecord();
        existing.setId(5L);
        existing.setFeedbackId(20);
        existing.setResultId(100);
        existing.setAlertLevel(5);
        existing.setAlertStatus("PENDING");
        existing.setCreatedAt(LocalDateTime.of(2026, 9, 12, 9, 0));

        when(alertRecordMapper.selectById(5L)).thenReturn(existing);
        when(alertRecordMapper.handleAlert(eq(5L), any())).thenReturn(1);

        final NepmAqiAlertVO handledVO = sampleAlert(5L, 20, 100, 5, "HANDLED", existing.getCreatedAt());
        when(alertRecordMapper.findDetailForNepm(5L)).thenReturn(handledVO);

        final NepmAqiAlertVO result = service.handleAlert(5L);
        assertNotNull(result);
        assertEquals(5L, result.id());
        assertEquals("HANDLED", result.alertStatus());
        verify(alertRecordMapper).handleAlert(eq(5L), any());
    }

    @Test
    void handleAlert_notFound() {
        when(alertRecordMapper.selectById(999L)).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> service.handleAlert(999L));
        verify(alertRecordMapper, never()).handleAlert(any(), any());
    }

    @Test
    void handleAlert_conflictWhenAlreadyHandled() {
        final AlertRecord existing = new AlertRecord();
        existing.setId(5L);
        existing.setAlertStatus("HANDLED");
        when(alertRecordMapper.selectById(5L)).thenReturn(existing);

        assertThrows(IllegalStateException.class, () -> service.handleAlert(5L));
        verify(alertRecordMapper, never()).handleAlert(any(), any());
    }

    @Test
    void handleAlert_conflictWhenUpdateZeroRows() {
        final AlertRecord existing = new AlertRecord();
        existing.setId(5L);
        existing.setAlertStatus("PENDING");
        when(alertRecordMapper.selectById(5L)).thenReturn(existing);
        when(alertRecordMapper.handleAlert(eq(5L), any())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> service.handleAlert(5L));
    }

    @Test
    void getDetectionStats_aggregatesProperly() {
        final NepmDetectionResultVO d1 = sampleDetection(1, 10, 1, LocalDateTime.of(2026, 8, 15, 10, 0));
        final NepmDetectionResultVO d2 = sampleDetection(2, 11, 4, LocalDateTime.of(2026, 9, 2, 10, 0));
        final NepmDetectionResultVO d3 = sampleDetection(3, 12, 4, LocalDateTime.of(2026, 9, 10, 10, 0));
        when(detectionResultMapper.findForNepm(eq(110000), eq(110100), eq(null), any(), any(), eq(null)))
                .thenReturn(List.of(d1, d2, d3));

        final AlertRecordStatItem a1 = new AlertRecordStatItem(1L, "HANDLED", 4, LocalDateTime.of(2026, 9, 2, 10, 0));
        final AlertRecordStatItem a2 = new AlertRecordStatItem(2L, "PENDING", 4, LocalDateTime.of(2026, 9, 10, 10, 0));
        when(alertRecordMapper.findAlertsForStats(eq(110000), eq(110100), any(), any()))
                .thenReturn(List.of(a1, a2));

        final NepmDetectionStatsVO stats = service.getDetectionStats(110000, 110100, null, null);

        assertEquals(3L, stats.totalDetections());
        assertEquals(6, stats.aqiDistribution().size());
        assertEquals(1L, stats.aqiDistribution().get(0).count()); // level 1
        assertEquals(0L, stats.aqiDistribution().get(1).count()); // level 2
        assertEquals(2L, stats.aqiDistribution().get(3).count()); // level 4
        assertEquals(2L, stats.highAlertCount());
        assertEquals(1L, stats.pendingAlertCount());
        assertEquals(1L, stats.handledAlertCount());
        assertEquals(2, stats.monthlyTrends().size());
        assertEquals("2026-08", stats.monthlyTrends().get(0).month());
        assertEquals(1L, stats.monthlyTrends().get(0).detectionCount());
        assertEquals(0L, stats.monthlyTrends().get(0).alertCount());
        assertEquals("2026-09", stats.monthlyTrends().get(1).month());
        assertEquals(2L, stats.monthlyTrends().get(1).detectionCount());
        assertEquals(2L, stats.monthlyTrends().get(1).alertCount());
    }

    @Test
    void getDetectionStats_handlesEmptyData() {
        when(detectionResultMapper.findForNepm(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(alertRecordMapper.findAlertsForStats(any(), any(), any(), any()))
                .thenReturn(List.of());

        final NepmDetectionStatsVO stats = service.getDetectionStats(null, null, null, null);

        assertEquals(0L, stats.totalDetections());
        assertEquals(6, stats.aqiDistribution().size());
        assertEquals(0L, stats.highAlertCount());
        assertEquals(0L, stats.pendingAlertCount());
        assertEquals(0L, stats.handledAlertCount());
        assertTrue(stats.monthlyTrends().isEmpty());
    }

    private NepmDetectionResultVO sampleDetection(final int id, final int feedbackId, final int aqiId, final LocalDateTime time) {
        return new NepmDetectionResultVO(
                id, feedbackId, 110000, "北京市", 110100, "北京市",
                "中关村南大街1号", "异味", 3, "13900000001", "张三",
                new BigDecimal("45.00"), 2, new BigDecimal("8.50"), 3,
                new BigDecimal("110.00"), aqiId, aqiId, time, time
        );
    }

    private NepmAqiAlertVO sampleAlert(final long id, final int feedbackId, final int resultId, final int level, final String status, final LocalDateTime time) {
        return new NepmAqiAlertVO(
                id, feedbackId, resultId, level, status, time, "HANDLED".equals(status) ? time.plusHours(1) : null,
                110000, "北京市", 110100, "北京市", "中关村南大街1号", level, "张三"
        );
    }
}
