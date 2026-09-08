package com.dongruan.environment.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dongruan.environment.service.IRegionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegionController.class)
class RegionControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IRegionService regionService;

    @Test
    void rejectsCitiesRequestWithoutProvinceId() throws Exception {
        mockMvc.perform(get("/region/cities"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
