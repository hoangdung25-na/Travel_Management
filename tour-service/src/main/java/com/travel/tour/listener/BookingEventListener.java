package com.travel.tour.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.exception.BusinessException;
import com.travel.tour.constant.OutboxStatus;
import com.travel.tour.dto.event.BookingCreatedEvent;
import com.travel.tour.dto.event.TourSeatsReservationFailedEvent;
import com.travel.tour.dto.event.TourSeatsReservedEvent;
import com.travel.tour.entity.OutboxEventEntity;
import com.travel.tour.repository.OutboxEventRepository;
import com.travel.tour.service.SeatAllocationDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final SeatAllocationDomainService seatAllocationDomainService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "booking.events", groupId = "tour-group")
    public void handleBookingCreatedEvent(String messagePayload) {
        log.info("Nhận được tin nhắn từ Kafka topic 'booking.events': {}", messagePayload);

        BookingCreatedEvent event;
        try {
            event = objectMapper.readValue(messagePayload, BookingCreatedEvent.class);
        } catch (Exception e) {
            log.error("Lỗi giải mã bản tin BookingCreatedEvent: {}", e.getMessage(), e);
            return;
        }

        try {
            // 1. Thực hiện trừ/giữ chỗ nguyên tố dưới CSDL (có Pessimistic Lock)
            seatAllocationDomainService.reserveSeats(event.getScheduleId(), event.getRequestedSeats());

            // 2. Nếu giữ chỗ thành công -> Ghi TourSeatsReservedEvent vào outbox
            TourSeatsReservedEvent reservedEvent = TourSeatsReservedEvent.builder()
                    .bookingId(event.getBookingId())
                    .scheduleId(event.getScheduleId())
                    .requestedSeats(event.getRequestedSeats())
                    .totalPrice(event.getTotalPrice())
                    .build();

            saveOutbox("TourSchedule", event.getScheduleId().toString(), "TourSeatsReservedEvent", reservedEvent);
            log.info("Giữ chỗ thành công cho Booking ID: {}. Đã ghi bản tin TourSeatsReservedEvent vào Outbox.", event.getBookingId());

        } catch (BusinessException ex) {
            log.warn("Giữ chỗ thất bại cho Booking ID: {}. Lý do: {}", event.getBookingId(), ex.getMessage());

            // 3. Nếu hết chỗ hoặc lỗi -> Ghi TourSeatsReservationFailedEvent vào outbox
            TourSeatsReservationFailedEvent failedEvent = TourSeatsReservationFailedEvent.builder()
                    .bookingId(event.getBookingId())
                    .scheduleId(event.getScheduleId())
                    .requestedSeats(event.getRequestedSeats())
                    .reason(ex.getMessage())
                    .build();

            saveOutbox("TourSchedule", event.getScheduleId().toString(), "TourSeatsReservationFailedEvent", failedEvent);
        } catch (Exception ex) {
            log.error("Lỗi không xác định khi xử lý Booking ID {}: {}", event.getBookingId(), ex.getMessage(), ex);

            TourSeatsReservationFailedEvent failedEvent = TourSeatsReservationFailedEvent.builder()
                    .bookingId(event.getBookingId())
                    .scheduleId(event.getScheduleId())
                    .requestedSeats(event.getRequestedSeats())
                    .reason("Lỗi hệ thống khi giữ chỗ: " + ex.getMessage())
                    .build();

            saveOutbox("TourSchedule", event.getScheduleId().toString(), "TourSeatsReservationFailedEvent", failedEvent);
        }
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
