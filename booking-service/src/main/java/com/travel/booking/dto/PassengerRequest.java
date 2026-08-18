package com.travel.booking.dto;

import com.travel.booking.constant.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PassengerRequest(
        @NotBlank(message = "Họ và tên hành khách không được để trống")
        @Size(max = 100, message = "Họ và tên hành khách không vượt quá 100 ký tự")
        String fullName,

        LocalDate dateOfBirth,

        @NotNull(message = "Loại vé hành khách không được để trống")
        PassengerType passengerType,

        @Size(max = 30, message = "Số CCCD/Passport không vượt quá 30 ký tự")
        String idCardNumber
) {
}
