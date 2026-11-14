"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { LayoutDashboard, Map, Ticket, Users, MapPin, ShieldCheck } from "lucide-react";

const NAV = [
  { href: "/admin", label: "Tổng quan", icon: LayoutDashboard },
  { href: "/admin/tours", label: "Tour & Lịch trình", icon: Map },
  { href: "/admin/destinations", label: "Điểm đến", icon: MapPin },
  { href: "/admin/bookings", label: "Đơn đặt tour", icon: Ticket },
  { href: "/admin/users", label: "Người dùng", icon: Users },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();

  return (
    <div className="min-h-screen bg-slate-50/50">
      <div className="mx-auto max-w-[1440px] px-4 sm:px-6 lg:px-8 py-8 grid md:grid-cols-[240px_1fr] gap-8 items-start">
        <aside className="sticky top-24 bg-white rounded-3xl p-5 border border-slate-200/80 shadow-xs">
          <div className="flex items-center gap-2 mb-6 pb-4 border-b border-slate-100">
            <div className="w-8 h-8 rounded-xl bg-teal-500/10 flex items-center justify-center text-teal-600">
              <ShieldCheck size={18} />
            </div>
            <div>
              <p className="text-[10px] uppercase tracking-widest text-teal-600 font-bold">Hệ Thống</p>
              <h2 className="text-sm font-extrabold text-slate-800">Quản Trị Viên</h2>
            </div>
          </div>

          <nav className="flex md:flex-col gap-1.5 overflow-x-auto">
            {NAV.map((item) => {
              const isActive =
                item.href === "/admin"
                  ? pathname === "/admin"
                  : pathname?.startsWith(item.href);

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-xs font-semibold transition-all whitespace-nowrap ${
                    isActive
                      ? "bg-teal-600 text-white shadow-md shadow-teal-600/20"
                      : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
                  }`}
                >
                  <item.icon size={16} className={isActive ? "text-white" : "text-slate-400"} />
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </aside>
        <div className="min-w-0">{children}</div>
      </div>
    </div>
  );
}

