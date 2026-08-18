package com.travel.booking.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TourScheduleResponse(
        UUID id,
        BigDecimal priceAdult,
        BigDecimal priceChild,
        Integer availableSeats
) {
}
