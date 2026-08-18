package com.travel.booking.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.constant.OutboxStatus;
import com.travel.booking.dto.event.PaymentFailedEvent;
import com.travel.booking.dto.event.PaymentProcessedEvent;
import com.travel.booking.dto.event.TourSeatsReservationFailedEvent;
import com.travel.booking.dto.event.TourSeatsReservedEvent;
import com.travel.booking.entity.Booking;
import com.travel.booking.entity.OutboxEventEntity;
import com.travel.booking.repository.BookingRepository;
import com.travel.booking.repository.OutboxEventRepository;
import com.travel.booking.service.BookingSagaStateDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final BookingRepository bookingRepository;
    private final BookingSagaStateDomainService bookingSagaStateDomainService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"tour-events", "tour.events"}, groupId = "booking-service-group")
    @Transactional
    public void handleTourEvents(String messagePayload) {
        log.info("Nhận được tin nhắn từ Kafka topic tour-events: {}", messagePayload);
        try {
            JsonNode rootNode = objectMapper.readTree(messagePayload);
            String eventType = rootNode.has("eventType") ? rootNode.get("eventType").asText() : "";

            if ("TourSeatsReservedEvent".equals(eventType) || messagePayload.contains("requestedSeats") && !messagePayload.contains("reason")) {
                TourSeatsReservedEvent event = objectMapper.readValue(messagePayload, TourSeatsReservedEvent.class);
                processTourSeatsReserved(event);
            } else if ("TourSeatsReservationFailedEvent".equals(eventType) || messagePayload.contains("reason")) {
                TourSeatsReservationFailedEvent event = objectMapper.readValue(messagePayload, TourSeatsReservationFailedEvent.class);
                processTourSeatsReservationFailed(event);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn tour-events: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = {"payment-events", "payment.events"}, groupId = "booking-service-group")
    @Transactional
    public void handlePaymentEvents(String messagePayload) {
        log.info("Nhận được tin nhắn từ Kafka topic payment-events: {}", messagePayload);
        try {
            JsonNode rootNode = objectMapper.readTree(messagePayload);
            String eventType = rootNode.has("eventType") ? rootNode.get("eventType").asText() : "";

            if ("PaymentProcessedEvent".equals(eventType) || messagePayload.contains("paymentId")) {
                PaymentProcessedEvent event = objectMapper.readValue(messagePayload, PaymentProcessedEvent.class);
                processPaymentProcessed(event);
            } else if ("PaymentFailedEvent".equals(eventType) || messagePayload.contains("reason")) {
                PaymentFailedEvent event = objectMapper.readValue(messagePayload, PaymentFailedEvent.class);
                processPaymentFailed(event);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn payment-events: {}", e.getMessage(), e);
        }
    }

    private void processTourSeatsReserved(TourSeatsReservedEvent event) {
        Optional<Booking> optionalBooking = bookingRepository.findById(event.bookingId());
        if (optionalBooking.isEmpty()) {
            log.warn("Không tìm thấy Booking ID: {} để chuyển trạng thái PAYMENT_PENDING", event.bookingId());
            return;
        }

        Booking booking = optionalBooking.get();
        bookingSagaStateDomainService.validateAndTransitionToSeatsReserved(booking);
        bookingRepository.save(booking);
        log.info("Saga Update: Tour-service giữ chỗ thành công cho Booking ID: {}. Đã chuyển trạng thái sang PAYMENT_PENDING", booking.getId());
    }

    private void processTourSeatsReservationFailed(TourSeatsReservationFailedEvent event) {
        Optional<Booking> optionalBooking = bookingRepository.findById(event.bookingId());
        if (optionalBooking.isEmpty()) {
            log.warn("Không tìm thấy Booking ID: {} để chuyển trạng thái CANCELLED", event.bookingId());
            return;
        }

        Booking booking = optionalBooking.get();
        bookingSagaStateDomainService.validateAndTransitionToCancelled(booking);
        bookingRepository.save(booking);
        log.warn("Saga Update: Tour-service giữ chỗ thất bại cho Booking ID: {}. Lý do: {}. Đã chuyển đơn hàng sang CANCELLED", booking.getId(), event.reason());
    }

    private void processPaymentProcessed(PaymentProcessedEvent event) {
        Optional<Booking> optionalBooking = bookingRepository.findById(event.bookingId());
        if (optionalBooking.isEmpty()) {
            log.warn("Không tìm thấy Booking ID: {} để xác nhận CONFIRMED", event.bookingId());
            return;
        }

        Booking booking = optionalBooking.get();
        bookingSagaStateDomainService.validateAndTransitionToConfirmed(booking);
        bookingRepository.save(booking);
        log.info("Saga Complete: Thanh toán thành công cho Booking ID: {}. Đã xác nhận đơn hàng CONFIRMED!", booking.getId());
    }

    private void processPaymentFailed(PaymentFailedEvent event) {
        Optional<Booking> optionalBooking = bookingRepository.findById(event.bookingId());
        if (optionalBooking.isEmpty()) {
            log.warn("Không tìm thấy Booking ID: {} để hủy đơn", event.bookingId());
            return;
        }

        Booking booking = optionalBooking.get();
        bookingSagaStateDomainService.validateAndTransitionToCancelled(booking);
        bookingRepository.save(booking);

        int seatCount = booking.getPassengers() != null ? booking.getPassengers().size() : 1;

        // Ghi bản tin Outbox BookingCancelledEvent để tour-service nhả lại số chỗ đã giữ
        saveOutbox("Booking", booking.getId().toString(), "BookingCancelledEvent", Map.of(
                "bookingId", booking.getId(),
                "scheduleId", booking.getTourScheduleId(),
                "requestedSeats", seatCount,
                "reason", "Thanh toán thất bại: " + event.reason()
        ));

        log.warn("Saga Compensate: Thanh toán thất bại cho Booking ID: {}. Lý do: {}. Đã chuyển đơn hàng sang CANCELLED và ghi Outbox nhả chỗ", booking.getId(), event.reason());
    }

    private void saveOutbox(String aggregateType, String aggregateId, String eventType, Object payloadObj) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadObj);
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Lỗi đóng gói JSON Outbox Event: {}", e.getMessage(), e);
        }
    }
}
