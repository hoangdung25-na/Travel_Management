package com.travel.booking.publisher;

import com.travel.booking.constant.OutboxStatus;
import com.travel.booking.entity.OutboxEventEntity;
import com.travel.booking.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String BOOKING_EVENTS_TOPIC = "booking-events";
    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishOutboxEvents() {
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, pageable);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Tìm thấy {} bản tin Outbox PENDING cần đẩy sang Kafka topic '{}'", pendingEvents.size(), BOOKING_EVENTS_TOPIC);

        for (OutboxEventEntity event : pendingEvents) {
            try {
                kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Đã gửi thành công Outbox Event ID: {} (Type: {}) sang Kafka", event.getId(), event.getEventType());
                            } else {
                                log.error("Lỗi khi gửi Outbox Event ID: {} sang Kafka: {}", event.getId(), ex.getMessage());
                            }
                        });

                event.setStatus(OutboxStatus.PROCESSED);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Lỗi khi bắn event ID {}: {}", event.getId(), e.getMessage(), e);
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}
