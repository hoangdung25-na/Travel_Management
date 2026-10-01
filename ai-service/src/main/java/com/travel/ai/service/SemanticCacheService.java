package com.travel.ai.service;

import java.util.Optional;

/**
 * Service quản lý Semantic Caching trên Redis giúp phản hồi ngay lập tức cho các câu hỏi trùng lặp
 */
public interface SemanticCacheService {

    /**
     * Kiểm tra xem câu hỏi đã có kết quả cache trong Redis chưa
     */
    Optional<String> getCachedResponse(String queryText);

    /**
     * Lưu câu hỏi và câu trả lời AI vào Redis với thời gian sống TTL mặc định
     */
    void cacheResponse(String queryText, String responseJson);
}
