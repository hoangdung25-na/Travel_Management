package com.travel.ai.service;

import com.travel.ai.dto.event.TourUpdatedEvent;

/**
 * Service xử lý nạp và Indexing dữ liệu Tour vào Vector Database (Pipeline 1)
 */
public interface TourIngestionService {

    /**
     * Tiếp nhận Event Tour từ Kafka, phân đoạn văn bản, gọi Embedding Model và lưu vào pgvector
     */
    void processTourUpdate(TourUpdatedEvent event);
}
