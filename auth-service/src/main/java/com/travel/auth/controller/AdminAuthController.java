package com.travel.auth.controller;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.service.AuthService;
import com.travel.auth.viewmodel.UserVm;
import com.travel.common.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final AuthService authService;

    @PutMapping("/users/{userId}/status")
    public ApiResponse<UserVm> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam AccountStatus status) {
        log.info("Admin updating status for user ID {} to {}", userId, status);
        UserVm userVm = authService.updateUserStatus(userId, status);
        return ApiResponse.ok(userVm, "Cập nhật trạng thái tài khoản thành công");
    }
}
