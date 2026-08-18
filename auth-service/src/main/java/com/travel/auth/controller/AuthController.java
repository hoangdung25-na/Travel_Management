package com.travel.auth.controller;

import com.travel.auth.dto.LoginRequest;
import com.travel.auth.dto.RefreshTokenRequest;
import com.travel.auth.dto.RegisterRequest;
import com.travel.auth.dto.UpdateProfileRequest;
import com.travel.auth.service.AuthService;
import com.travel.auth.viewmodel.AuthTokenVm;
import com.travel.auth.viewmodel.UserProfileVm;
import com.travel.auth.viewmodel.UserVm;
import com.travel.common.core.dto.ApiResponse;
import com.travel.common.core.exception.BusinessException;
import com.travel.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserVm> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for email: {}", request.getEmail());
        UserVm userVm = authService.register(request);
        return ApiResponse.ok(userVm, "Đăng ký tài khoản thành công");
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenVm> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        AuthTokenVm tokenVm = authService.login(request);
        return ApiResponse.ok(tokenVm, "Đăng nhập thành công");
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthTokenVm> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        AuthTokenVm tokenVm = authService.refreshToken(request);
        return ApiResponse.ok(tokenVm, "Làm mới token thành công");
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileVm> getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String userIdStr = UserContext.getUserId() != null ? UserContext.getUserId() : headerUserId;
        if (userIdStr == null || userIdStr.isBlank()) {
            throw BusinessException.badRequest("Thiếu thông tin người dùng xác thực (X-User-Id)");
        }
        UUID userId = UUID.fromString(userIdStr);
        UserProfileVm profileVm = authService.getProfile(userId);
        return ApiResponse.ok(profileVm, "Lấy thông tin hồ sơ người dùng thành công");
    }

    @PutMapping(value = "/me", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ApiResponse<UserProfileVm> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @Valid @RequestPart(value = "profile", required = false) UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        String userIdStr = UserContext.getUserId() != null ? UserContext.getUserId() : headerUserId;
        if (userIdStr == null || userIdStr.isBlank()) {
            throw BusinessException.badRequest("Thiếu thông tin người dùng xác thực (X-User-Id)");
        }
        UUID userId = UUID.fromString(userIdStr);
        if (request == null) {
            request = new UpdateProfileRequest();
        }
        UserProfileVm profileVm = authService.updateProfile(userId, request, avatarFile);
        return ApiResponse.ok(profileVm, "Cập nhật hồ sơ thành công");
    }
}
