package com.travel.payment.dto.event;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event Payload bắn ra Kafka khi thanh toán thành công
 */
@Builder
public record PaymentProcessedEvent(
    UUID bookingId,
    UUID paymentId,
    BigDecimal amount,
    String paymentMethod,
    String status
) {
}
