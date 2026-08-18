package com.travel.booking.controller;

import com.travel.booking.dto.CancelBookingRequest;
import com.travel.booking.dto.CreateBookingRequest;
import com.travel.booking.service.BookingService;
import com.travel.booking.viewmodel.BookingDetailVm;
import com.travel.booking.viewmodel.BookingVm;
import com.travel.common.core.dto.ApiResponse;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.core.exception.ErrorCode;
import com.travel.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<BookingVm> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        UUID userId = getCurrentUserId();
        log.info("REST request tạo đơn đặt tour cho userId: {}, scheduleId: {}", userId, request.tourScheduleId());
        BookingVm bookingVm = bookingService.createBooking(userId, request);
        return ApiResponse.ok(bookingVm, "Đơn đặt tour đã được tạo thành công, đang giữ chỗ tạm thời");
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<Page<BookingVm>> getMyBookings(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UUID userId = getCurrentUserId();
        log.info("REST request lấy danh sách booking của userId: {}", userId);
        Page<BookingVm> page = bookingService.getMyBookings(userId, pageable);
        return ApiResponse.ok(page, "Lấy danh sách đơn đặt tour thành công");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<BookingDetailVm> getBookingById(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        log.info("REST request lấy chi tiết booking ID: {} cho userId: {}", id, userId);
        BookingDetailVm detailVm = bookingService.getBookingDetail(userId, id);
        return ApiResponse.ok(detailVm, "Lấy chi tiết đơn đặt tour thành công");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TOURIST', 'GUIDE', 'ADMIN')")
    public ApiResponse<BookingVm> cancelBooking(
            @PathVariable UUID id,
            @Valid @RequestBody CancelBookingRequest request) {
        UUID userId = getCurrentUserId();
        log.info("REST request hủy đơn đặt tour ID: {} cho userId: {}, lý do: {}", id, userId, request.reason());
        BookingVm bookingVm = bookingService.cancelBooking(userId, id, request);
        return ApiResponse.ok(bookingVm, "Yêu cầu hủy đơn đặt tour thành công");
    }

    private UUID getCurrentUserId() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED, "Thiếu thông tin người dùng xác thực (X-User-Id)");
        }

        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Định dạng X-User-Id không hợp lệ (cần định dạng UUID)");
        }
    }
}

