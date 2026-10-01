import { NextRequest, NextResponse } from "next/server";

const ACCESS_TOKEN_COOKIE = "tm_access_token";
const ROLES_COOKIE = "tm_roles";

// Prefix route -> role bắt buộc để truy cập
const PROTECTED_PREFIXES: { prefix: string; role: string }[] = [
  { prefix: "/admin", role: "ROLE_ADMIN" },
  { prefix: "/guide", role: "ROLE_GUIDE" },
  { prefix: "/account", role: "" }, // chỉ cần đăng nhập, không cần role cụ thể
];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const match = PROTECTED_PREFIXES.find((p) => pathname.startsWith(p.prefix));
  if (!match) return NextResponse.next();

  const token = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  if (!token) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("redirect", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (match.role) {
    const roles = (request.cookies.get(ROLES_COOKIE)?.value ?? "").split(",");
    if (!roles.includes(match.role)) {
      return NextResponse.redirect(new URL("/", request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/admin/:path*", "/guide/:path*", "/account/:path*"],
};
