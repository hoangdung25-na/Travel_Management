package com.travel.payment.dto.event;

import lombok.Builder;

import java.util.UUID;

/**
 * Event Payload bắn ra Kafka khi thanh toán thất bại hoặc hủy bỏ
 */
@Builder
public record PaymentFailedEvent(
    UUID bookingId,
    UUID paymentId,
    String reason
) {
}
