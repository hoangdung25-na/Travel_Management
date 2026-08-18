package com.travel.booking.client;

import com.travel.booking.client.dto.TourScheduleResponse;

import java.util.UUID;

public interface TourServiceClient {

    // Lấy thông tin giá vé và số chỗ thực tế từ tour-service
    TourScheduleResponse getScheduleById(UUID scheduleId);
}
