"use client";

import Link from "next/link";
import { useState } from "react";
import {
  Compass,
  Menu,
  X,
  User,
  LogOut,
  LayoutDashboard,
  Ticket,
  Sparkles,
  ShieldCheck,
  MapPin,
} from "lucide-react";
import { useAuth } from "@/lib/auth-context";

export function Header() {
  const { isAuthenticated, user, roles, logout, isLoading } = useAuth();
  const [open, setOpen] = useState(false);

  const isAdmin = isAuthenticated && roles.includes("ROLE_ADMIN");
  const isGuide = isAuthenticated && roles.includes("ROLE_GUIDE");

  return (
    <header className="sticky top-0 z-50 bg-[#0D2B2B]/95 backdrop-blur-md text-stone-100 border-b border-[#123A3A] shadow-xl">
      <div className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="flex h-16 items-center justify-between">
          {/* Brand Logo */}
          <Link href="/" className="flex items-center gap-2.5 font-bold text-xl tracking-tight text-white group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#E2603F] to-[#D4A24C] flex items-center justify-center shadow-lg shadow-[#E2603F]/20 group-hover:scale-105 transition-transform">
              <Compass size={24} className="text-white" />
            </div>
            <div className="flex flex-col">
              <span className="leading-none text-lg bg-gradient-to-r from-white via-amber-100 to-[#D4A24C] bg-clip-text text-transparent font-extrabold">
                TravelPlatform
              </span>
              <span className="text-[10px] text-[#D4A24C] font-medium tracking-wider uppercase mt-0.5">
                Du Lịch Việt Nam
              </span>
            </div>
          </Link>

          {/* Navigation Links */}
          <nav className="hidden md:flex items-center gap-8 text-sm font-medium">
            <Link href="/tours" className="hover:text-[#D4A24C] transition-colors flex items-center gap-1.5 text-stone-200">
              <MapPin size={16} className="text-[#E2603F]" /> Danh mục Tour
            </Link>
            <Link href="/ai-assistant" className="hover:text-[#E2603F] transition-colors flex items-center gap-1.5 text-[#D4A24C] font-semibold">
              <Sparkles size={16} className="text-[#D4A24C] animate-pulse" /> Trợ lý AI Thông Minh
            </Link>
            {isAdmin && (
              <Link href="/admin" className="hover:text-[#E2603F] transition-colors flex items-center gap-1.5 text-[#D4A24C]">
                <LayoutDashboard size={16} /> Quản trị Admin
              </Link>
            )}
            {isGuide && (
              <Link href="/guide" className="hover:text-[#E2603F] transition-colors flex items-center gap-1.5 text-[#D4A24C]">
                <ShieldCheck size={16} /> Lịch Dẫn Đoàn
              </Link>
            )}
          </nav>

          {/* Right Actions: Auth */}
          <div className="hidden md:flex items-center gap-4">
            {isLoading ? null : isAuthenticated ? (
              <div className="flex items-center gap-3">
                <Link
                  href="/account/bookings"
                  className="flex items-center gap-1.5 text-xs bg-[#123A3A] hover:bg-[#1a4e4e] text-stone-200 px-3 py-1.5 rounded-lg border border-[#1f5252] transition-colors"
                >
                  <Ticket size={14} className="text-[#E2603F]" /> Đơn Đặt Tour
                </Link>

                <Link
                  href="/account/profile"
                  className="flex items-center gap-1.5 text-xs text-stone-200 hover:text-white font-medium transition-colors"
                >
                  <User size={14} className="text-[#D4A24C]" /> {user?.fullName?.split(" ")[0] ?? "Tài khoản"}
                </Link>

                <button
                  onClick={logout}
                  className="text-stone-400 hover:text-[#E2603F] transition-colors p-1.5"
                  title="Đăng xuất"
                >
                  <LogOut size={16} />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  href="/login"
                  className="text-xs font-medium text-stone-300 hover:text-white px-3 py-1.5 transition-colors"
                >
                  Đăng nhập
                </Link>
                <Link
                  href="/register"
                  className="text-xs font-semibold bg-[#E2603F] hover:bg-[#d55333] text-white px-4 py-2 rounded-lg shadow-md transition-all"
                >
                  Đăng ký
                </Link>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <button className="md:hidden p-2 text-stone-300" onClick={() => setOpen(!open)} aria-label="Menu">
            {open ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {open && (
        <div className="md:hidden border-t border-[#123A3A] px-4 pb-6 pt-4 flex flex-col gap-4 text-sm bg-[#0D2B2B]">
          <Link href="/tours" onClick={() => setOpen(false)} className="flex items-center gap-2 text-stone-200">
            <MapPin size={16} className="text-[#E2603F]" /> Tìm kiếm Tour
          </Link>
          <Link href="/ai-assistant" onClick={() => setOpen(false)} className="flex items-center gap-2 text-[#D4A24C]">
            <Sparkles size={16} /> Trợ lý AI Thông Minh
          </Link>
          {isAdmin && (
            <Link href="/admin" onClick={() => setOpen(false)} className="flex items-center gap-2 text-[#D4A24C]">
              <LayoutDashboard size={16} /> Trang Quản trị Admin
            </Link>
          )}
          {isGuide && (
            <Link href="/guide" onClick={() => setOpen(false)} className="flex items-center gap-2 text-[#D4A24C]">
              <ShieldCheck size={16} /> Lịch Dẫn Đoàn Tour
            </Link>
          )}

          <div className="pt-3 border-t border-[#123A3A] flex items-center justify-between">
            {isAuthenticated ? (
              <>
                <Link href="/account/bookings" onClick={() => setOpen(false)} className="text-[#E2603F] font-medium">
                  Đơn hàng của tôi
                </Link>
                <button onClick={logout} className="text-[#E2603F] font-medium">
                  Đăng xuất
                </button>
              </>
            ) : (
              <>
                <Link href="/login" onClick={() => setOpen(false)} className="text-stone-300">
                  Đăng nhập
                </Link>
                <Link href="/register" onClick={() => setOpen(false)} className="text-[#E2603F] font-semibold">
                  Đăng ký ngay
                </Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
