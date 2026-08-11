package com.travel.auth.viewmodel;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.constant.RoleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileVm {
    private UUID userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private String emergencyContact;
    private AccountStatus status;
    private Set<RoleCode> roles;
}
