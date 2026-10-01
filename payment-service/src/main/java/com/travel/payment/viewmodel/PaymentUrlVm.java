package com.travel.payment.viewmodel;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * ViewModel phản hồi sau khi tạo thành công liên kết thanh toán VNPAY/Stripe
 */
@Builder
public record PaymentUrlVm(
    UUID paymentId,
    UUID bookingId,
    BigDecimal amount,
    String paymentUrl
) {
}
