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
    private com.travel.ai.service.ConversationMemoryService conversationMemoryService;

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
                conversationMemoryService,
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

    @Test
    @DisplayName("Test 3.4: Chatbot đa lượt - Nhớ ngữ cảnh câu hỏi trước qua Redis và lưu trữ hội thoại")
    void chat_MultiTurnContext_SavesAndUsesHistory() {
        String convId = "conv-abc";
        String q1 = "Tôi muốn đi tour Sa Pa 3N2Đ";
        String q2 = "Tour này có đón tại khách sạn không?";

        com.travel.ai.dto.ChatMessageDto priorUserMsg = com.travel.ai.dto.ChatMessageDto.builder()
                .role("user").content(q1).timestamp(1000L).build();
        com.travel.ai.dto.ChatMessageDto priorAiMsg = com.travel.ai.dto.ChatMessageDto.builder()
                .role("assistant").content("Dạ em gửi anh chị [Tour Sa Pa 3N2Đ](/tours/123)").timestamp(2000L).build();

        when(conversationMemoryService.getRecentMessages(eq(convId), eq(6)))
                .thenReturn(List.of(priorUserMsg, priorAiMsg));

        RetrievedChunkDto chunk = RetrievedChunkDto.builder()
                .id(UUID.randomUUID())
                .chunkType(ChunkType.POLICY)
                .content("Chính sách đón trả: Xe đón tận nơi tại các khách sạn khu vực phố cổ.")
                .similarityScore(0.92)
                .build();

        when(tourRetrievalService.retrieveKnowledgeChunks(anyString(), eq(4)))
                .thenReturn(List.of(chunk));
        when(promptEngineeringService.buildChatPrompt(eq(q2), anyList(), anyList()))
                .thenReturn("Chat Prompt with Context and History");

        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("Dạ tour có đón tận nơi tại khách sạn ạ!");
        when(mockGeneration.getOutput()).thenReturn(assistantMessage);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

        com.travel.ai.dto.AiChatResponse response = aiRecommendationService.chat(convId, q2);

        assertNotNull(response);
        assertEquals(convId, response.getConversationId());
        assertEquals("Dạ tour có đón tận nơi tại khách sạn ạ!", response.getReply());

        // Kiểm tra đã lưu tin nhắn user và ai vào Redis
        verify(conversationMemoryService, times(1)).saveMessage(eq(convId), eq("user"), eq(q2));
        verify(conversationMemoryService, times(1)).saveMessage(eq(convId), eq("assistant"), eq("Dạ tour có đón tận nơi tại khách sạn ạ!"));
    }

    @Test
    @DisplayName("Test 3.5: Chatbot đa lượt - Câu trả lời sở thích dù chunks rỗng vẫn KHÔNG bị chặn guardrail mà gửi vào LLM")
    void chat_ContextualReplyWithEmptyChunks_DoesNotBlockAndCallsLLM() {
        String convId = "conv-xyz";
        String userPreference = "tôi yêu thích sự lung linh và biển";

        com.travel.ai.dto.ChatMessageDto priorAiMsg = com.travel.ai.dto.ChatMessageDto.builder()
                .role("assistant")
                .content("Quý khách thích lung linh phố cổ hay biển trong xanh ạ?")
                .timestamp(2000L).build();

        when(conversationMemoryService.getRecentMessages(eq(convId), eq(6)))
                .thenReturn(List.of(priorAiMsg));

        // CSDL không tìm thấy chunk nào khớp với câu nói sở thích
        when(tourRetrievalService.retrieveKnowledgeChunks(anyString(), eq(4)))
                .thenReturn(Collections.emptyList());

        when(promptEngineeringService.buildChatPrompt(eq(userPreference), anyList(), anyList()))
                .thenReturn("Chat Prompt with History and Empty Chunks");

        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage assistantMessage = new AssistantMessage("Vậy Tour Đà Nẵng - Hội An là lựa chọn tuyệt vời nhất cho bạn!");
        when(mockGeneration.getOutput()).thenReturn(assistantMessage);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);
        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

        com.travel.ai.dto.AiChatResponse response = aiRecommendationService.chat(convId, userPreference);

        assertNotNull(response);
        assertEquals(convId, response.getConversationId());
        // Khẳng định KHÔNG bị trả về thông báo từ chối phạm vi mặc định
        assertFalse(response.getReply().contains("ngoài phạm vi") || response.getReply().contains("Dạ xin lỗi bạn, tôi là Trợ lý AI chuyên về Du lịch"));
        assertEquals("Vậy Tour Đà Nẵng - Hội An là lựa chọn tuyệt vời nhất cho bạn!", response.getReply());

        // Kiểm tra đã gọi LLM và lưu vào Memory
        verify(chatModel, times(1)).call(any(Prompt.class));
        verify(conversationMemoryService, times(1)).saveMessage(eq(convId), eq("user"), eq(userPreference));
    }
}
