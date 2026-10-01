package com.travel.payment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.payment.config.VnPayProperties;
import com.travel.payment.constant.OutboxStatus;
import com.travel.payment.constant.PaymentMethod;
import com.travel.payment.constant.PaymentStatus;
import com.travel.payment.dto.CreatePaymentUrlRequest;
import com.travel.payment.dto.RefundPaymentRequest;
import com.travel.payment.dto.event.PaymentFailedEvent;
import com.travel.payment.dto.event.PaymentProcessedEvent;
import com.travel.payment.entity.OutboxEventEntity;
import com.travel.payment.entity.Payment;
import com.travel.payment.entity.PaymentTransaction;
import com.travel.payment.helper.VnPayUtils;
import com.travel.payment.mapper.PaymentMapper;
import com.travel.payment.repository.OutboxEventRepository;
import com.travel.payment.repository.PaymentRepository;
import com.travel.payment.repository.PaymentTransactionRepository;
import com.travel.payment.service.PaymentService;
import com.travel.payment.service.PaymentVerificationDomainService;
import com.travel.payment.viewmodel.PaymentUrlVm;
import com.travel.payment.viewmodel.PaymentVm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation Service xử lý logic nghiệp vụ thanh toán
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentVerificationDomainService verificationDomainService;
    private final VnPayProperties vnPayProperties;
    private final PaymentMapper paymentMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentUrlVm createPaymentUrl(CreatePaymentUrlRequest request, HttpServletRequest httpRequest) {
        log.info("Khởi tạo liên kết thanh toán cho đơn hàng: bookingId={}", request.bookingId());

        BigDecimal targetAmount = request.amount() != null ? request.amount() : new BigDecimal("1000000.00");
        Payment payment = paymentRepository.findByBookingId(request.bookingId())
                .orElseGet(() -> Payment.builder()
                        .bookingId(request.bookingId())
                        .amount(targetAmount)
                        .paymentMethod(request.paymentMethod())
                        .status(PaymentStatus.PENDING)
                        .build());

        if (request.amount() != null) {
            payment.setAmount(request.amount());
        }
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        // 2. Sinh mã tham chiếu giao dịch duy nhất
        String txnRef = "VNP" + System.currentTimeMillis() + VnPayUtils.getRandomNumber(3);

        // 3. Xây dựng bộ tham số chuẩn gửi tới VNPAY
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayProperties.getVersion());
        vnpParams.put("vnp_Command", vnPayProperties.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayProperties.getTmnCode());

        // Số tiền tính theo đơn vị Đồng * 100 theo quy định VNPAY (ví dụ: 100,000 VND -> 10000000)
        long amountInCents = payment.getAmount().multiply(new BigDecimal("100")).longValue();
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", vnPayProperties.getCurrCode());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + request.bookingId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", vnPayProperties.getLocale());
        vnpParams.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VnPayUtils.getIpAddress(httpRequest));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // 4. Sắp xếp tham số ASCII & Tạo chuỗi Hash Checksum
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = VnPayUtils.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        String paymentUrl = vnPayProperties.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;

        // 5. Lưu nhật ký giao dịch khởi tạo PaymentTransaction
        PaymentTransaction transaction = PaymentTransaction.builder()
                .payment(payment)
                .txnRef(txnRef)
                .amount(payment.getAmount())
                .build();
        payment.addTransaction(transaction);
        paymentRepository.save(payment);

        return PaymentUrlVm.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .paymentUrl(paymentUrl)
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> processVnPayCallback(Map<String, String> queryParams) {
        log.info("Tiếp nhận Webhook Callback IPN từ VNPAY: queryParams={}", queryParams);

        Map<String, String> response = new HashMap<>();

        // 1. Kiểm tra tính toàn vẹn chữ ký số HASH
        boolean isValidSignature = verificationDomainService.verifyVnPaySignature(queryParams, vnPayProperties.getHashSecret());
        if (!isValidSignature) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid Checksum");
            return response;
        }

        String txnRef = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");

        // 2. Kiểm tra tồn tại của mã giao dịch tham chiếu
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findByTxnRef(txnRef);
        if (transactionOpt.isEmpty()) {
            response.put("RspCode", "01");
            response.put("Message", "Order Not Found");
            return response;
        }

        PaymentTransaction transaction = transactionOpt.get();
        Payment payment = transaction.getPayment();

        // 3. Kiểm tra Idempotency (Tránh xử lý lặp lại nếu đơn đã confirm trước đó)
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        // 4. Xử lý kết quả giao dịch từ VNPAY
        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            transaction.setGatewayResponseCode("00");
            transaction.setRawResponse(queryParams.toString());

            // Lưu sự kiện PaymentProcessedEvent vào Outbox Table (Transactional Outbox Pattern)
            PaymentProcessedEvent eventPayload = PaymentProcessedEvent.builder()
                    .bookingId(payment.getBookingId())
                    .paymentId(payment.getId())
                    .amount(payment.getAmount())
                    .paymentMethod(PaymentMethod.VNPAY.name())
                    .status(PaymentStatus.SUCCESS.name())
                    .build();

            saveOutboxEvent("PaymentProcessedEvent", payment.getId().toString(), eventPayload);
            log.info("Thanh toán thành công cho đơn hàng: bookingId={}, paymentId={}", payment.getBookingId(), payment.getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            transaction.setGatewayResponseCode(responseCode);
            transaction.setRawResponse(queryParams.toString());

            // Lưu sự kiện PaymentFailedEvent vào Outbox Table
            PaymentFailedEvent eventPayload = PaymentFailedEvent.builder()
                    .bookingId(payment.getBookingId())
                    .paymentId(payment.getId())
                    .reason("VNPAY Transaction Failed with code: " + responseCode)
                    .build();

            saveOutboxEvent("PaymentFailedEvent", payment.getId().toString(), eventPayload);
            log.warn("Thanh toán thất bại cho đơn hàng: bookingId={}, code={}", payment.getBookingId(), responseCode);
        }

        paymentRepository.save(payment);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentVm getPaymentByBookingId(UUID bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "Không tìm thấy thông tin thanh toán cho bookingId: " + bookingId));
        return paymentMapper.toPaymentVm(payment);
    }

    @Override
    @Transactional
    public PaymentVm refundPayment(UUID paymentId, RefundPaymentRequest request) {
        log.info("Yêu cầu hoàn tiền giao dịch: paymentId={}, reason={}", paymentId, request.reason());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "Không tìm thấy giao dịch thanh toán id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Chỉ giao dịch ở trạng thái SUCCESS mới được hoàn tiền");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        log.info("Hoàn tiền thành công cho giao dịch: paymentId={}", paymentId);
        return paymentMapper.toPaymentVm(payment);
    }

    /**
     * Helper lưu sự kiện Outbox Event chuẩn hóa giống booking-service & tour-service
     */
    private void saveOutboxEvent(String eventType, String aggregateId, Object payloadObj) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payloadObj);
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .aggregateType("PAYMENT")
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Lỗi đóng gói JSON cho Outbox Event {}: ", eventType, e);
            throw BusinessException.of(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể tạo sự kiện Outbox cho thanh toán");
        }
    }
}
