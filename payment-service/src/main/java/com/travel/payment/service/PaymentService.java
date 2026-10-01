package com.travel.payment.service;

import com.travel.payment.dto.CreatePaymentUrlRequest;
import com.travel.payment.dto.RefundPaymentRequest;
import com.travel.payment.viewmodel.PaymentUrlVm;
import com.travel.payment.viewmodel.PaymentVm;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

/**
 * Interface Service xử lý các nghiệp vụ thanh toán
 */
public interface PaymentService {

    /**
     * Khởi tạo liên kết thanh toán VNPAY/Stripe cho đơn hàng
     */
    PaymentUrlVm createPaymentUrl(CreatePaymentUrlRequest request, HttpServletRequest httpRequest);

    /**
     * Tiếp nhận và xử lý Webhook Callback IPN từ Cổng thanh toán VNPAY
     */
    Map<String, String> processVnPayCallback(Map<String, String> queryParams);

    /**
     * Lấy thông tin thanh toán theo mã đơn hàng
     */
    PaymentVm getPaymentByBookingId(UUID bookingId);

    /**
     * Xử lý yêu cầu hoàn tiền giao dịch
     */
    PaymentVm refundPayment(UUID paymentId, RefundPaymentRequest request);
}
