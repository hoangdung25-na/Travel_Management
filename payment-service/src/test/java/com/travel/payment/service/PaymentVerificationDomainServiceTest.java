package com.travel.payment.service;

import com.travel.payment.helper.VnPayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentVerificationDomainServiceTest {

    private PaymentVerificationDomainService verificationDomainService;
    private final String secretKey = "TRAVELSECRETKEY888888888888888";

    @BeforeEach
    void setUp() {
        verificationDomainService = new PaymentVerificationDomainService();
    }

    @Test
    @DisplayName("verifyVnPaySignature - Thành công khi chữ ký HASH đúng")
    void verifyVnPaySignature_Success() {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("vnp_Amount", "100000000");
        queryParams.put("vnp_Command", "pay");
        queryParams.put("vnp_CreateDate", "20260804223000");
        queryParams.put("vnp_CurrCode", "VND");
        queryParams.put("vnp_IpAddr", "127.0.0.1");
        queryParams.put("vnp_Locale", "vn");
        queryParams.put("vnp_Merchant", "TRAVELAPP");
        queryParams.put("vnp_OrderInfo", "Thanh toan don hang");
        queryParams.put("vnp_ResponseCode", "00");
        queryParams.put("vnp_TmnCode", "TRAVEL01");
        queryParams.put("vnp_TxnRef", "VNP123456");
        queryParams.put("vnp_Version", "2.1.0");

        // Calculate valid hash for testing
        String expectedHash = VnPayUtils.hashAllFields(queryParams, secretKey);
        queryParams.put("vnp_SecureHash", expectedHash);

        // When
        boolean isValid = verificationDomainService.verifyVnPaySignature(queryParams, secretKey);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("verifyVnPaySignature - Thất bại khi chữ ký HASH bị giả mạo")
    void verifyVnPaySignature_InvalidHash_ReturnsFalse() {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("vnp_Amount", "100000000");
        queryParams.put("vnp_TxnRef", "VNP123456");
        queryParams.put("vnp_SecureHash", "INVALIDHASH123456789");

        // When
        boolean isValid = verificationDomainService.verifyVnPaySignature(queryParams, secretKey);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("verifyVnPaySignature - Thất bại khi thiếu trường vnp_SecureHash")
    void verifyVnPaySignature_MissingHashParam_ReturnsFalse() {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("vnp_Amount", "100000000");

        // When
        boolean isValid = verificationDomainService.verifyVnPaySignature(queryParams, secretKey);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("verifyPaymentAmount - Đúng khi hai số tiền khớp nhau")
    void verifyPaymentAmount_MatchingAmount_ReturnsTrue() {
        // Given
        BigDecimal expected = new BigDecimal("1000000.00");
        BigDecimal actual = new BigDecimal("1000000.00");

        // When
        boolean isValid = verificationDomainService.verifyPaymentAmount(expected, actual);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("verifyPaymentAmount - Sai khi hai số tiền lệch nhau")
    void verifyPaymentAmount_MismatchAmount_ReturnsFalse() {
        // Given
        BigDecimal expected = new BigDecimal("1000000.00");
        BigDecimal actual = new BigDecimal("500000.00");

        // When
        boolean isValid = verificationDomainService.verifyPaymentAmount(expected, actual);

        // Then
        assertThat(isValid).isFalse();
    }
}
