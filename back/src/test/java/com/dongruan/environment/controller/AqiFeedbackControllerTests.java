package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AqiFeedbackController.class)
class AqiFeedbackControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAqiFeedbackService aqiFeedbackService;

    @Test
    void savesValidFeedback() throws Exception {
        when(aqiFeedbackService.saveFeedback(any(AqiFeedback.class))).thenReturn(true);

        mockMvc.perform(post("/aqiFeedback/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(aqiFeedbackService).saveFeedback(any(AqiFeedback.class));
    }

    @Test
    void rejectsMissingRequiredFeedbackFields() throws Exception {
        mockMvc.perform(post("/aqiFeedback/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telId\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.data.telId").value("手机号不能为空"));
    }

    @Test
    void returnsNotFoundWhenUpdatingUnknownFeedback() throws Exception {
        when(aqiFeedbackService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(post("/aqiFeedback/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().replaceFirst("\\{", "{\"afId\":99,")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    private String validRequest() {
        return "{\"telId\":\"13800000000\",\"provinceId\":1,\"cityId\":2,\"address\":\"测试地址\",\"information\":\"空气质量反馈\",\"estimatedGrade\":2,\"afDate\":\"2026-09-08\",\"afTime\":\"10:20:30\"}";
    }
}
