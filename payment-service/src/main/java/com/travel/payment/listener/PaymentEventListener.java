package com.travel.payment.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka Event Listener tiêu thụ sự kiện từ các microservices khác trong luồng Saga
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"tour-events", "tour.events"}, groupId = "payment-service-group")
    @Transactional
    public void handleTourEvents(String messagePayload) {
        log.info("Payment-service nhận được sự kiện từ Kafka topic tour-events: {}", messagePayload);
        try {
            JsonNode rootNode = objectMapper.readTree(messagePayload);
            String eventType = rootNode.has("eventType") ? rootNode.get("eventType").asText() : "";

            if ("TourSeatsReservedEvent".equals(eventType) || messagePayload.contains("requestedSeats")) {
                log.info("Saga Event Notification: Tour-service đã giữ chỗ thành công. Sẵn sàng tiếp nhận thanh toán cho đơn hàng!");
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện tour-events tại payment-service: {}", e.getMessage(), e);
        }
    }
}
