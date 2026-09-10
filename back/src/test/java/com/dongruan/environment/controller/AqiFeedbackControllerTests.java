package com.dongruan.environment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private static final String SUPERVISOR_TEL_ID = "13800000000";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAqiFeedbackService aqiFeedbackService;

    @Test
    void savesFeedbackUsingSessionSupervisorIdentity() throws Exception {
        when(aqiFeedbackService.saveFeedback(any(AqiFeedback.class))).thenReturn(true);

        mockMvc.perform(post("/aqiFeedback/save")
                        .sessionAttr(AuthController.SESSION_TEL_ID, SUPERVISOR_TEL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(aqiFeedbackService).saveFeedback(org.mockito.ArgumentMatchers.argThat(
                feedback -> SUPERVISOR_TEL_ID.equals(feedback.getTelId())
                        && feedback.getAfDate() != null
                        && feedback.getAfTime() != null));
    }

    @Test
    void rejectsUnauthenticatedFeedbackRequests() throws Exception {
        mockMvc.perform(get("/aqiFeedback/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void hidesFeedbackNotOwnedByCurrentSupervisor() throws Exception {
        when(aqiFeedbackService.findBySupervisorTelIdAndId(99, SUPERVISOR_TEL_ID))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/aqiFeedback/find/99")
                        .sessionAttr(AuthController.SESSION_TEL_ID, SUPERVISOR_TEL_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void rejectsUpdatingFeedbackThatIsNotEditable() throws Exception {
        when(aqiFeedbackService.updateOwnedPendingFeedback(any(AqiFeedback.class), eq(SUPERVISOR_TEL_ID)))
                .thenReturn(false);

        mockMvc.perform(post("/aqiFeedback/update")
                        .sessionAttr(AuthController.SESSION_TEL_ID, SUPERVISOR_TEL_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"afId\":99," + validRequest().substring(1)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void allowsDeletingPendingFeedbackOwnedByCurrentSupervisor() throws Exception {
        when(aqiFeedbackService.deleteOwnedPendingFeedback(99, SUPERVISOR_TEL_ID)).thenReturn(true);

        mockMvc.perform(get("/aqiFeedback/delete/99")
                        .sessionAttr(AuthController.SESSION_TEL_ID, SUPERVISOR_TEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String validRequest() {
        return "{\"provinceId\":1,\"cityId\":2,\"address\":\"测试地址\",\"information\":\"空气质量反馈\",\"estimatedGrade\":2}";
    }
}
