package com.dongruan.environment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dongruan.environment.dto.MeasurementRequest;
import com.dongruan.environment.entity.Aqi;
import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.entity.AlertRecord;
import com.dongruan.environment.entity.DetectionResult;
import com.dongruan.environment.entity.GridMember;
import com.dongruan.environment.mapper.AlertRecordMapper;
import com.dongruan.environment.mapper.AqiFeedbackMapper;
import com.dongruan.environment.mapper.AqiMapper;
import com.dongruan.environment.mapper.DetectionResultMapper;
import com.dongruan.environment.mapper.GridMemberMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NepgTaskServiceImplTests {
    @Mock private GridMemberMapper gridMemberMapper;
    @Mock private AqiFeedbackMapper feedbackMapper;
    @Mock private AqiMapper aqiMapper;
    @Mock private DetectionResultMapper resultMapper;
    @Mock private AlertRecordMapper alertRecordMapper;
    @InjectMocks private NepgTaskServiceImpl service;

    @Test
    void calculatesMaximumPollutantGradeAndCreatesAlert() {
        final AqiFeedback feedback = feedback(1, "GM-1");
        when(gridMemberMapper.selectOne(any())).thenReturn(member());
        when(feedbackMapper.selectById(9)).thenReturn(feedback);
        when(feedbackMapper.update(any(), any())).thenReturn(1);
        when(resultMapper.selectCount(any())).thenReturn(0L);
        when(aqiMapper.selectList(null)).thenReturn(List.of(aqi(1, 0, 50), aqi(4, 51, 100), aqi(5, 101, 200)));
        when(feedbackMapper.findDetailForNepm(9)).thenReturn(feedback);
        when(resultMapper.selectOne(any())).thenReturn(null);

        service.submitMeasurement(9, "grid-code", new MeasurementRequest(new BigDecimal("68"), new BigDecimal("68"), new BigDecimal("156")));

        final ArgumentCaptor<DetectionResult> result = ArgumentCaptor.forClass(DetectionResult.class);
        verify(resultMapper).insert(result.capture());
        assertEquals(5, result.getValue().getAqiId());
        assertEquals(4, result.getValue().getSo2Level());
        verify(alertRecordMapper).insert(any(AlertRecord.class));
    }

    @Test
    void rejectsUnmatchedValueBeforeCompletingTask() {
        when(gridMemberMapper.selectOne(any())).thenReturn(member());
        when(feedbackMapper.selectById(9)).thenReturn(feedback(1, "GM-1"));
        when(resultMapper.selectCount(any())).thenReturn(0L);
        when(aqiMapper.selectList(null)).thenReturn(List.of(aqi(1, 0, 50)));

        assertThrows(IllegalArgumentException.class, () -> service.submitMeasurement(9, "grid-code",
                new MeasurementRequest(new BigDecimal("68"), BigDecimal.ONE, BigDecimal.ONE)));
        verify(feedbackMapper, never()).update(any(), any());
    }

    @Test
    void rejectsTaskAssignedToAnotherGridMember() {
        when(gridMemberMapper.selectOne(any())).thenReturn(member());
        when(feedbackMapper.selectById(9)).thenReturn(feedback(1, "GM-OTHER"));

        assertThrows(SecurityException.class, () -> service.findMyTask(9, "grid-code"));
    }

    private GridMember member() { final GridMember member = new GridMember(); member.setGmId("GM-1"); return member; }
    private AqiFeedback feedback(final int state, final String gmId) { final AqiFeedback feedback = new AqiFeedback(); feedback.setAfId(9); feedback.setState(state); feedback.setGmId(gmId); return feedback; }
    private Aqi aqi(final int level, final int min, final int max) { final Aqi aqi = new Aqi(); aqi.setAqiId(level); aqi.setSo2Min(min); aqi.setSo2Max(max); aqi.setCoMin(min); aqi.setCoMax(max); aqi.setSpmMin(min); aqi.setSpmMax(max); return aqi; }
}
