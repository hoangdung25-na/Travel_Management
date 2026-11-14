package com.travel.auth.service;

import com.travel.auth.constant.AccountStatus;
import com.travel.auth.dto.LoginRequest;
import com.travel.auth.dto.RefreshTokenRequest;
import com.travel.auth.dto.RegisterRequest;
import com.travel.auth.dto.UpdateProfileRequest;
import com.travel.auth.viewmodel.AuthTokenVm;
import com.travel.auth.viewmodel.UserProfileVm;
import com.travel.auth.viewmodel.UserVm;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AuthService {
    UserVm register(RegisterRequest request);
    AuthTokenVm login(LoginRequest request);
    AuthTokenVm refreshToken(RefreshTokenRequest request);
    UserProfileVm getProfile(UUID userId);
    UserProfileVm updateProfile(UUID userId, UpdateProfileRequest request, MultipartFile avatarFile);
    UserVm updateUserStatus(UUID userId, AccountStatus status);
    java.util.List<UserVm> getAllUsers();
    UserVm adminUpdateUser(UUID userId, com.travel.auth.dto.AdminUpdateUserRequest request);
    void adminDeleteUser(UUID userId);
    AuthTokenVm loginWithGoogle(com.travel.auth.dto.GoogleLoginRequest request);
    void sendForgotPasswordOtp(String email);
    boolean verifyForgotPasswordOtp(String email, String otp);
    void resetPassword(com.travel.auth.dto.ResetPasswordRequest request);
}
