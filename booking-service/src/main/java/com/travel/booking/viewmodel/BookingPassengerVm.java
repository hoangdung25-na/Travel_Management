package com.travel.booking.viewmodel;

import com.travel.booking.constant.PassengerType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record BookingPassengerVm(
        UUID id,
        String fullName,
        LocalDate dateOfBirth,
        PassengerType passengerType,
        String idCardNumber,
        BigDecimal price
) {
}
