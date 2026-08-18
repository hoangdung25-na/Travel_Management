package com.travel.tour.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateDestinationRequest(
        @NotBlank(message = "Tên điểm đến không được để trống")
        String name,

        @NotBlank(message = "Thành phố không được để trống")
        String city,

        @NotBlank(message = "Quốc gia không được để trống")
        String country
) {
}
