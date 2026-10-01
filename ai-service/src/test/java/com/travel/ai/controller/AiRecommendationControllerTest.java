package com.travel.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.service.AiRecommendationService;
import com.travel.ai.viewmodel.TripRecommendationVm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiRecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiRecommendationService aiRecommendationService;

    @Test
    @DisplayName("REST API /api/v1/ai/recommendations trả về HTTP 200 OK và ApiResponse chuẩn")
    void getRecommendations_ApiSuccess() throws Exception {
        AiQueryRequest request = AiQueryRequest.builder()
                .queryText("Tư vấn tour đi Đà Nẵng")
                .build();

        TripRecommendationVm mockVm = TripRecommendationVm.builder()
                .introduction("Bài tư vấn tour Đà Nẵng")
                .recommendedTours(Collections.emptyList())
                .detailedItinerary(Collections.emptyList())
                .build();

        when(aiRecommendationService.getRecommendation(any())).thenReturn(mockVm);

        mockMvc.perform(post("/api/v1/ai/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.introduction").value("Bài tư vấn tour Đà Nẵng"));
    }
}
