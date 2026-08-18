package com.travel.tour.service;

import java.util.UUID;

public interface SeatAllocationDomainService {

    /**
     * Thực hiện trừ/giữ chỗ nguyên tố cho chuyến đi.
     * Kiểm tra điều kiện Tour phải ở trạng thái PUBLISHED và số chỗ còn lại đủ cho requestedSeats.
     *
     * @param scheduleId ID của lịch khởi hành
     * @param requestedSeats Số lượng chỗ cần giữ
     * @return true nếu giữ chỗ thành công, false nếu không đủ chỗ hoặc Tour chưa PUBLISHED
     */
    boolean reserveSeats(UUID scheduleId, int requestedSeats);

    /**
     * Hoàn lại số chỗ trống (Compensating Transaction) khi đơn hàng bị hủy hoặc thanh toán thất bại.
     *
     * @param scheduleId ID của lịch khởi hành
     * @param releasedSeats Số lượng chỗ cần hoàn
     */
    void releaseSeats(UUID scheduleId, int releasedSeats);
}
