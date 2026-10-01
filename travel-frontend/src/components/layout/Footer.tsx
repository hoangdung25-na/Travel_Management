import Link from "next/link";
import { Compass, ShieldCheck, CreditCard, Sparkles, PhoneCall, Mail } from "lucide-react";

export function Footer() {
  return (
    <footer className="mt-auto bg-[#071919] text-stone-400 border-t border-[#123A3A]">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 py-12 grid grid-cols-1 md:grid-cols-5 gap-8 text-sm">
        <div className="md:col-span-2">
          <div className="flex items-center gap-2.5 font-bold text-xl text-white mb-3">
            <div className="w-8 h-8 rounded-lg bg-[#E2603F] flex items-center justify-center shadow-md">
              <Compass size={20} className="text-white" />
            </div>
            <span>Travel Platform</span>
          </div>
          <p className="text-stone-400 text-xs leading-relaxed max-w-sm mb-4">
            Hệ thống quản lý & đặt tour du lịch thông minh thiết kế theo kiến trúc Microservices chuẩn Event-Driven (Saga Pattern, Kafka Outbox, Redis Cache, Trợ lý AI Vector Search & Keycloak Security).
          </p>
          <div className="flex items-center gap-3">
            <span className="inline-flex items-center gap-1 text-[11px] bg-[#0D2B2B] border border-[#123A3A] text-stone-300 px-2.5 py-1 rounded-md">
              <ShieldCheck size={12} className="text-[#D4A24C]" /> Keycloak Auth
            </span>
            <span className="inline-flex items-center gap-1 text-[11px] bg-[#0D2B2B] border border-[#123A3A] text-[#D4A24C] px-2.5 py-1 rounded-md">
              <Sparkles size={12} className="text-[#D4A24C]" /> Gợi ý lịch trình thông minh
            </span>
          </div>
        </div>

        <div>
          <p className="text-stone-200 font-semibold mb-3">Dịch vụ & Tính năng</p>
          <ul className="space-y-2 text-xs">
            <li><Link href="/tours" className="hover:text-[#E2603F] transition-colors">Danh mục Tour toàn quốc</Link></li>
            <li><Link href="/ai-assistant" className="hover:text-[#E2603F] transition-colors">Gợi ý lịch trình thông minh</Link></li>
            <li><Link href="/account/bookings" className="hover:text-[#E2603F] transition-colors">Quản lý Đơn đặt tour</Link></li>
            <li><Link href="/guide" className="hover:text-[#E2603F] transition-colors">Cổng Hướng Dẫn Viên</Link></li>
          </ul>
        </div>

        <div>
          <p className="text-stone-200 font-semibold mb-3">Cổng Thanh Toán</p>
          <ul className="space-y-2 text-xs">
            <li className="flex items-center gap-2 text-stone-300">
              <CreditCard size={14} className="text-[#E2603F]" /> VNPAY QR Gate
            </li>
            <li className="flex items-center gap-2 text-stone-300">
              <CreditCard size={14} className="text-[#D4A24C]" /> Ví MoMo Payment
            </li>
            <li className="flex items-center gap-2 text-stone-300">
              <CreditCard size={14} className="text-[#E2603F]" /> Stripe International
            </li>
          </ul>
        </div>

        <div>
          <p className="text-stone-200 font-semibold mb-3">Hỗ trợ 24/7</p>
          <ul className="space-y-2 text-xs">
            <li className="flex items-center gap-2">
              <PhoneCall size={14} className="text-[#E2603F]" /> 1900 6868 (Hotline)
            </li>
            <li className="flex items-center gap-2">
              <Mail size={14} className="text-[#D4A24C]" /> support@travelplatform.vn
            </li>
            <li className="text-[11px] text-stone-500 pt-1">
              Phản hồi tức thì qua AI Chatbot 24/7 trên hệ thống.
            </li>
          </ul>
        </div>
      </div>

      <div className="border-t border-[#041111] py-4 text-center text-xs text-stone-500">
        © {new Date().getFullYear()} Travel Management Platform. Du lịch Việt Nam — Hành Trình Trọn Vẹn Niềm Vui.
      </div>
    </footer>
  );
}
