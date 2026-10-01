"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  TrendingUp,
  Map,
  Ticket,
  Users,
  ShieldAlert,
  ArrowRight,
  Plus,
  CheckCircle2,
} from "lucide-react";
import { MockStore } from "@/lib/mock-store";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { BookingDetailVm, UserVm, TourDetailVm } from "@/lib/types";

export default function AdminDashboardPage() {
  const [bookings, setBookings] = useState<BookingDetailVm[]>([]);
  const [tours, setTours] = useState<TourDetailVm[]>([]);
  const [users, setUsers] = useState<UserVm[]>([]);

  useEffect(() => {
    setBookings(MockStore.getAllBookings());
    setTours(MockStore.getTours({ size: 100 }).content as any);
    setUsers(MockStore.getUsers());
  }, []);

  const totalRevenue = bookings
    .filter((b) => b.status === "CONFIRMED")
    .reduce((acc, b) => acc + b.totalAmount, 0);

  const pendingGuides = users.filter(
    (u) => u.roles.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL"
  );

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-widest text-teal-600 font-bold block mb-1">
            Hệ Thống Quản Trị Trung Tâm
          </span>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Tổng Quan Hệ Thống Microservices
          </h1>
        </div>

        <Link
          href="/admin/tours"
          className="inline-flex items-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs px-4 py-2.5 rounded-xl shadow-md transition-all shrink-0"
        >
          <Plus size={16} /> Tạo Tour Mới
        </Link>
      </div>

      {/* Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500">Tổng Doanh Thu</span>
            <div className="w-8 h-8 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center">
              <TrendingUp size={18} />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-slate-900">{formatCurrency(totalRevenue)}</p>
          <p className="text-[11px] text-emerald-600 font-semibold">Tăng +18% so với tháng trước</p>
        </div>

        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500">Tổng Tour Hoạt Động</span>
            <div className="w-8 h-8 rounded-xl bg-teal-100 text-teal-600 flex items-center justify-center">
              <Map size={18} />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-slate-900">{tours.length} Tour</p>
          <p className="text-[11px] text-slate-400">Đã xuất bản trên toàn hệ thống</p>
        </div>

        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500">Đơn Đặt Tour</span>
            <div className="w-8 h-8 rounded-xl bg-sky-100 text-sky-600 flex items-center justify-center">
              <Ticket size={18} />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-slate-900">{bookings.length} Đơn</p>
          <p className="text-[11px] text-sky-600 font-semibold">
            {bookings.filter((b) => b.status === "CONFIRMED").length} Đơn đã thanh toán
          </p>
        </div>

        <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-slate-500">Duyệt Hướng Dẫn Viên</span>
            <div className="w-8 h-8 rounded-xl bg-amber-100 text-amber-600 flex items-center justify-center">
              <Users size={18} />
            </div>
          </div>
          <p className="text-2xl font-extrabold text-amber-600">{pendingGuides.length} Hồ sơ</p>
          <Link href="/admin/users" className="text-[11px] text-amber-600 font-semibold underline block">
            Xem hồ sơ chờ duyệt ngay
          </Link>
        </div>
      </div>

      {/* Pending Guide Approval Alert Banner */}
      {pendingGuides.length > 0 && (
        <div className="bg-amber-50 border border-amber-200 p-4 rounded-2xl flex items-center justify-between gap-4 text-xs">
          <div className="flex items-center gap-3 text-amber-900">
            <ShieldAlert size={20} className="text-amber-600 shrink-0" />
            <span>
              Có <strong>{pendingGuides.length} tài khoản Hướng dẫn viên</strong> đang chờ Admin duyệt quyền dẫn đoàn.
            </span>
          </div>
          <Link
            href="/admin/users"
            className="bg-amber-600 hover:bg-amber-700 text-white font-bold px-4 py-2 rounded-xl transition-colors shrink-0"
          >
            Duyệt Ngay
          </Link>
        </div>
      )}

      {/* Recent Bookings Table */}
      <div className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
        <div className="flex items-center justify-between border-b border-slate-100 pb-4">
          <h3 className="font-bold text-slate-900 text-base">Đơn Đặt Tour Mới Nhất</h3>
          <Link href="/admin/bookings" className="text-xs font-semibold text-teal-600 hover:text-teal-700 flex items-center gap-1">
            Xem tất cả đơn hàng <ArrowRight size={14} />
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-100 uppercase tracking-wider">
              <tr>
                <th className="py-3 px-4">Mã Đơn</th>
                <th className="py-3 px-4">Khách Hàng</th>
                <th className="py-3 px-4">Tour</th>
                <th className="py-3 px-4">Số Tiền</th>
                <th className="py-3 px-4">Trạng Thái</th>
                <th className="py-3 px-4">Thời Gian</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {bookings.slice(0, 5).map((b) => (
                <tr key={b.bookingId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3.5 px-4 font-mono font-bold text-teal-600">{b.bookingCode}</td>
                  <td className="py-3.5 px-4">{b.userFullName || b.userId}</td>
                  <td className="py-3.5 px-4 font-bold text-slate-900 max-w-xs truncate">{b.tourTitle}</td>
                  <td className="py-3.5 px-4 font-extrabold text-rose-600">{formatCurrency(b.totalAmount)}</td>
                  <td className="py-3.5 px-4">
                    <span
                      className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                        b.status === "CONFIRMED"
                          ? "bg-emerald-100 text-emerald-700"
                          : b.status === "PENDING"
                          ? "bg-amber-100 text-amber-700"
                          : "bg-rose-100 text-rose-700"
                      }`}
                    >
                      {b.status}
                    </span>
                  </td>
                  <td className="py-3.5 px-4 text-slate-400">{formatDateTime(b.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
