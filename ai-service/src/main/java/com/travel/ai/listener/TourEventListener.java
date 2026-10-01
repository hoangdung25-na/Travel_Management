package com.travel.ai.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.service.TourIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka Listener tiêu thụ các sự kiện Tour từ tour-service để tự động Indexing dữ liệu RAG
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TourEventListener {

    private final TourIngestionService tourIngestionService;
    private final ObjectMapper objectMapper;

    /**
     * Tiêu thụ thông tin TourUpdatedEvent từ Kafka topic 'tour-events'
     */
    @KafkaListener(topics = {"tour-events", "tour.events"}, groupId = "ai-service-group")
    public void handleTourUpdatedEvent(String messagePayload) {
        log.info("Nhận tin nhắn Kafka từ topic 'tour-events': {}", messagePayload);

        try {
            TourUpdatedEvent event = objectMapper.readValue(messagePayload, TourUpdatedEvent.class);
            if (event.getTourId() == null) {
                log.warn("Bản tin TourUpdatedEvent không có tourId, bỏ qua xử lý.");
                return;
            }

            tourIngestionService.processTourUpdate(event);
            log.info("Xử lý thành công RAG Indexing cho Tour ID: {}", event.getTourId());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý bản tin TourUpdatedEvent từ Kafka: {}", e.getMessage(), e);
        }
    }
}
