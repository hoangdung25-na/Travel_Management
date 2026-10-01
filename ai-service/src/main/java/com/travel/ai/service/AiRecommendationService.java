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
}
