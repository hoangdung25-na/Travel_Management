package com.travel.ai.service;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.viewmodel.TripRecommendationVm;

/**
 * Service tổng hợp toàn bộ luồng RAG AI (Cache -> Retrieval -> Generation)
 */
public interface AiRecommendationService {

    /**
     * Tư vấn Tour du lịch thông minh và trả về bài tư vấn cấu trúc JSON chuẩn TripRecommendationVm
     */
    TripRecommendationVm getRecommendation(AiQueryRequest request);

    /**
     * Trò chuyện và phản hồi trực tiếp với người dùng qua AI Chatbot 24/7 (tương thích cũ)
     */
    String chat(String promptText);

    /**
     * Trò chuyện trực tiếp hỗ trợ lưu phiên và ngữ cảnh hội thoại đa lượt (Multi-turn Context)
     */
    com.travel.ai.dto.AiChatResponse chat(String conversationId, String promptText);
}
