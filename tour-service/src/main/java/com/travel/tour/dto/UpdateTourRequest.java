package com.travel.tour.dto;

import com.travel.tour.constant.TourStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record UpdateTourRequest(
        @NotBlank(message = "Tên tour không được để trống")
        @Size(min = 10, max = 250, message = "Tên tour phải từ 10 đến 250 ký tự")
        String title,

        @NotBlank(message = "Mô tả tour không được để trống")
        String description,

        TourStatus status,

        Set<UUID> destinationIds
) {
}
