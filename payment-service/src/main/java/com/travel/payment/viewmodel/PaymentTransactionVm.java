package com.travel.payment.viewmodel;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ViewModel phản hồi nhật ký lịch sử nỗ lực giao dịch thanh toán
 */
@Builder
public record PaymentTransactionVm(
    UUID id,
    String txnRef,
    String gatewayResponseCode,
    BigDecimal amount,
    OffsetDateTime createdAt
) {
}
