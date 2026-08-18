package com.travel.booking.service;

import com.travel.booking.constant.BookingStatus;
import com.travel.booking.entity.Booking;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class BookingSagaStateDomainService {

    /**
     * Chuyển trạng thái khi Tour Service xác nhận giữ chỗ thành công: PENDING -> PAYMENT_PENDING
     */
    public void validateAndTransitionToSeatsReserved(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Không thể chuyển sang trạng thái giữ chỗ vì đơn hàng ở trạng thái: " + booking.getStatus());
        }
        booking.setStatus(BookingStatus.PAYMENT_PENDING);
    }

    /**
     * Chuyển trạng thái khi Payment Service xác nhận thanh toán thành công: PAYMENT_PENDING/SEATS_RESERVED -> CONFIRMED
     */
    public void validateAndTransitionToConfirmed(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Không thể xác nhận đơn hàng đã bị hủy");
        }
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return; // Idempotent: Đã xác nhận trước đó thì không báo lỗi
        }
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    /**
     * Chuyển trạng thái sang CANCELLED (khi giữ chỗ thất bại / thanh toán thất bại / người dùng yêu cầu hủy)
     */
    public void validateAndTransitionToCancelled(Booking booking) {
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Không thể hủy đơn hàng đã hoàn tất thanh toán thành công qua API này");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return; // Idempotent: Đã hủy trước đó thì giữ nguyên
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }
}


