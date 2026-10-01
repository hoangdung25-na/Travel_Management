"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  Ticket,
  ChevronRight,
  Clock,
  UserCheck,
  XCircle,
  CreditCard,
  AlertCircle,
  CalendarDays,
} from "lucide-react";
import { bookingService } from "@/lib/services/booking-service";
import { useAuth } from "@/lib/auth-context";
import { BookingDetailVm } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function MyBookingsPage() {
  const { user } = useAuth();
  const { notify } = useToast();
  const [bookings, setBookings] = useState<BookingDetailVm[] | null>(null);
  const [selectedBooking, setSelectedBooking] = useState<BookingDetailVm | null>(null);

  const [cancelReason, setCancelReason] = useState("");
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [isCancelling, setIsCancelling] = useState(false);

  useEffect(() => {
    bookingService
      .getMyBookings(user?.userId ?? "11111111-1111-1111-1111-111111111111", 0, 20)
      .then((res) => setBookings(res.content))
      .catch(() => setBookings([]));
  }, [user]);

  async function handleConfirmCancel() {
    if (!selectedBooking || !cancelReason.trim()) return;
    setIsCancelling(true);
    try {
      const updated = await bookingService.cancel(selectedBooking.bookingId, { reason: cancelReason });
      setBookings((prev) =>
        prev ? prev.map((b) => (b.bookingId === updated.bookingId ? updated : b)) : []
      );
      setSelectedBooking(updated);
      setShowCancelModal(false);
      setCancelReason("");
      notify("success", "Đã hủy đơn đặt tour thành công và hoàn trả số chỗ.");
    } catch (err: any) {
      notify("error", err.message || "Hủy đơn hàng thất bại.");
    } finally {
      setIsCancelling(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6 py-10 space-y-8">
      <div>
        <span className="text-xs uppercase tracking-widest text-[#E2603F] font-bold block mb-1">
          Hồ Sơ Cá Nhân
        </span>
        <h1 className="text-3xl font-extrabold text-stone-900 tracking-tight">
          Lịch Sử Đơn Đặt Tour Của Tôi
        </h1>
      </div>

      {bookings === null && (
        <div className="space-y-4">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="h-28 rounded-2xl bg-stone-200 animate-pulse" />
          ))}
        </div>
      )}

      {bookings?.length === 0 && (
        <div className="text-center py-20 bg-white rounded-3xl border border-[#e7e0d3] space-y-4">
          <div className="w-16 h-16 bg-[#FBF8F2] rounded-full flex items-center justify-center mx-auto text-stone-400 border border-[#e7e0d3]">
            <Ticket size={32} />
          </div>
          <div className="space-y-1">
            <p className="text-stone-800 font-bold text-base">Bạn chưa có đơn đặt tour nào.</p>
            <p className="text-stone-500 text-xs">Hãy khám phá danh mục tour và giữ chỗ ngay hôm nay!</p>
          </div>
          <Link
            href="/tours"
            className="inline-flex items-center gap-2 bg-[#E2603F] hover:bg-[#d55333] text-white font-semibold text-xs px-5 py-2.5 rounded-xl shadow-md transition-all"
          >
            Khám Phá Danh Mục Tour
          </Link>
        </div>
      )}

      {/* Booking List */}
      <div className="space-y-4">
        {bookings?.map((b) => (
          <div
            key={b.bookingId}
            className="bg-white rounded-2xl border border-[#e7e0d3] p-6 shadow-sm hover:shadow-md transition-all space-y-4"
          >
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#f5f0e6] pb-4">
              <div className="flex items-center gap-3">
                <span className="font-mono text-xs font-bold bg-[#0D2B2B] text-[#D4A24C] px-2.5 py-1 rounded border border-[#D4A24C]/30">
                  {b.bookingCode}
                </span>
                <StatusBadge status={b.status} />
              </div>
              <span className="text-xs text-stone-400">
                Tạo lúc: {formatDateTime(b.createdAt)}
              </span>
            </div>

            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="space-y-1">
                <h3 className="font-bold text-stone-900 text-base">{b.tourTitle}</h3>
                <p className="text-xs text-stone-500 flex items-center gap-1.5">
                  <CalendarDays size={14} className="text-[#E2603F]" />
                  Khởi hành: {formatDateTime(b.departureTime || b.createdAt)}
                </p>
                <p className="text-xs text-stone-500 flex items-center gap-1.5">
                  <UserCheck size={14} className="text-stone-400" />
                  Hành khách: {b.passengers?.length ?? 0} người
                  {b.passengers && b.passengers.length > 0
                    ? ` (${b.passengers.map((p) => p.fullName).join(", ")})`
                    : ""}
                </p>
              </div>

              <div className="flex items-center justify-between md:flex-col md:items-end gap-2">
                <span className="text-xs text-stone-400">Tổng thanh toán</span>
                <span className="font-extrabold text-2xl text-[#E2603F]">
                  {formatCurrency(b.totalAmount)}
                </span>
              </div>
            </div>

            {/* Actions */}
            <div className="pt-3 border-t border-[#f5f0e6] flex flex-wrap items-center justify-between gap-2">
              <button
                onClick={() => setSelectedBooking(b)}
                className="text-xs font-semibold text-[#E2603F] hover:text-[#d55333] underline"
              >
                Xem Chi Tiết Đơn Hàng
              </button>

              <div className="flex items-center gap-2">
                {b.status === "PENDING" && (
                  <Link
                    href={`/checkout/${b.bookingId}`}
                    className="inline-flex items-center gap-1.5 bg-[#E2603F] hover:bg-[#d55333] text-white font-bold text-xs px-4 py-2 rounded-xl shadow-md"
                  >
                    <CreditCard size={14} /> Thanh Toán Ngay
                  </Link>
                )}

                {b.status !== "CANCELLED" && (
                  <button
                    onClick={() => { setSelectedBooking(b); setShowCancelModal(true); }}
                    className="text-xs font-medium text-rose-600 hover:text-rose-700 bg-rose-50 px-3 py-1.5 rounded-lg border border-rose-200 transition-colors"
                  >
                    Yêu Cầu Hủy Tour
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Cancel Modal */}
      {showCancelModal && selectedBooking && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 space-y-5 shadow-2xl">
            <div className="flex items-center gap-3 text-rose-600">
              <XCircle size={24} />
              <h3 className="text-lg font-bold text-slate-900">Xác Nhận Hủy Đơn Tour {selectedBooking.bookingCode}</h3>
            </div>

            <p className="text-xs text-slate-600">
              Quý khách vui lòng nhập lý do hủy. Số chỗ sẽ được hoàn lại hệ thống và áp dụng chính sách hủy tour hiện hành.
            </p>

            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Nhập lý do hủy (ví dụ: Thay đổi lịch công tác đột xuất...)"
              rows={3}
              className="w-full text-xs p-3 border border-slate-200 rounded-xl focus:outline-none focus:border-rose-500"
            />

            <div className="flex gap-3 pt-2">
              <button
                onClick={() => setShowCancelModal(false)}
                className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-xl"
              >
                Đóng
              </button>
              <button
                disabled={isCancelling || !cancelReason.trim()}
                onClick={handleConfirmCancel}
                className="flex-1 py-2.5 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-xl shadow-md disabled:opacity-50"
              >
                {isCancelling ? "Đang xử lý..." : "Xác Nhận Hủy"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
