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
}
