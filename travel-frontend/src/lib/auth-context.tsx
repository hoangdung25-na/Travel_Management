"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from "react";
import Cookies from "js-cookie";
import { useRouter } from "next/navigation";
import { authService } from "./services/auth-service";
import {
  ACCESS_TOKEN_COOKIE,
  REFRESH_TOKEN_COOKIE,
  ROLES_COOKIE,
} from "./api-client";
import { LoginRequest, RegisterRequest, UserProfileVm, UserRole } from "./types";

interface AuthContextValue {
  user: UserProfileVm | null;
  roles: UserRole[];
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<UserProfileVm>;
  loginWithGoogle: (email: string, fullName: string, avatarUrl?: string) => Promise<UserProfileVm>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  switchRole: (role: UserRole) => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfileVm | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  const refreshUser = useCallback(async () => {
    const token = Cookies.get(ACCESS_TOKEN_COOKIE);
    const activeRole = Cookies.get(ROLES_COOKIE);

    if (!token) {
      setUser(null);
      setIsLoading(false);
      return;
    }

    try {
      const profile = await authService.getProfile();
      if (activeRole && profile.roles.includes(activeRole as UserRole)) {
        profile.roles = [activeRole as UserRole, ...profile.roles.filter(r => r !== activeRole)];
      }
      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
    } catch {
      const savedUserStr = localStorage.getItem("tm_user_profile");
      if (savedUserStr) {
        try {
          const parsed = JSON.parse(savedUserStr);
          setUser(parsed);
        } catch {
          setUser(null);
        }
      } else {
        setUser(null);
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = useCallback(async (payload: LoginRequest): Promise<UserProfileVm> => {
    // 1. Gọi trực tiếp API Backend thật
    const tokens = await authService.login(payload);
    Cookies.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, { expires: 7 });
    Cookies.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, { expires: 7 });

    // 2. Lấy hồ sơ người dùng thực tế từ PostgreSQL
    const profile = await authService.getProfile();
    setUser(profile);
    localStorage.setItem("tm_user_profile", JSON.stringify(profile));
    if (profile.roles && profile.roles.length > 0) {
      Cookies.set(ROLES_COOKIE, profile.roles.join(","), { expires: 7 });
    }
    return profile;
  }, []);

  const loginWithGoogle = useCallback(
    async (
      email: string,
      fullName: string,
      avatarUrl?: string
    ): Promise<UserProfileVm> => {
      // 1. Gửi thông tin Google về Backend auth-service để lưu hoặc cập nhật vào database PostgreSQL thật
      const tokens = await authService.loginWithGoogle({ email, fullName, avatarUrl });
      Cookies.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, { expires: 7 });
      Cookies.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, { expires: 7 });

      // 2. Lấy thông tin user thật từ database
      const profile = await authService.getProfile();
      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
      if (profile.roles && profile.roles.length > 0) {
        Cookies.set(ROLES_COOKIE, profile.roles.join(","), { expires: 7 });
      }
      return profile;
    },
    []
  );

  const register = useCallback(async (payload: RegisterRequest) => {
    // Đăng ký trực tiếp với PostgreSQL qua auth-service
    await authService.register(payload);
  }, []);

  const switchRole = useCallback(
    (newRole: UserRole) => {
      if (!user) return;
      const updatedRoles = [newRole, ...user.roles.filter((r) => r !== newRole)];
      const updatedUser: UserProfileVm = {
        ...user,
        roles: updatedRoles,
      };
      setUser(updatedUser);
      localStorage.setItem("tm_user_profile", JSON.stringify(updatedUser));
      Cookies.set(ROLES_COOKIE, newRole, { expires: 7 });

      if (newRole === "ROLE_ADMIN") router.push("/admin");
      else if (newRole === "ROLE_GUIDE") router.push("/guide");
      else router.push("/");
    },
    [user, router]
  );

  const logout = useCallback(() => {
    Cookies.remove(ACCESS_TOKEN_COOKIE);
    Cookies.remove(REFRESH_TOKEN_COOKIE);
    Cookies.remove(ROLES_COOKIE);
    localStorage.removeItem("tm_user_profile");
    setUser(null);
    router.push("/login");
  }, [router]);

  const value: AuthContextValue = {
    user,
    roles: user?.roles ?? [],
    isLoading,
    isAuthenticated: !!user,
    login,
    loginWithGoogle,
    register,
    logout,
    refreshUser,
    switchRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth phải dùng bên trong AuthProvider");
  return ctx;
}
