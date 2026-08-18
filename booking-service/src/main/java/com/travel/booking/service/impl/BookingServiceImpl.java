package com.travel.booking.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.booking.client.TourServiceClient;
import com.travel.booking.client.dto.TourScheduleResponse;
import com.travel.booking.constant.BookingStatus;
import com.travel.booking.constant.OutboxStatus;
import com.travel.booking.constant.PassengerType;
import com.travel.booking.dto.CancelBookingRequest;
import com.travel.booking.dto.CreateBookingRequest;
import com.travel.booking.dto.PassengerRequest;
import com.travel.booking.entity.Booking;
import com.travel.booking.entity.BookingPassenger;
import com.travel.booking.entity.OutboxEventEntity;
import com.travel.booking.helper.BookingCodeGenerator;
import com.travel.booking.mapper.BookingMapper;
import com.travel.booking.repository.BookingRepository;
import com.travel.booking.repository.OutboxEventRepository;
import com.travel.booking.service.BookingSagaStateDomainService;
import com.travel.booking.service.BookingService;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingVm;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BookingCodeGenerator bookingCodeGenerator;
    private final BookingSagaStateDomainService bookingSagaStateDomainService;
    private final TourServiceClient tourServiceClient;
    private final BookingMapper bookingMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public BookingVm createBooking(UUID userId, CreateBookingRequest request) {
        log.info("Khởi tạo đơn đặt tour cho userId: {}, tourScheduleId: {}", userId, request.tourScheduleId());

        // 1. Lấy thông tin giá vé thực tế của chuyến đi từ tour-service (SSOT)
        TourScheduleResponse scheduleInfo = tourServiceClient.getScheduleById(request.tourScheduleId());

        String bookingCode = bookingCodeGenerator.generateCode();
        BigDecimal totalAmount = calculateTotalAmount(request, scheduleInfo);

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(userId)
                .tourScheduleId(request.tourScheduleId())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        for (PassengerRequest passengerReq : request.passengers()) {
            BigDecimal price = calculatePassengerPrice(passengerReq.passengerType(), scheduleInfo);
            BookingPassenger passenger = BookingPassenger.builder()
                    .fullName(passengerReq.fullName())
                    .dateOfBirth(passengerReq.dateOfBirth())
                    .passengerType(passengerReq.passengerType())
                    .idCardNumber(passengerReq.idCardNumber())
                    .price(price)
                    .build();
            booking.addPassenger(passenger);
        }

        // 2. Lưu Booking + BookingPassenger xuống DB
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Ghi bản tin Outbox Event trong cùng DB Transaction
        createAndSaveOutboxEvent("BookingCreatedEvent", savedBooking);

        log.info("Tạo thành công Booking ID: {}, Mã: {}, Tổng tiền: {}", savedBooking.getId(), savedBooking.getBookingCode(), savedBooking.getTotalAmount());
        return bookingMapper.toBookingVm(savedBooking);
    }

    @Override
    public Page<BookingVm> getMyBookings(UUID userId, Pageable pageable) {
        log.info("Lấy danh sách đơn đặt tour của userId: {}", userId);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookingMapper::toBookingVm);
    }

    @Override
    public BookingDetailVm getBookingDetail(UUID userId, UUID bookingId) {
        log.info("Lấy chi tiết đơn đặt tour ID: {} cho userId: {}", bookingId, userId);
        Booking booking = bookingRepository.findByIdWithPassengers(bookingId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.BOOKING_NOT_FOUND, "Không tìm thấy đơn đặt tour với ID: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN, "Bạn không có quyền truy cập thông tin đơn hàng này");
        }

        return bookingMapper.toBookingDetailVm(booking);
    }

    @Override
    @Transactional
    public BookingVm cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request) {
        log.info("Yêu cầu hủy đơn đặt tour ID: {}, Lý do: {}", bookingId, request.reason());
        Booking booking = bookingRepository.findByIdWithPassengers(bookingId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.BOOKING_NOT_FOUND, "Không tìm thấy đơn đặt tour với ID: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN, "Bạn không có quyền hủy đơn hàng này");
        }

        // Chuyển trạng thái sang CANCELLED qua Domain Service
        bookingSagaStateDomainService.validateAndTransitionToCancelled(booking);
        Booking updatedBooking = bookingRepository.save(booking);

        // Ghi Outbox Event hủy đơn để hoàn chỗ bên Tour Service
        createAndSaveOutboxEvent("BookingCancelledEvent", updatedBooking);

        return bookingMapper.toBookingVm(updatedBooking);
    }

    private BigDecimal calculateTotalAmount(CreateBookingRequest request, TourScheduleResponse scheduleInfo) {
        BigDecimal total = BigDecimal.ZERO;
        for (PassengerRequest passenger : request.passengers()) {
            total = total.add(calculatePassengerPrice(passenger.passengerType(), scheduleInfo));
        }
        return total;
    }

    private BigDecimal calculatePassengerPrice(PassengerType passengerType, TourScheduleResponse scheduleInfo) {
        if (passengerType == PassengerType.CHILD) {
            return scheduleInfo.priceChild() != null ? scheduleInfo.priceChild() : scheduleInfo.priceAdult().multiply(new BigDecimal("0.75"));
        } else if (passengerType == PassengerType.INFANT) {
            return BigDecimal.ZERO;
        }
        return scheduleInfo.priceAdult();
    }

    private void createAndSaveOutboxEvent(String eventType, Booking booking) {
        try {
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("bookingId", booking.getId());
            payloadMap.put("bookingCode", booking.getBookingCode());
            payloadMap.put("userId", booking.getUserId());
            payloadMap.put("tourScheduleId", booking.getTourScheduleId());
            payloadMap.put("passengerCount", booking.getPassengers().size());
            payloadMap.put("totalAmount", booking.getTotalAmount());
            payloadMap.put("status", booking.getStatus().name());

            String payloadJson = objectMapper.writeValueAsString(payloadMap);

            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                    .aggregateType("Booking")
                    .aggregateId(booking.getId().toString())
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(OutboxStatus.PENDING)
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Lỗi serialize payload cho Outbox Event: {}", eventType, e);
            throw BusinessException.of(ErrorCode.INTERNAL_SERVER_ERROR, "Không thể tạo sự kiện Outbox cho đơn hàng");
        }
    }
}


