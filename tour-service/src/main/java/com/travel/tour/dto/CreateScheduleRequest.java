package com.travel.tour.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record CreateScheduleRequest(
        @NotNull(message = "Thời gian khởi hành không được để trống")
        @Future(message = "Thời gian khởi hành phải ở tương lai")
        OffsetDateTime departureTime,

        @NotNull(message = "Thời gian đến không được để trống")
        @Future(message = "Thời gian đến phải ở tương lai")
        OffsetDateTime arrivalTime,

        @NotNull(message = "Giá vé người lớn không được để trống")
        @Positive(message = "Giá vé người lớn phải lớn hơn 0")
        BigDecimal priceAdult,

        @NotNull(message = "Giá vé trẻ em không được để trống")
        @PositiveOrZero(message = "Giá vé trẻ em phải lớn hơn hoặc bằng 0")
        BigDecimal priceChild,

        @NotNull(message = "Tổng số chỗ không được để trống")
        @Positive(message = "Tổng số chỗ phải lớn hơn 0")
        Integer totalSeats
) {
}
