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
}
