package com.travel.payment.service;

import com.travel.payment.helper.VnPayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain Service: Kiểm tra tính toàn vẹn chữ ký số và đối soát số tiền giao dịch
 */
@Service
@Slf4j
public class PaymentVerificationDomainService {

    /**
     * Xác thực chữ ký số HASH (vnp_SecureHash) từ Webhook Callback VNPAY
     */
    public boolean verifyVnPaySignature(Map<String, String> queryParams, String secretKey) {
        if (queryParams == null || !queryParams.containsKey("vnp_SecureHash")) {
            log.warn("Lỗi xác thực VNPAY: Thiếu vnp_SecureHash trong Query Parameters");
            return false;
        }

        String receivedHash = queryParams.get("vnp_SecureHash");

        // Clone map và loại bỏ vnp_SecureHash, vnp_SecureHashType để tính lại checksum
        Map<String, String> fields = new HashMap<>(queryParams);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String calculatedHash = VnPayUtils.hashAllFields(fields, secretKey);

        boolean isValid = calculatedHash.equalsIgnoreCase(receivedHash);
        if (!isValid) {
            log.warn("Lỗi xác thực chữ ký VNPAY: Checksum không khớp. Received={}, Calculated={}", 
                    receivedHash, calculatedHash);
        }
        return isValid;
    }

    /**
     * Đối soát số tiền thanh toán nhận từ cổng thanh toán với số tiền thực tế của đơn hàng
     */
    public boolean verifyPaymentAmount(BigDecimal expectedAmount, BigDecimal actualAmount) {
        if (expectedAmount == null || actualAmount == null) {
            return false;
        }
        // So sánh bằng BigDecimal (compareTo == 0 bỏ qua sự khác biệt scale .00)
        return expectedAmount.compareTo(actualAmount) == 0;
    }
}
