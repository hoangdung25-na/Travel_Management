package com.travel.booking.service;

import com.travel.booking.constant.BookingStatus;
import com.travel.booking.entity.Booking;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingSagaStateDomainServiceTest {

    private BookingSagaStateDomainService domainService;
    private Booking booking;

    @BeforeEach
    void setUp() {
        domainService = new BookingSagaStateDomainService();
        booking = Booking.builder()
                .id(UUID.randomUUID())
                .bookingCode("BK20260819-TEST")
                .userId(UUID.randomUUID())
                .tourScheduleId(UUID.randomUUID())
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Chuyển trạng thái từ PENDING sang PAYMENT_PENDING thành công khi giữ chỗ")
    void validateAndTransitionToSeatsReserved_ValidPendingStatus_Success() {
        // Arrange
        booking.setStatus(BookingStatus.PENDING);

        // Act
        domainService.validateAndTransitionToSeatsReserved(booking);

        // Assert
        assertEquals(BookingStatus.PAYMENT_PENDING, booking.getStatus());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi giữ chỗ cho đơn hàng không ở trạng thái PENDING")
    void validateAndTransitionToSeatsReserved_InvalidStatus_ThrowsException() {
        // Arrange
        booking.setStatus(BookingStatus.CONFIRMED);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                domainService.validateAndTransitionToSeatsReserved(booking)
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("Chuyển trạng thái từ PAYMENT_PENDING sang CONFIRMED thành công khi thanh toán")
    void validateAndTransitionToConfirmed_ValidStatus_Success() {
        // Arrange
        booking.setStatus(BookingStatus.PAYMENT_PENDING);

        // Act
        domainService.validateAndTransitionToConfirmed(booking);

        // Assert
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    @DisplayName("Xử lý Idempotent: Đơn hàng đã CONFIRMED từ trước sẽ giữ nguyên không ném lỗi")
    void validateAndTransitionToConfirmed_AlreadyConfirmed_IdempotentSuccess() {
        // Arrange
        booking.setStatus(BookingStatus.CONFIRMED);

        // Act & Assert
        assertDoesNotThrow(() -> domainService.validateAndTransitionToConfirmed(booking));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi cố gắng xác nhận đơn hàng đã bị CANCELLED")
    void validateAndTransitionToConfirmed_CancelledStatus_ThrowsException() {
        // Arrange
        booking.setStatus(BookingStatus.CANCELLED);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                domainService.validateAndTransitionToConfirmed(booking)
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("Chuyển trạng thái sang CANCELLED thành công từ PENDING")
    void validateAndTransitionToCancelled_PendingStatus_Success() {
        // Arrange
        booking.setStatus(BookingStatus.PENDING);

        // Act
        domainService.validateAndTransitionToCancelled(booking);

        // Assert
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi cố gắng hủy đơn hàng đã hoàn tất CONFIRMED qua API này")
    void validateAndTransitionToCancelled_AlreadyConfirmed_ThrowsException() {
        // Arrange
        booking.setStatus(BookingStatus.CONFIRMED);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () ->
                domainService.validateAndTransitionToCancelled(booking)
        );

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
    }
}
