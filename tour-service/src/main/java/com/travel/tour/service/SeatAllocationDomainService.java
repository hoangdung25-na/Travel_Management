package com.travel.tour.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.tour.dto.event.BookingCreatedEvent;
import com.travel.tour.dto.event.PaymentFailedEvent;
import com.travel.tour.entity.OutboxEvent;
import com.travel.tour.entity.TourSchedule;
import com.travel.tour.exception.NotFoundException;
import com.travel.tour.repository.OutboxEventRepository;
import com.travel.tour.repository.TourScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatAllocationDomainService {

    private final TourScheduleRepository scheduleRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public SeatAllocationDomainService(TourScheduleRepository scheduleRepository,
                                       OutboxEventRepository outboxEventRepository,
                                       ObjectMapper objectMapper) {
        this.scheduleRepository = scheduleRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void reserveSeats(BookingCreatedEvent event) {
        try {
            TourSchedule schedule = scheduleRepository.findById(event.getTourScheduleId())
                    .orElseThrow(() -> new NotFoundException("Schedule not found"));

            if (schedule.getAvailableSeats() >= event.getRequestedSeats()) {
                schedule.setAvailableSeats(schedule.getAvailableSeats() - event.getRequestedSeats());
                scheduleRepository.save(schedule);
                
                // Write Success Event
                saveOutboxEvent(event.getBookingId().toString(), "TourSeatsReservedEvent", "{\"eventId\":\"" + event.getEventId() + "\",\"bookingId\":\"" + event.getBookingId() + "\"}");
            } else {
                // Not enough seats
                saveOutboxEvent(event.getBookingId().toString(), "TourSeatsReservationFailedEvent", "{\"eventId\":\"" + event.getEventId() + "\",\"bookingId\":\"" + event.getBookingId() + "\",\"reason\":\"Not enough seats\"}");
            }
        } catch (Exception e) {
            saveOutboxEvent(event.getBookingId().toString(), "TourSeatsReservationFailedEvent", "{\"eventId\":\"" + event.getEventId() + "\",\"bookingId\":\"" + event.getBookingId() + "\",\"reason\":\"" + e.getMessage() + "\"}");
        }
    }

    @Transactional
    public void releaseSeats(PaymentFailedEvent event) {
        scheduleRepository.findById(event.getTourScheduleId()).ifPresent(schedule -> {
            schedule.setAvailableSeats(schedule.getAvailableSeats() + event.getRequestedSeats());
            scheduleRepository.save(schedule);
            
            saveOutboxEvent(event.getBookingId().toString(), "TourSeatsReleasedEvent", "{\"eventId\":\"" + event.getEventId() + "\",\"bookingId\":\"" + event.getBookingId() + "\"}");
        });
    }

    private void saveOutboxEvent(String aggregateId, String eventType, String payload) {
        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateType("TOUR");
        outbox.setAggregateId(aggregateId);
        outbox.setEventType(eventType);
        outbox.setPayload(payload);
        outbox.setStatus("PENDING");
        outboxEventRepository.save(outbox);
    }
}
