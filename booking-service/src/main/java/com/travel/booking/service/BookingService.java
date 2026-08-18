package com.travel.booking.service;

import com.travel.booking.dto.CancelBookingRequest;
import com.travel.booking.dto.CreateBookingRequest;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingVm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    // Khởi tạo đơn đặt tour mới và lưu Outbox Event (Saga Initiator)
    BookingVm createBooking(UUID userId, CreateBookingRequest request);

    // Lấy danh sách booking của User có phân trang
    Page<BookingVm> getMyBookings(UUID userId, Pageable pageable);

    // Lấy chi tiết đơn đặt tour
    BookingDetailVm getBookingDetail(UUID userId, UUID bookingId);

    // Yêu cầu hủy đơn đặt tour
    BookingVm cancelBooking(UUID userId, UUID bookingId, CancelBookingRequest request);
}
