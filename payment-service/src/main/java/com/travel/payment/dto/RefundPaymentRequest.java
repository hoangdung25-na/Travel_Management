package com.travel.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request DTO yêu cầu hoàn tiền đơn hàng
 */
@Builder
public record RefundPaymentRequest(
    @NotBlank(message = "Lý do hoàn tiền không được để trống")
    @Size(max = 255, message = "Lý do hoàn tiền không vượt quá 255 ký tự")
    String reason
) {
}
