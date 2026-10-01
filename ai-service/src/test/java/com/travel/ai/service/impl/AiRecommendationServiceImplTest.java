package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.constant.ChunkType;
import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;
import com.travel.ai.service.PromptEngineeringService;
import com.travel.ai.service.SemanticCacheService;
import com.travel.ai.service.TourRetrievalService;
import com.travel.ai.viewmodel.RecommendedTourVm;
import com.travel.ai.viewmodel.TripRecommendationVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceImplTest {

    @Mock
    private TourRetrievalService tourRetrievalService;

    @Mock
    private PromptEngineeringService promptEngineeringService;

    @Mock
    private SemanticCacheService semanticCacheService;

    @Mock
    private ChatModel chatModel;

    private ObjectMapper objectMapper;

    @InjectMocks
    private AiRecommendationServiceImpl aiRecommendationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiRecommendationService = new AiRecommendationServiceImpl(
                tourRetrievalService,
                promptEngineeringService,
                semanticCacheService,
                chatModel,
                objectMapper
        );
    }

    @Test
    @DisplayName("Test 2.1: Tour không tồn tại - AI từ chối lịch sự và không tự bịa thông tin giả")
    void getRecommendation_NonExistentTour_PoliteDecline() {
        AiQueryRequest request = AiQueryRequest.builder()
                .queryText("Tôi muốn đi tour Sao Hỏa 5 ngày giá 100k")
                .build();

        when(semanticCacheService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(tourRetrievalService.retrieveRelevantChunks(any())).thenReturn(Collections.emptyList());

        TripRecommendationVm result = aiRecommendationService.getRecommendation(request);

        assertNotNull(result);
        assertTrue(result.getIntroduction().contains("Rất tiếc"));
        assertTrue(result.getRecommendedTours().isEmpty());
        assertTrue(result.getDetailedItinerary().isEmpty());
        verifyNoInteractions(chatModel); // Đảm bảo KHÔNG gọi LLM khi không có dữ liệu
    }

    @Test
    @DisplayName("Test 2.2: Tour thực tế - AI trả về bài tư vấn chuẩn kèm link đặt tour booking_url")
    void getRecommendation_RealTour_ReturnsValidRecommendation() throws Exception {
        UUID tourId = UUID.randomUUID();
        AiQueryRequest request = AiQueryRequest.builder()
                .queryText("Tôi muốn đi du lịch Đà Nẵng 3N2Đ giá dưới 5 triệu")
                .maxBudget(new BigDecimal("5000000"))
                .build();

        RetrievedChunkDto chunk = RetrievedChunkDto.builder()
                .id(UUID.randomUUID())
                .tourId(tourId)
                .chunkType(ChunkType.METADATA)
                .content("Tour Đà Nẵng 3N2Đ - Giá 4.500.000 VNĐ")
                .similarityScore(0.89)
                .build();

        TripRecommendationVm mockVm = TripRecommendationVm.builder()
                .introduction("Chào bạn, tôi xin gợi ý Tour Đà Nẵng 3N2Đ giá 4.5 triệu.")
                .recommendedTours(List.of(
                        RecommendedTourVm.builder()
                                .tourId(tourId)
                                .title("Tour Đà Nẵng 3N2Đ")
                                .price(new BigDecimal("4500000"))
                                .bookingUrl("/tours/" + tourId)
                                .reason("Phù hợp ngân sách và địa điểm yêu cầu")
                                .build()
                ))
                .detailedItinerary(Collections.emptyList())
                .build();

        String jsonResponse = objectMapper.writeValueAsString(mockVm);

        when(semanticCacheService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(tourRetrievalService.retrieveRelevantChunks(any())).thenReturn(List.of(chunk));
        when(promptEngineeringService.buildPrompt(any(), any(), any())).thenReturn("Full Prompt Text");

        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage(jsonResponse);

        when(mockGeneration.getOutput()).thenReturn(assistantMessage);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

        TripRecommendationVm result = aiRecommendationService.getRecommendation(request);

        assertNotNull(result);
        assertEquals(1, result.getRecommendedTours().size());
        assertEquals("/tours/" + tourId, result.getRecommendedTours().get(0).getBookingUrl());
        verify(semanticCacheService, times(1)).cacheResponse(eq(request.getQueryText()), anyString());
    }
}
