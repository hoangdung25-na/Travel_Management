package com.travel.tour.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.tour.constant.TourStatus;
import com.travel.tour.dto.CreateItineraryRequest;
import com.travel.tour.dto.CreateTourRequest;
import com.travel.tour.service.TourService;
import com.travel.tour.viewmodel.TourDetailVm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = TourController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class TourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TourService tourService;

    @Test
    @DisplayName("POST /api/v1/tours - Tạo Tour mới trả về HTTP 201 CREATED")
    void createTour_Returns201Created() throws Exception {
        UUID tourId = UUID.randomUUID();

        CreateItineraryRequest itinerary = CreateItineraryRequest.builder()
                .dayNumber(1)
                .title("Ngày 1: Tham quan vịnh Hạ Long")
                .content("Khám phá Động Thiên Cung và Hòn Gà Chọi")
                .build();

        CreateTourRequest request = CreateTourRequest.builder()
                .code("TOUR-HALONG-01")
                .title("Tour Du Lịch Hạ Long")
                .description("Hành trình di sản thiên nhiên thế giới")
                .itineraries(List.of(itinerary))
                .destinationIds(Collections.emptySet())
                .build();

        TourDetailVm responseVm = TourDetailVm.builder()
                .tourId(tourId)
                .code("TOUR-HALONG-01")
                .title("Tour Du Lịch Hạ Long")
                .status(TourStatus.DRAFT)
                .build();

        when(tourService.createTour(any(CreateTourRequest.class))).thenReturn(responseVm);

        mockMvc.perform(post("/api/v1/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.code").value("TOUR-HALONG-01"));
    }

    @Test
    @DisplayName("GET /api/v1/tours/{id} - Lấy chi tiết Tour trả về HTTP 200 OK")
    void getTourById_Returns200OK() throws Exception {
        UUID tourId = UUID.randomUUID();
        TourDetailVm responseVm = TourDetailVm.builder()
                .tourId(tourId)
                .code("TOUR-HALONG-01")
                .title("Tour Du Lịch Hạ Long")
                .status(TourStatus.PUBLISHED)
                .build();

        when(tourService.getTourById(tourId.toString())).thenReturn(responseVm);

        mockMvc.perform(get("/api/v1/tours/{id}", tourId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tourId").value(tourId.toString()))
                .andExpect(jsonPath("$.data.title").value("Tour Du Lịch Hạ Long"));
    }
}
