package com.travel.tour.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateItineraryRequest(
        @NotNull(message = "Số ngày không được để trống")
        @Positive(message = "Số ngày phải lớn hơn 0")
        Integer dayNumber,

        @NotBlank(message = "Tiêu đề lịch trình không được để trống")
        @Size(min = 3, max = 255, message = "Tiêu đề lịch trình phải từ 3 đến 255 ký tự")
        String title,

        @NotBlank(message = "Nội dung lịch trình không được để trống")
        String content
) {
}
