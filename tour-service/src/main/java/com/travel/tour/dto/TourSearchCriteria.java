package com.travel.tour.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TourSearchCriteria(
        String keyword,
        String destination,

        @PositiveOrZero(message = "Giá tối thiểu phải lớn hơn hoặc bằng 0")
        BigDecimal minPrice,

        @PositiveOrZero(message = "Giá tối đa phải lớn hơn hoặc bằng 0")
        BigDecimal maxPrice,

        Integer page,
        Integer size
) {
    public TourSearchCriteria {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
    }
}
