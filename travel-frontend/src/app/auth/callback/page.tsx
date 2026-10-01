"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";
import Cookies from "js-cookie";
import { ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";

const KEYCLOAK_HOST = process.env.NEXT_PUBLIC_KEYCLOAK_URL ?? "http://localhost:8180";
const REALM = process.env.NEXT_PUBLIC_KEYCLOAK_REALM ?? "travel-realm";
const CLIENT_ID = process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID ?? "travel-client";

function CallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { refreshUser } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = searchParams.get("code");
    const redirect = searchParams.get("redirect") ?? "/";
    if (!code) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- xử lý trường hợp Keycloak redirect về thiếu mã code
      setError("Không nhận được mã xác thực từ Google.");
      return;
    }

    const redirectUri = `${window.location.origin}/auth/callback?redirect=${encodeURIComponent(redirect)}`;

    // NOTE: đây là exchange trực tiếp với Keycloak token endpoint (Authorization Code flow,
    // client public). Cần xác nhận với backend rằng `travel-client` được cấu hình là public
    // client (không cần client_secret) để luồng này chạy được từ trình duyệt.
    axios
      .post(
        `${KEYCLOAK_HOST}/realms/${REALM}/protocol/openid-connect/token`,
        new URLSearchParams({
          grant_type: "authorization_code",
          client_id: CLIENT_ID,
          code,
          redirect_uri: redirectUri,
        }),
        { headers: { "Content-Type": "application/x-www-form-urlencoded" } }
      )
      .then(async (res) => {
        Cookies.set(ACCESS_TOKEN_COOKIE, res.data.access_token, { expires: 7 });
        Cookies.set(REFRESH_TOKEN_COOKIE, res.data.refresh_token, { expires: 7 });
        await refreshUser();
        router.push(redirect);
      })
      .catch(() => setError("Đăng nhập bằng Google thất bại. Vui lòng thử lại."));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (error) {
    return (
      <div className="mx-auto max-w-sm px-4 py-24 text-center">
        <p className="text-coral mb-3">{error}</p>
        <a href="/login" className="text-teal underline text-sm">
          Quay lại đăng nhập
        </a>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-sm px-4 py-24 text-center text-ink-soft">
      Đang hoàn tất đăng nhập...
    </div>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense>
      <CallbackContent />
    </Suspense>
  );
}
