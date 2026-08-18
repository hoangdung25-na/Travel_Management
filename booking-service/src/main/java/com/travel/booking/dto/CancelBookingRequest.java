package com.travel.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CancelBookingRequest(
        @NotBlank(message = "Lý do hủy đơn không được để trống")
        @Size(max = 255, message = "Lý do hủy đơn không vượt quá 255 ký tự")
        String reason
) {
}
