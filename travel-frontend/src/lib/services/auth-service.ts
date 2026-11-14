import { apiClient, unwrap } from "../api-client";
import {
  AuthTokenVm,
  LoginRequest,
  RefreshTokenRequest,
  RegisterRequest,
  UpdateProfileRequest,
  UserProfileVm,
  UserVm,
  ApiResponse,
} from "../types";

export const authService = {
  login: (payload: LoginRequest): Promise<AuthTokenVm> =>
    unwrap<AuthTokenVm>(
      apiClient.post<ApiResponse<AuthTokenVm>>("/api/v1/auth/login", payload)
    ),

  register: (payload: RegisterRequest): Promise<UserVm> =>
    unwrap<UserVm>(
      apiClient.post<ApiResponse<UserVm>>("/api/v1/auth/register", payload)
    ),

  loginWithGoogle: (payload: { email: string; fullName: string; avatarUrl?: string }): Promise<AuthTokenVm> =>
    unwrap<AuthTokenVm>(
      apiClient.post<ApiResponse<AuthTokenVm>>("/api/v1/auth/google", payload)
    ),

  sendForgotPasswordOtp: (email: string): Promise<string> =>
    unwrap<string>(
      apiClient.post<ApiResponse<string>>("/api/v1/auth/forgot-password/send-otp", { email })
    ),

  verifyForgotPasswordOtp: (email: string, otp: string): Promise<boolean> =>
    unwrap<boolean>(
      apiClient.post<ApiResponse<boolean>>("/api/v1/auth/forgot-password/verify-otp", { email, otp })
    ),

  resetPassword: (payload: { email: string; otp: string; newPassword: string }): Promise<string> =>
    unwrap<string>(
      apiClient.post<ApiResponse<string>>("/api/v1/auth/forgot-password/reset", payload)
    ),

  refreshToken: (payload: RefreshTokenRequest): Promise<AuthTokenVm> =>
    unwrap<AuthTokenVm>(
      apiClient.post<ApiResponse<AuthTokenVm>>("/api/v1/auth/refresh-token", payload)
    ),

  getProfile: (): Promise<UserProfileVm> =>
    unwrap<UserProfileVm>(
      apiClient.get<ApiResponse<UserProfileVm>>("/api/v1/auth/me")
    ),

  updateProfile: (payload: UpdateProfileRequest, avatar?: File): Promise<UserProfileVm> => {
    const form = new FormData();
    form.append(
      "profile",
      new Blob([JSON.stringify(payload)], { type: "application/json" })
    );
    if (avatar) form.append("avatar", avatar);
    return unwrap<UserProfileVm>(
      apiClient.put<ApiResponse<UserProfileVm>>("/api/v1/auth/me", form, {
        headers: { "Content-Type": "multipart/form-data" },
      })
    );
  },

  updateUserStatus: (userId: string, status: "PENDING_APPROVAL" | "ACTIVE" | "BLOCKED"): Promise<UserVm> =>
    unwrap<UserVm>(
      apiClient.put<ApiResponse<UserVm>>(
        `/api/v1/auth/admin/users/${userId}/status`,
        null,
        { params: { status } }
      )
    ),
};
