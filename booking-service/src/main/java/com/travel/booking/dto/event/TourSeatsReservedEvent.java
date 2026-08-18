package com.travel.booking.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record TourSeatsReservedEvent(
        UUID bookingId,
        UUID scheduleId,
        int requestedSeats,
        BigDecimal totalPrice
) {}
