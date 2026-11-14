package com.travel.auth.dto;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.constant.RoleCode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUpdateUserRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String phoneNumber;

    private AccountStatus status;

    private Set<RoleCode> roles;
}
