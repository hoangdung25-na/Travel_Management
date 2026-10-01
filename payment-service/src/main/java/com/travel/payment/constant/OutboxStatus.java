package com.travel.payment.constant;

/**
 * Trạng thái của bản tin trong bảng Outbox Events (Transactional Outbox Pattern)
 */
public enum OutboxStatus {
    PENDING,    // Bản tin mới ghi vào DB, chưa phát sang Kafka
    PROCESSED,  // Đã phát thành công sang Kafka
    FAILED      // Phát thất bại sau nhiều lần retry
}
