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
import { MOCK_USERS } from "./mock-data";

interface AuthContextValue {
  user: UserProfileVm | null;
  roles: UserRole[];
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<UserProfileVm>;
  loginWithGoogle: (email?: string, fullName?: string) => Promise<UserProfileVm>;
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
      if (activeRole) {
        profile.roles = activeRole.split(",") as UserRole[];
      }
      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
    } catch {
      const savedUserStr = localStorage.getItem("tm_user_profile");
      if (savedUserStr) {
        try {
          const parsed = JSON.parse(savedUserStr);
          setUser(parsed);
          setIsLoading(false);
          return;
        } catch {}
      }

      const defaultUser = MOCK_USERS[0];
      setUser({
        ...defaultUser,
        dateOfBirth: "1995-05-15",
        emergencyContact: "0909999888",
      });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = useCallback(async (payload: LoginRequest) => {
    const tokens = await authService.login(payload);
    Cookies.set(ACCESS_TOKEN_COOKIE, tokens.accessToken, { expires: 7 });
    Cookies.set(REFRESH_TOKEN_COOKIE, tokens.refreshToken, { expires: 7 });

    try {
      const profile = await authService.getProfile();
      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
      if (profile.roles && profile.roles.length > 0) {
        Cookies.set(ROLES_COOKIE, profile.roles.join(","), { expires: 7 });
      }
      return profile;
    } catch {
      const roles: UserRole[] = payload.username.includes("admin")
        ? ["ROLE_ADMIN"]
        : payload.username.includes("guide")
        ? ["ROLE_GUIDE"]
        : ["ROLE_TOURIST"];

      const mockMatch = MOCK_USERS.find((u) => u.roles.includes(roles[0])) ?? MOCK_USERS[0];
      const profile: UserProfileVm = {
        ...mockMatch,
        email: payload.username,
        roles,
      };

      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
      Cookies.set(ROLES_COOKIE, roles.join(","), { expires: 7 });
      return profile;
    }
  }, []);

  const loginWithGoogle = useCallback(
    async (email: string = "dunghoang@gmail.com", fullName: string = "Hoàng Dũng") => {
      const roles: UserRole[] = ["ROLE_TOURIST"];
      const profile: UserProfileVm = {
        userId: "11111111-1111-1111-1111-111111111111",
        email,
        fullName,
        status: "ACTIVE",
        roles,
        phoneNumber: "0901234567",
        createdAt: new Date().toISOString(),
        dateOfBirth: "1995-05-15",
        emergencyContact: "0909999888",
      };

      setUser(profile);
      localStorage.setItem("tm_user_profile", JSON.stringify(profile));
      Cookies.set(ACCESS_TOKEN_COOKIE, `google-access-token-${Date.now()}`, { expires: 7 });
      Cookies.set(REFRESH_TOKEN_COOKIE, `google-refresh-token-${Date.now()}`, { expires: 7 });
      Cookies.set(ROLES_COOKIE, roles.join(","), { expires: 7 });
      return profile;
    },
    []
  );

  const register = useCallback(async (payload: RegisterRequest) => {
    await authService.register(payload);
  }, []);

  const switchRole = useCallback(
    (newRole: UserRole) => {
      const targetUser = MOCK_USERS.find((u) => u.roles.includes(newRole)) ?? MOCK_USERS[0];
      const updated: UserProfileVm = {
        ...targetUser,
        roles: [newRole],
      };
      setUser(updated);
      localStorage.setItem("tm_user_profile", JSON.stringify(updated));
      Cookies.set(ROLES_COOKIE, newRole, { expires: 7 });
      Cookies.set(ACCESS_TOKEN_COOKIE, `mock-token-${newRole}`, { expires: 7 });

      if (newRole === "ROLE_ADMIN") router.push("/admin");
      else if (newRole === "ROLE_GUIDE") router.push("/guide");
      else router.push("/");
    },
    [router]
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
