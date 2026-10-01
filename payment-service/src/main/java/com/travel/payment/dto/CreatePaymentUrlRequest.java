package com.travel.payment.dto;

import com.travel.payment.constant.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO khởi tạo liên kết thanh toán VNPAY/Stripe/MoMo
 */
@Builder
public record CreatePaymentUrlRequest(
    @NotNull(message = "Mã đơn hàng (bookingId) không được để trống")
    UUID bookingId,

    @NotNull(message = "Phương thức thanh toán không được để trống")
    PaymentMethod paymentMethod,

    BigDecimal amount
) {
}
