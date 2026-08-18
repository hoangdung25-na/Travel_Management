package com.travel.booking.viewmodel;

import com.travel.booking.constant.BookingStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record BookingVm(
        UUID bookingId,
        String bookingCode,
        UUID userId,
        UUID tourScheduleId,
        BigDecimal totalAmount,
        BookingStatus status,
        OffsetDateTime createdAt
) {
}
