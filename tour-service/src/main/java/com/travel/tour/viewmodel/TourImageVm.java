package com.travel.tour.viewmodel;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TourImageVm(
        UUID id,
        String imageUrl,
        String caption,
        Boolean isPrimary,
        Integer displayOrder
) {
}
