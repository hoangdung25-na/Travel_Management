package com.travel.tour.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TourImageRequest(
        String id,

        @NotBlank(message = "Đường dẫn ảnh không được để trống")
        String imageUrl,

        String caption,

        Boolean isPrimary,

        Integer displayOrder
) {
}
