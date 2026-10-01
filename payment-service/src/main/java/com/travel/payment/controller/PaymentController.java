package com.travel.payment.controller;

import com.travel.common.core.dto.ApiResponse;
import com.travel.payment.dto.CreatePaymentUrlRequest;
import com.travel.payment.dto.RefundPaymentRequest;
import com.travel.payment.service.PaymentService;
import com.travel.payment.viewmodel.PaymentUrlVm;
import com.travel.payment.viewmodel.PaymentVm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller cho Payment Service
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * API Khởi tạo liên kết thanh toán VNPAY/Stripe
     */
    @PostMapping("/create-url")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<PaymentUrlVm> createPaymentUrl(
            @Valid @RequestBody CreatePaymentUrlRequest request,
            HttpServletRequest httpRequest) {
        log.info("REST request tạo liên kết thanh toán cho bookingId: {}", request.bookingId());
        PaymentUrlVm paymentUrlVm = paymentService.createPaymentUrl(request, httpRequest);
        return ApiResponse.ok(paymentUrlVm, "Khởi tạo liên kết thanh toán thành công");
    }

    /**
     * API Webhook Callback IPN nhận từ VNPAY (HTTP GET)
     */
    @GetMapping("/vnpay-callback")
    public Map<String, String> processVnPayCallbackGet(@RequestParam Map<String, String> queryParams) {
        log.info("REST request IPN Callback (GET) từ VNPAY: {}", queryParams);
        return paymentService.processVnPayCallback(queryParams);
    }

    /**
     * API Webhook Callback IPN nhận từ VNPAY (HTTP POST)
     */
    @PostMapping("/vnpay-callback")
    public Map<String, String> processVnPayCallbackPost(@RequestParam Map<String, String> queryParams) {
        log.info("REST request IPN Callback (POST) từ VNPAY: {}", queryParams);
        return paymentService.processVnPayCallback(queryParams);
    }

    /**
     * API Xem thông tin thanh toán theo Mã đơn hàng (bookingId)
     */
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<PaymentVm> getPaymentByBookingId(@PathVariable UUID bookingId) {
        log.info("REST request lấy thông tin thanh toán cho bookingId: {}", bookingId);
        PaymentVm paymentVm = paymentService.getPaymentByBookingId(bookingId);
        return ApiResponse.ok(paymentVm, "Lấy thông tin thanh toán thành công");
    }

    /**
     * API Hoàn tiền giao dịch (Chỉ dành cho ADMIN)
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentVm> refundPayment(
            @PathVariable UUID id,
            @Valid @RequestBody RefundPaymentRequest request) {
        log.info("REST request hoàn tiền cho paymentId: {}, lý do: {}", id, request.reason());
        PaymentVm paymentVm = paymentService.refundPayment(id, request);
        return ApiResponse.ok(paymentVm, "Xử lý hoàn tiền giao dịch thành công");
    }
}
