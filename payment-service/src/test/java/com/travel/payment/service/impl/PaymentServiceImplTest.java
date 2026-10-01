package com.travel.payment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.payment.config.VnPayProperties;
import com.travel.payment.constant.PaymentMethod;
import com.travel.payment.constant.PaymentStatus;
import com.travel.payment.dto.CreatePaymentUrlRequest;
import com.travel.payment.dto.RefundPaymentRequest;
import com.travel.payment.entity.OutboxEventEntity;
import com.travel.payment.entity.Payment;
import com.travel.payment.entity.PaymentTransaction;
import com.travel.payment.mapper.PaymentMapper;
import com.travel.payment.repository.OutboxEventRepository;
import com.travel.payment.repository.PaymentRepository;
import com.travel.payment.repository.PaymentTransactionRepository;
import com.travel.payment.service.PaymentVerificationDomainService;
import com.travel.payment.viewmodel.PaymentUrlVm;
import com.travel.payment.viewmodel.PaymentVm;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private PaymentVerificationDomainService verificationDomainService;

    @Mock
    private VnPayProperties vnPayProperties;

    @Mock
    private PaymentMapper paymentMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID bookingId;
    private UUID paymentId;
    private Payment payment;
    private PaymentTransaction transaction;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        payment = Payment.builder()
                .id(paymentId)
                .bookingId(bookingId)
                .amount(new BigDecimal("1000000.00"))
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        transaction = PaymentTransaction.builder()
                .id(UUID.randomUUID())
                .payment(payment)
                .txnRef("VNP123456789")
                .amount(new BigDecimal("1000000.00"))
                .build();
    }

    @Test
    @DisplayName("createPaymentUrl - Khởi tạo liên kết VNPAY thành công")
    void createPaymentUrl_Success() {
        // Given
        CreatePaymentUrlRequest request = CreatePaymentUrlRequest.builder()
                .bookingId(bookingId)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(vnPayProperties.getVersion()).thenReturn("2.1.0");
        when(vnPayProperties.getCommand()).thenReturn("pay");
        when(vnPayProperties.getTmnCode()).thenReturn("TRAVEL01");
        when(vnPayProperties.getCurrCode()).thenReturn("VND");
        when(vnPayProperties.getLocale()).thenReturn("vn");
        when(vnPayProperties.getReturnUrl()).thenReturn("http://localhost:8080/vnpay-callback");
        when(vnPayProperties.getPayUrl()).thenReturn("https://sandbox.vnpayment.vn/pay");
        when(vnPayProperties.getHashSecret()).thenReturn("SECRETKEY123");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        PaymentUrlVm result = paymentService.createPaymentUrl(request, httpRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.bookingId()).isEqualTo(bookingId);
        assertThat(result.paymentUrl()).contains("https://sandbox.vnpayment.vn/pay");
        assertThat(result.paymentUrl()).contains("vnp_SecureHash=");

        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    @DisplayName("processVnPayCallback - Thành công (vnp_ResponseCode = 00)")
    void processVnPayCallback_Success() {
        // Given
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("vnp_TxnRef", "VNP123456789");
        queryParams.put("vnp_ResponseCode", "00");
        queryParams.put("vnp_SecureHash", "VALIDHASH");

        when(vnPayProperties.getHashSecret()).thenReturn("SECRETKEY123");
        when(verificationDomainService.verifyVnPaySignature(any(), anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTxnRef("VNP123456789")).thenReturn(Optional.of(transaction));

        // When
        Map<String, String> response = paymentService.processVnPayCallback(queryParams);

        // Then
        assertThat(response.get("RspCode")).isEqualTo("00");
        assertThat(response.get("Message")).isEqualTo("Confirm Success");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(outboxEventRepository, times(1)).save(any(OutboxEventEntity.class));
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("processVnPayCallback - Thất bại do chữ ký HASH giả mạo (RspCode = 97)")
    void processVnPayCallback_InvalidSignature_Returns97() {
        // Given
        Map<String, String> queryParams = Map.of("vnp_SecureHash", "INVALID");
        when(vnPayProperties.getHashSecret()).thenReturn("SECRETKEY123");
        when(verificationDomainService.verifyVnPaySignature(any(), anyString())).thenReturn(false);

        // When
        Map<String, String> response = paymentService.processVnPayCallback(queryParams);

        // Then
        assertThat(response.get("RspCode")).isEqualTo("97");
        assertThat(response.get("Message")).isEqualTo("Invalid Checksum");

        verify(paymentRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("processVnPayCallback - Thất bại do không tìm thấy đơn hàng (RspCode = 01)")
    void processVnPayCallback_OrderNotFound_Returns01() {
        // Given
        Map<String, String> queryParams = Map.of("vnp_TxnRef", "VNP_NOT_FOUND", "vnp_SecureHash", "VALID");
        when(vnPayProperties.getHashSecret()).thenReturn("SECRETKEY123");
        when(verificationDomainService.verifyVnPaySignature(any(), anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTxnRef("VNP_NOT_FOUND")).thenReturn(Optional.empty());

        // When
        Map<String, String> response = paymentService.processVnPayCallback(queryParams);

        // Then
        assertThat(response.get("RspCode")).isEqualTo("01");
        assertThat(response.get("Message")).isEqualTo("Order Not Found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("processVnPayCallback - Idempotent: Đơn đã xác nhận thành công trước đó (RspCode = 02)")
    void processVnPayCallback_AlreadyConfirmed_Returns02() {
        // Given
        payment.setStatus(PaymentStatus.SUCCESS);
        Map<String, String> queryParams = Map.of("vnp_TxnRef", "VNP123456789", "vnp_SecureHash", "VALID");

        when(vnPayProperties.getHashSecret()).thenReturn("SECRETKEY123");
        when(verificationDomainService.verifyVnPaySignature(any(), anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTxnRef("VNP123456789")).thenReturn(Optional.of(transaction));

        // When
        Map<String, String> response = paymentService.processVnPayCallback(queryParams);

        // Then
        assertThat(response.get("RspCode")).isEqualTo("02");
        assertThat(response.get("Message")).isEqualTo("Order already confirmed");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("getPaymentByBookingId - Thành công khi tìm thấy thông tin thanh toán")
    void getPaymentByBookingId_Success() {
        // Given
        PaymentVm expectedVm = PaymentVm.builder()
                .id(paymentId)
                .bookingId(bookingId)
                .amount(new BigDecimal("1000000.00"))
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentVm(payment)).thenReturn(expectedVm);

        // When
        PaymentVm result = paymentService.getPaymentByBookingId(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.bookingId()).isEqualTo(bookingId);
    }

    @Test
    @DisplayName("getPaymentByBookingId - Thất bại ném NOT_FOUND khi không tìm thấy đơn")
    void getPaymentByBookingId_NotFound_ThrowsException() {
        // Given
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.getPaymentByBookingId(bookingId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("refundPayment - Thành công khi đơn ở trạng thái SUCCESS")
    void refundPayment_Success() {
        // Given
        payment.setStatus(PaymentStatus.SUCCESS);
        RefundPaymentRequest request = RefundPaymentRequest.builder().reason("Khách đổi lịch").build();

        PaymentVm refundedVm = PaymentVm.builder()
                .id(paymentId)
                .status(PaymentStatus.REFUNDED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toPaymentVm(payment)).thenReturn(refundedVm);

        // When
        PaymentVm result = paymentService.refundPayment(paymentId, request);

        // Then
        assertThat(result.status()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    @DisplayName("refundPayment - Thất bại ném BAD_REQUEST khi đơn chưa ở trạng thái SUCCESS")
    void refundPayment_NotSuccessStatus_ThrowsException() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        RefundPaymentRequest request = RefundPaymentRequest.builder().reason("Khách đổi lịch").build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When & Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId, request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);

        verify(paymentRepository, never()).save(any());
    }
}
