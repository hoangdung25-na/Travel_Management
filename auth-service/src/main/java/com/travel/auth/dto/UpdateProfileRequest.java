package com.travel.auth.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 100, message = "Họ và tên không vượt quá 100 ký tự")
    private String fullName;

    @Pattern(regexp = "^\\d{10}$", message = "Số điện thoại phải bao gồm 10 chữ số")
    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Thông tin liên hệ khẩn cấp không vượt quá 100 ký tự")
    private String emergencyContact;
}
