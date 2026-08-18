package com.travel.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CreateBookingRequest(
        @NotNull(message = "ID Lịch khởi hành không được để trống")
        UUID tourScheduleId,

        @NotEmpty(message = "Danh sách hành khách không được để trống")
        @Valid
        List<PassengerRequest> passengers
) {
}
