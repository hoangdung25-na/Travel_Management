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

    @GetMapping("/users")
    public ApiResponse<java.util.List<UserVm>> getAllUsers() {
        log.info("REST request to get all users for Admin");
        java.util.List<UserVm> users = authService.getAllUsers();
        return ApiResponse.ok(users, "Lấy danh sách người dùng thành công");
    }

    @PutMapping("/users/{userId}/status")
    public ApiResponse<UserVm> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam AccountStatus status) {
        log.info("Admin updating status for user ID {} to {}", userId, status);
        UserVm userVm = authService.updateUserStatus(userId, status);
        return ApiResponse.ok(userVm, "Cập nhật trạng thái tài khoản thành công");
    }

    @PutMapping("/users/{userId}")
    public ApiResponse<UserVm> adminUpdateUser(
            @PathVariable UUID userId,
            @jakarta.validation.Valid @RequestBody com.travel.auth.dto.AdminUpdateUserRequest request) {
        log.info("REST request to update user ID: {}", userId);
        UserVm userVm = authService.adminUpdateUser(userId, request);
        return ApiResponse.ok(userVm, "Cập nhật thông tin người dùng thành công");
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<Void> adminDeleteUser(@PathVariable UUID userId) {
        log.info("REST request to delete user ID: {}", userId);
        authService.adminDeleteUser(userId);
        return ApiResponse.ok(null, "Xóa tài khoản người dùng thành công");
    }
}
