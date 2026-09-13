package com.dongruan.environment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongruan.environment.entity.Admin;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.entity.TaskAssignLog;
import com.dongruan.environment.dto.NepmRecentTrendVO;
import com.dongruan.environment.mapper.AdminMapper;
import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.mapper.GridMemberMapper;
import com.dongruan.environment.mapper.TaskAssignLogMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NepmDispatchServiceImplTests {

    @Mock
    private AqiFeedbackMapper feedbackMapper;
    @Mock
    private GridMemberMapper gridMemberMapper;
    @Mock
    private AdminMapper adminMapper;
    @Mock
    private TaskAssignLogMapper taskAssignLogMapper;
    @InjectMocks
    private NepmDispatchServiceImpl service;

    @Test
    void candidatesPreferSameCityAndKeepSameProvinceFallback() {
        when(feedbackMapper.findDetailForNepm(9)).thenReturn(feedback(0, null));
        when(gridMemberMapper.findAvailableForNepm()).thenReturn(List.of(
                member("GM-OTHER", 1, 12, "石家庄市"),
                member("GM-SAME", 1, 11, "北京市"),
                member("GM-OUTSIDE", 2, 21, "天津市")));

        final var candidates = service.findCandidates(9);

        assertEquals(List.of("GM-SAME", "GM-OTHER"), candidates.stream().map(item -> item.gmId()).toList());
        assertEquals("SAME_CITY", candidates.get(0).sourceLevel());
        assertEquals("SAME_PROVINCE", candidates.get(1).sourceLevel());
    }

    @Test
    void returnsEmptyCandidatesWhenProvinceHasNoAvailableMember() {
        when(feedbackMapper.findDetailForNepm(9)).thenReturn(feedback(0, null));
        when(gridMemberMapper.findAvailableForNepm()).thenReturn(List.of(member("GM-OUTSIDE", 2, 21, "天津市")));

        assertEquals(List.of(), service.findCandidates(9));
    }

    @Test
    void rejectsCompletedFeedbackBeforeWritingDispatchLog() {
        when(feedbackMapper.findDetailForNepm(9)).thenReturn(feedback(2, "GM-OLD"));

        assertThrows(IllegalStateException.class, () -> service.dispatch(9, "GM-NEW", "admin"));

        verify(taskAssignLogMapper, never()).insert(any(TaskAssignLog.class));
    }

    @Test
    void rejectsDispatchToUnavailableMemberBeforeUpdatingFeedback() {
        when(feedbackMapper.findDetailForNepm(9)).thenReturn(feedback(0, null));
        final Admin admin = new Admin();
        admin.setAdminId(3);
        when(adminMapper.selectOne(any())).thenReturn(admin);
        final GridMember unavailable = member("GM-NEW", 1, 11, "北京市");
        unavailable.setState(1);
        when(gridMemberMapper.selectById("GM-NEW")).thenReturn(unavailable);

        assertThrows(IllegalStateException.class, () -> service.dispatch(9, "GM-NEW", "admin"));

        verify(taskAssignLogMapper, never()).insert(any(TaskAssignLog.class));
    }

    @Test
    void rejectsOverviewWithInvertedDateRange() {
        assertThrows(IllegalArgumentException.class,
                () -> service.overview(null, null, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1)));
    }

    @Test
    void recentTrendUsesRealDailyFlowsAndEndOfDayBacklog() {
        final LocalDate today = LocalDate.now();
        when(feedbackMapper.findAll()).thenReturn(List.of(
                feedbackWithDates(1, today.minusDays(6).atTime(9, 0), null),
                feedbackWithDates(2, today.minusDays(5).atTime(9, 0), today.minusDays(4).atTime(10, 0)),
                feedbackWithDates(2, today.atTime(9, 0), today.atTime(15, 0))));

        final NepmRecentTrendVO trend = service.recentTrend();

        assertEquals(List.of(1L, 1L, 0L, 0L, 0L, 0L, 1L), trend.newFeedbacks());
        assertEquals(List.of(0L, 0L, 1L, 0L, 0L, 0L, 1L), trend.completedFeedbacks());
        assertEquals(List.of(1L, 2L, 1L, 1L, 1L, 1L, 1L), trend.pendingFeedbacks());
    }

    @Test
    void recentTrendKeepsDaysWithoutBusinessFactsAtZero() {
        when(feedbackMapper.findAll()).thenReturn(List.of());

        final NepmRecentTrendVO trend = service.recentTrend();

        assertEquals(List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L), trend.newFeedbacks());
        assertEquals(List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L), trend.completedFeedbacks());
        assertEquals(List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L), trend.pendingFeedbacks());
    }

    private AqiFeedback feedbackWithDates(final int state, final LocalDateTime submittedAt,
                                          final LocalDateTime completedAt) {
        final AqiFeedback feedback = feedback(state, null);
        feedback.setSubmittedAt(submittedAt);
        feedback.setCompletedAt(completedAt);
        return feedback;
    }

    private AqiFeedback feedback(final int state, final String memberId) {
        final AqiFeedback feedback = new AqiFeedback();
        feedback.setAfId(9);
        feedback.setProvinceId(1);
        feedback.setCityId(11);
        feedback.setState(state);
        feedback.setGmId(memberId);
        return feedback;
    }

    private GridMember member(final String id, final int provinceId, final int cityId, final String cityName) {
        final GridMember member = new GridMember();
        member.setGmId(id);
        member.setGmName(id);
        member.setProvinceId(provinceId);
        member.setCityId(cityId);
        member.setCityName(cityName);
        member.setState(0);
        return member;
    }
}
