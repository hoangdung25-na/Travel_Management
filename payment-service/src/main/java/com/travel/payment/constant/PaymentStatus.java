package com.travel.payment.constant;

/**
 * Trạng thái giao dịch thanh toán
 */
public enum PaymentStatus {
    PENDING,    // Đang chờ khách hàng thanh toán trên cổng VNPAY/Stripe
    SUCCESS,    // Webhook xác nhận thanh toán thành công
    FAILED,     // Thanh toán thất bại hoặc quá thời gian chờ (Timeout)
    REFUNDED    // Đã hoàn tiền cho khách hàng
}
