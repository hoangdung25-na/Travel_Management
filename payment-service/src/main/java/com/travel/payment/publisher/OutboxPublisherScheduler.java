package com.travel.payment.publisher;

import com.travel.payment.constant.OutboxStatus;
import com.travel.payment.entity.OutboxEventEntity;
import com.travel.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Publisher quét bảng outbox_events và đẩy sự kiện sang Kafka Topic payment-events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    private static final int BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishOutboxEvents() {
        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, pageable);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Outbox Poller: Tìm thấy {} bản tin PENDING cần đẩy sang Kafka topic '{}'", pendingEvents.size(), PAYMENT_EVENTS_TOPIC);

        for (OutboxEventEntity event : pendingEvents) {
            try {
                kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Đã gửi thành công Outbox Event ID: {} (Type: {}) sang Kafka", event.getId(), event.getEventType());
                            } else {
                                log.error("Lỗi khi gửi Outbox Event ID: {} sang Kafka: {}", event.getId(), ex.getMessage());
                            }
                        });

                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Lỗi khi phát tin nhắn Outbox Event ID {}: {}", event.getId(), e.getMessage(), e);
                event.setStatus(OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}
