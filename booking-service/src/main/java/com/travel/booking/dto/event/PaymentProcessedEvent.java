package com.travel.booking.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentProcessedEvent(
        UUID bookingId,
        UUID paymentId,
        BigDecimal amount,
        String paymentMethod,
        String status
) {}
