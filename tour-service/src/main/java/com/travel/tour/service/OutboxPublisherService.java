package com.travel.tour.service;

import com.travel.tour.entity.OutboxEvent;
import com.travel.tour.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherService(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        
        for (OutboxEvent event : pendingEvents) {
            String topic = getTopic(event.getEventType());
            if (topic != null) {
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
            }
            event.setStatus("PROCESSED");
        }
        outboxEventRepository.saveAll(pendingEvents);
    }

    private String getTopic(String eventType) {
        return switch (eventType) {
            case "TourSeatsReservedEvent", "TourSeatsReservationFailedEvent", "TourSeatsReleasedEvent" -> "tour-events";
            default -> null;
        };
    }
}
