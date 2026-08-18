package com.travel.booking.dto.event;

import java.util.UUID;

public record TourSeatsReservationFailedEvent(
        UUID bookingId,
        UUID scheduleId,
        int requestedSeats,
        String reason
) {}
