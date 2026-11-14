"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { jwtDecode } from "jwt-decode";
import { useAuth } from "@/lib/auth-context";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";
import { buildGoogleLoginUrl } from "@/lib/keycloak";
import { Button } from "@/components/ui/Button";

interface GoogleJwtPayload {
  email: string;
  name: string;
  picture?: string;
  sub: string;
}

function LoginForm() {
  const { login, loginWithGoogle } = useAuth();
  const { notify } = useToast();
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirect = searchParams.get("redirect") ?? "/";

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [showGoogleModal, setShowGoogleModal] = useState(false);
  const [googleEmail, setGoogleEmail] = useState("dunghoang@gmail.com");
  const [googleClientConfigured, setGoogleClientConfigured] = useState(false);

  const googleBtnRef = useRef<HTMLDivElement>(null);

  // Kích hoạt Google Identity Services (One Tap & Google Button thật)
  useEffect(() => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID?.trim();
    if (!clientId) {
      setGoogleClientConfigured(false);
      return;
    }
    setGoogleClientConfigured(true);

    const initGoogleGsi = () => {
      // @ts-expect-error google GIS window object
      const google = window.google;
      if (google?.accounts?.id) {
        google.accounts.id.initialize({
          client_id: clientId,
          callback: async (response: { credential: string }) => {
            try {
              setLoading(true);
              const decoded = jwtDecode<GoogleJwtPayload>(response.credential);
              await loginWithGoogle(decoded.email, decoded.name, decoded.picture);
              notify("success", `Đăng nhập Google thành công: ${decoded.email}`);
              router.push(redirect);
            } catch {
              notify("error", "Đăng nhập bằng Google thất bại. Vui lòng thử lại.");
            } finally {
              setLoading(false);
            }
          },
          auto_select: false,
          cancel_on_tap_outside: true,
        });

        // Tự động bật thông báo Google One Tap ở góc phải màn hình
        google.accounts.id.prompt();

        // Render nút Google chính chủ nếu có ref
        if (googleBtnRef.current) {
          googleBtnRef.current.innerHTML = "";
          google.accounts.id.renderButton(googleBtnRef.current, {
            theme: "outline",
            size: "large",
            width: 350,
            text: "signin_with",
            shape: "rectangular",
            logo_alignment: "left",
          });
        }
      }
    };

    const timer = setInterval(() => {
      // @ts-expect-error google GIS window object
      if (window.google?.accounts?.id) {
        initGoogleGsi();
        clearInterval(timer);
      }
    }, 300);

    return () => clearInterval(timer);
  }, [loginWithGoogle, notify, redirect, router]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      await login({ username, password });
      notify("success", "Đăng nhập thành công.");
      router.push(redirect);
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể đăng nhập, vui lòng kiểm tra lại tài khoản và mật khẩu.");
    } finally {
      setLoading(false);
    }
  }

  function handleGoogleLoginClick() {
    // @ts-expect-error google GIS window object
    const google = window.google;
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID?.trim();

    if (google?.accounts?.id && clientId) {
      // Đã có Google Client ID -> Kích hoạt Google One Tap / Account Picker thật của trình duyệt
      google.accounts.id.prompt((notification: { isNotDisplayed: () => boolean; getNotDisplayedReason: () => string }) => {
        if (notification.isNotDisplayed()) {
          // Nếu One Tap bị chặn bởi trình duyệt, mở modal hỗ trợ
          setShowGoogleModal(true);
        }
      });
    } else {
      // Chưa cấu hình Client ID -> Mở modal hướng dẫn và hỗ trợ thử nghiệm nhanh
      setShowGoogleModal(true);
    }
  }

  async function confirmGoogleLogin(selectedEmail?: string) {
    const emailToUse = selectedEmail || googleEmail || "dunghoang@gmail.com";
    const nameToUse = emailToUse.split("@")[0].toUpperCase();
    try {
      setLoading(true);
      await loginWithGoogle(emailToUse, nameToUse);
      notify("success", `Đăng nhập thử nghiệm thành công với ${emailToUse}`);
      setShowGoogleModal(false);
      router.push(redirect);
    } catch {
      notify("error", "Đăng nhập thất bại.");
    } finally {
      setLoading(false);
    }
  }

  function handleKeycloakOAuth() {
    if (typeof window === "undefined") return;
    const callbackUrl = `${window.location.origin}/auth/callback?redirect=${encodeURIComponent(redirect)}`;
    window.location.href = buildGoogleLoginUrl(callbackUrl);
  }

  return (
    <div className="mx-auto max-w-sm px-4 py-20">
      <p className="text-xs uppercase tracking-[0.2em] text-[#E2603F] mb-2 text-center font-bold">
        Chào mừng trở lại
      </p>
      <h1 className="font-display text-3xl text-center mb-8 font-extrabold text-stone-900">
        Đăng nhập
      </h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <input
            required
            type="email"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Email"
            className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
          />
        </div>

        <div>
          <input
            required
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Mật khẩu"
            className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
          />
          <div className="flex justify-end pt-1.5">
            <Link
              href="/forgot-password"
              className="text-xs text-[#E2603F] hover:underline font-semibold"
            >
              Quên mật khẩu?
            </Link>
          </div>
        </div>

        <Button type="submit" className="w-full font-bold shadow-md" loading={loading}>
          Đăng nhập
        </Button>
      </form>

      <div className="flex items-center gap-3 my-6">
        <div className="flex-1 h-px bg-[#e7e0d3]" />
        <span className="text-xs text-stone-500">hoặc</span>
        <div className="flex-1 h-px bg-[#e7e0d3]" />
      </div>

      {/* Vùng hiển thị nút Google */}
      <div className="space-y-3">
        {googleClientConfigured && (
          <div ref={googleBtnRef} className="w-full flex justify-center min-h-[44px]" />
        )}

        <button
          onClick={handleGoogleLoginClick}
          type="button"
          className="w-full py-2.5 px-4 bg-white border border-[#e7e0d3] hover:border-stone-400 text-stone-700 font-medium rounded-lg text-sm flex items-center justify-center gap-2.5 shadow-sm transition-all hover:bg-[#FBF8F2]"
        >
          <svg className="w-4 h-4 shrink-0" viewBox="0 0 24 24">
            <path
              fill="#4285F4"
              d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
            />
            <path
              fill="#34A853"
              d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
            />
            <path
              fill="#FBBC05"
              d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
            />
            <path
              fill="#EA4335"
              d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
            />
          </svg>
          <span>Đăng nhập bằng Google</span>
        </button>
      </div>

      <p className="text-center text-sm text-stone-600 mt-8">
        Chưa có tài khoản?{" "}
        <Link href="/register" className="text-[#E2603F] font-bold hover:underline">
          Đăng ký ngay
        </Link>
      </p>

      {/* Google Setup Guidance & Demo Dialog */}
      {showGoogleModal && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-5 animate-in fade-in zoom-in duration-200">
            <div className="flex items-center gap-3 border-b border-slate-100 pb-3">
              <svg className="w-6 h-6 shrink-0" viewBox="0 0 24 24">
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <div>
                <h3 className="font-bold text-slate-800 text-base">Đăng nhập với Google</h3>
                <p className="text-xs text-slate-500">Google One Tap & Tài khoản cá nhân</p>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-200 rounded-xl p-3 text-xs text-amber-800 space-y-1">
              <p className="font-semibold">💡 Để bật cửa sổ Google thật của máy tính:</p>
              <p>
                1. Mở file <code className="bg-amber-100 px-1 py-0.5 rounded font-mono">.env.local</code> ở thư mục frontend.
              </p>
              <p>
                2. Dán mã <code className="bg-amber-100 px-1 py-0.5 rounded font-mono">NEXT_PUBLIC_GOOGLE_CLIENT_ID</code> lấy miễn phí từ Google Cloud Console (loại Web Application, thêm URI <code className="font-mono">http://localhost:3000</code>).
              </p>
            </div>

            <div className="space-y-2">
              <p className="text-xs font-semibold text-slate-700">Hoặc chọn tài khoản thử nghiệm nhanh (Demo):</p>
              <button
                onClick={() => confirmGoogleLogin("dunghoang@gmail.com")}
                className="w-full p-3 rounded-xl border border-teal-500/40 bg-teal-50/50 hover:bg-teal-100/60 text-left transition-colors flex items-center gap-3"
              >
                <div className="w-9 h-9 rounded-full bg-teal-600 text-white font-bold text-sm flex items-center justify-center shrink-0">
                  HD
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-bold text-slate-800 truncate">Hoàng Dũng (Google)</p>
                  <p className="text-[11px] text-slate-500 truncate">dunghoang@gmail.com</p>
                </div>
              </button>
            </div>

            <div className="space-y-2 pt-2 border-t border-slate-100">
              <label className="text-xs font-semibold text-slate-700">Hoặc nhập email Google bất kỳ:</label>
              <input
                type="email"
                value={googleEmail}
                onChange={(e) => setGoogleEmail(e.target.value)}
                placeholder="your.email@gmail.com"
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:border-teal-500"
              />
            </div>

            <div className="flex items-center justify-between gap-2 pt-2">
              <button
                type="button"
                onClick={handleKeycloakOAuth}
                className="text-[11px] text-slate-400 hover:text-teal-600 underline"
                title="Chuyển sang Cổng Keycloak SSO"
              >
                Keycloak SSO
              </button>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setShowGoogleModal(false)}
                  className="px-3 py-1.5 rounded-lg border border-slate-200 text-xs font-medium text-slate-600 hover:bg-slate-100"
                >
                  Hủy
                </button>
                <button
                  type="button"
                  onClick={() => confirmGoogleLogin()}
                  className="px-4 py-1.5 rounded-lg bg-teal-600 hover:bg-teal-700 text-white text-xs font-semibold shadow-md"
                >
                  Xác nhận
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense>
      <LoginForm />
    </Suspense>
  );
}
