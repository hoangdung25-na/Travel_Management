import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import Cookies from "js-cookie";
import { ApiResponse, AuthTokenVm } from "./types";
import { resolveErrorMessage, TOKEN_EXPIRED_CODE } from "./error-codes";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export const IS_MOCK_MODE = process.env.NEXT_PUBLIC_USE_MOCK === "true";

export const ACCESS_TOKEN_COOKIE = "tm_access_token";
export const REFRESH_TOKEN_COOKIE = "tm_refresh_token";
export const ROLES_COOKIE = "tm_roles";

// Lỗi nghiệp vụ đã được chuẩn hóa từ ApiResponse, ném ra cho UI bắt và hiển thị.
export class ApiError extends Error {
  code: string;
  errors: string[] | null;
  status?: number;

  constructor(message: string, code: string, errors: string[] | null, status?: number) {
    super(message);
    this.code = code;
    this.errors = errors;
    this.status = status;
  }
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 30000, // Tăng timeout lên 30 giây để xử lý các request AI & Microservices
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = Cookies.get(ACCESS_TOKEN_COOKIE);
  const roles = Cookies.get(ROLES_COOKIE) || "ROLE_TOURIST";
  const userProfileStr = typeof window !== "undefined" ? localStorage.getItem("tm_user_profile") : null;

  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`);
    const uuidMatch = token.match(/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})/);
    if (uuidMatch && uuidMatch[1]) {
      config.headers.set("X-User-Id", uuidMatch[1]);
    }
  }

  if (userProfileStr) {
    try {
      const user = JSON.parse(userProfileStr);
      if (user?.userId && user.userId !== "11111111-1111-1111-1111-111111111111") config.headers.set("X-User-Id", user.userId);
      if (user?.roles) config.headers.set("X-User-Roles", Array.isArray(user.roles) ? user.roles.join(",") : user.roles);
      if (user?.email) config.headers.set("X-User-Email", user.email);
    } catch {}
  }
  return config;
});

let isRefreshing = false;
let pendingQueue: Array<() => void> = [];

function clearSession() {
  Cookies.remove(ACCESS_TOKEN_COOKIE);
  Cookies.remove(REFRESH_TOKEN_COOKIE);
  Cookies.remove(ROLES_COOKIE);
}

async function performRefresh(): Promise<string | null> {
  const refreshToken = Cookies.get(REFRESH_TOKEN_COOKIE);
  if (!refreshToken) return null;
  try {
    const res = await axios.post<ApiResponse<AuthTokenVm>>(
      `${API_BASE_URL}/api/v1/auth/refresh-token`,
      { refreshToken }
    );
    const data = res.data.data;
    if (!data) return null;
    Cookies.set(ACCESS_TOKEN_COOKIE, data.accessToken, { expires: 7 });
    Cookies.set(REFRESH_TOKEN_COOKIE, data.refreshToken, { expires: 7 });
    return data.accessToken;
  } catch {
    return null;
  }
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    const body = error.response?.data;

    // AUTH-1002 = access token hết hạn -> thử refresh 1 lần rồi gọi lại request cũ
    if (body?.code === TOKEN_EXPIRED_CODE && original && !original._retry) {
      original._retry = true;

      if (!isRefreshing) {
        isRefreshing = true;
        const newToken = await performRefresh();
        isRefreshing = false;
        pendingQueue.forEach((resolve) => resolve());
        pendingQueue = [];
        if (!newToken) {
          clearSession();
          if (typeof window !== "undefined") window.location.href = "/login";
          return Promise.reject(error);
        }
        return apiClient(original);
      }

      return new Promise((resolve) => {
        pendingQueue.push(() => resolve(apiClient(original)));
      });
    }

    const message = resolveErrorMessage(body?.code, body?.message || error.message);
    return Promise.reject(
      new ApiError(message, body?.code ?? "NETWORK_ERROR", body?.errors ?? null, error.response?.status)
    );
  }
);

// Helper: unwrap ApiResponse.data, ném ApiError nếu success=false
export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const res = await promise;
  if (!res.data.success) {
    throw new ApiError(
      resolveErrorMessage(res.data.code, res.data.message),
      res.data.code,
      res.data.errors
    );
  }
  return res.data.data as T;
}

// Wrap mock response in standard ApiResponse wrapper
export function wrapMockResponse<T>(data: T, message: string = "Thao tác thực hiện thành công"): ApiResponse<T> {
  return {
    success: true,
    code: "OK",
    message,
    data,
    errors: null,
    path: typeof window !== "undefined" ? window.location.pathname : "/mock",
    traceId: `mock-trace-${Date.now()}`,
    timestamp: new Date().toISOString(),
  };
}

