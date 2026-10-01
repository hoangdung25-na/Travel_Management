import Link from "next/link";
import { LayoutDashboard, Map, Ticket, Users, MapPin } from "lucide-react";

const NAV = [
  { href: "/admin", label: "Tổng quan", icon: LayoutDashboard },
  { href: "/admin/tours", label: "Tour & lịch trình", icon: Map },
  { href: "/admin/destinations", label: "Điểm đến", icon: MapPin },
  { href: "/admin/bookings", label: "Đơn hàng", icon: Ticket },
  { href: "/admin/users", label: "Người dùng", icon: Users },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto max-w-6xl px-4 sm:px-6 py-8 grid md:grid-cols-[200px_1fr] gap-8">
      <aside>
        <p className="text-xs uppercase tracking-[0.2em] text-coral mb-4">Quản trị</p>
        <nav className="flex md:flex-col gap-1 overflow-x-auto">
          {NAV.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-ink-soft hover:bg-paper-dim hover:text-ink transition-colors whitespace-nowrap"
            >
              <item.icon size={15} />
              {item.label}
            </Link>
          ))}
        </nav>
      </aside>
      <div className="min-w-0">{children}</div>
    </div>
  );
}
