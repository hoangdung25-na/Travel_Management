package com.travel.tour.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.tour.dto.event.BookingCreatedEvent;
import com.travel.tour.dto.event.PaymentFailedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TourEventConsumer {

    private final SeatAllocationDomainService seatAllocationDomainService;
    private final ObjectMapper objectMapper;

    public TourEventConsumer(SeatAllocationDomainService seatAllocationDomainService, ObjectMapper objectMapper) {
        this.seatAllocationDomainService = seatAllocationDomainService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking-created", groupId = "tour-service-group")
    public void consumeBookingCreated(String payload) {
        try {
            // NOTE: Should check Idempotency Key in Redis here in production
            BookingCreatedEvent event = objectMapper.readValue(payload, BookingCreatedEvent.class);
            seatAllocationDomainService.reserveSeats(event);
        } catch (Exception e) {
            // Handle error, e.g. logging or DLQ
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "tour-service-group")
    public void consumePaymentFailed(String payload) {
        try {
            // NOTE: Should check Idempotency Key in Redis here in production
            PaymentFailedEvent event = objectMapper.readValue(payload, PaymentFailedEvent.class);
            seatAllocationDomainService.releaseSeats(event);
        } catch (Exception e) {
            // Handle error
        }
    }
}
