package com.travel.payment.viewmodel;

import com.travel.payment.constant.PaymentMethod;
import com.travel.payment.constant.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ViewModel phản hồi thông tin chi tiết thanh toán của đơn hàng
 */
@Builder
public record PaymentVm(
    UUID id,
    UUID bookingId,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    PaymentStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
