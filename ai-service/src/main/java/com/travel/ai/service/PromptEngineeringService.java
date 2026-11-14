package com.travel.ai.service;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;

import java.util.List;

/**
 * Service dựng Prompt Template chống ảo giác (Anti-Hallucination Prompt Assembly)
 */
public interface PromptEngineeringService {

    /**
     * Lắp ráp System Prompt từ danh sách ngữ cảnh Chunks trích xuất từ pgvector và câu hỏi người dùng
     */
    String buildPrompt(AiQueryRequest request, List<RetrievedChunkDto> chunks, String outputFormatInstructions);

    /**
     * Lắp ráp Chat Prompt thông minh kết hợp tri thức Chunks (Tour + Policy) cho Chatbot 24/7
     */
    String buildChatPrompt(String userQuery, List<RetrievedChunkDto> chunks);

    /**
     * Lắp ráp Chat Prompt thông minh kết hợp tri thức Chunks và lịch sử hội thoại gần nhất (Multi-turn Context)
     */
    String buildChatPrompt(String userQuery, List<RetrievedChunkDto> chunks, List<com.travel.ai.dto.ChatMessageDto> history);
}
