package com.travel.tour.viewmodel;

import com.travel.tour.constant.TourStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record TourVm(
        UUID tourId,
        String code,
        String title,
        BigDecimal minPrice,
        Integer availableSeats,
        TourStatus status,
        OffsetDateTime createdAt
) {
}
