package com.travel.auth.viewmodel;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.constant.RoleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVm {
    private UUID userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private AccountStatus status;
    private Set<RoleCode> roles;
    private OffsetDateTime createdAt;
}
