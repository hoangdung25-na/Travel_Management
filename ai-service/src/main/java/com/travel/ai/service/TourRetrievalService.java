package com.travel.ai.service;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;

import java.util.List;

/**
 * Service thực hiện Truy vấn Ngữ nghĩa & Lọc tương đồng (Pipeline 2: Retrieval & Hybrid Search)
 */
public interface TourRetrievalService {

    /**
     * Tìm kiếm Top K đoạn văn bản (Chunks) liên quan nhất dựa trên Cosine Similarity và Điều kiện lọc cứng
     */
    List<RetrievedChunkDto> retrieveRelevantChunks(AiQueryRequest request);

    /**
     * Tìm kiếm Top K đoạn tri thức (bao gồm cả Tour và Chính sách công ty) phục vụ Chatbot RAG
     */
    List<RetrievedChunkDto> retrieveKnowledgeChunks(String queryText, int limit);
}
