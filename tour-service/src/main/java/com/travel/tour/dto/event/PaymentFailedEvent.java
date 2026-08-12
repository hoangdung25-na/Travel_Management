package com.travel.tour.dto.event;

import java.util.UUID;

public class PaymentFailedEvent {
    private UUID eventId;
    private UUID bookingId;
    private UUID tourScheduleId;
    private Integer requestedSeats;

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getBookingId() { return bookingId; }
    public void setBookingId(UUID bookingId) { this.bookingId = bookingId; }
    public UUID getTourScheduleId() { return tourScheduleId; }
    public void setTourScheduleId(UUID tourScheduleId) { this.tourScheduleId = tourScheduleId; }
    public Integer getRequestedSeats() { return requestedSeats; }
    public void setRequestedSeats(Integer requestedSeats) { this.requestedSeats = requestedSeats; }
}
