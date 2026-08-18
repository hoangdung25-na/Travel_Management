package com.travel.tour.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record CreateTourRequest(
        @NotBlank(message = "Mã tour không được để trống")
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Mã tour chỉ được chứa chữ in hoa, số và dấu gạch ngang")
        String code,

        @NotBlank(message = "Tên tour không được để trống")
        @Size(min = 10, max = 250, message = "Tên tour phải từ 10 đến 250 ký tự")
        String title,

        @NotBlank(message = "Mô tả tour không được để trống")
        String description,

        @NotEmpty(message = "Danh sách lịch trình không được để trống")
        @Valid
        List<CreateItineraryRequest> itineraries,

        Set<UUID> destinationIds
) {
}
