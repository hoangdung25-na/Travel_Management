package com.travel.tour.viewmodel;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record TourScheduleVm(
        UUID id,
        OffsetDateTime departureTime,
        OffsetDateTime arrivalTime,
        BigDecimal priceAdult,
        BigDecimal priceChild,
        Integer totalSeats,
        Integer availableSeats
) {
}
