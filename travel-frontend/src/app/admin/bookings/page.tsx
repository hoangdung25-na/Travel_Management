"use client";

import { useEffect, useState } from "react";
import { Search, Ticket, CreditCard, RefreshCcw, ShieldCheck, XCircle } from "lucide-react";
import { bookingService } from "@/lib/services/booking-service";
import { paymentService } from "@/lib/services/payment-service";
import { BookingDetailVm, PaymentVm } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function AdminBookingsPage() {
  const { notify } = useToast();
  const [bookings, setBookings] = useState<BookingDetailVm[]>([]);
  const [searchKw, setSearchKw] = useState("");
  const [selectedBooking, setSelectedBooking] = useState<BookingDetailVm | null>(null);
  const [refundReason, setRefundReason] = useState("");
  const [isRefunding, setIsRefunding] = useState(false);

  useEffect(() => {
    loadBookings();
  }, []);

  function loadBookings() {
    bookingService.getAllBookings().then(setBookings);
  }

  async function handleRefundSimulation() {
    if (!selectedBooking || !refundReason.trim()) return;
    setIsRefunding(true);
    try {
      await paymentService.refund(selectedBooking.bookingId, { reason: refundReason });
      const updated = await bookingService.cancel(selectedBooking.bookingId, { reason: `Hoàn tiền Admin: ${refundReason}` });
      notify("success", "Đã xử lý hoàn tiền và cập nhật trạng thái đơn hàng!");
      setSelectedBooking(updated);
      setRefundReason("");
      loadBookings();
    } catch (err: any) {
      notify("error", err.message || "Lỗi xử lý hoàn tiền");
    } finally {
      setIsRefunding(false);
    }
  }

  const filteredBookings = bookings.filter(
    (b) =>
      b.bookingCode.toLowerCase().includes(searchKw.toLowerCase()) ||
      b.tourTitle?.toLowerCase().includes(searchKw.toLowerCase()) ||
      b.userEmail?.toLowerCase().includes(searchKw.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-widest text-teal-600 font-bold block mb-1">
            Quản Lý Đơn Hàng & Giao Dịch
          </span>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Tất Cả Đơn Đặt Tour Trên Hệ Thống
          </h1>
        </div>

        <div className="relative">
          <Search size={16} className="absolute left-3 top-3 text-slate-400" />
          <input
            value={searchKw}
            onChange={(e) => setSearchKw(e.target.value)}
            placeholder="Tìm theo mã đơn, email..."
            className="pl-9 pr-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 w-64 shadow-sm"
          />
        </div>
      </div>

      {/* Bookings Table */}
      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-100 uppercase tracking-wider">
              <tr>
                <th className="py-3.5 px-4">Mã Đơn</th>
                <th className="py-3.5 px-4">Tài Khoản Đặt</th>
                <th className="py-3.5 px-4">Tour</th>
                <th className="py-3.5 px-4">Số Tiền</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
                <th className="py-3.5 px-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {filteredBookings.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    Không tìm thấy đơn hàng nào.
                  </td>
                </tr>
              )}
              {filteredBookings.map((b) => (
                <tr key={b.bookingId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-4 px-4 font-mono font-bold text-teal-600">{b.bookingCode}</td>
                  <td className="py-4 px-4">{b.userEmail || b.userId}</td>
                  <td className="py-4 px-4 font-bold text-slate-900 max-w-xs truncate">{b.tourTitle}</td>
                  <td className="py-4 px-4 font-extrabold text-rose-600">{formatCurrency(b.totalAmount)}</td>
                  <td className="py-4 px-4">
                    <StatusBadge status={b.status} />
                  </td>
                  <td className="py-4 px-4 text-right">
                    <button
                      onClick={() => setSelectedBooking(b)}
                      className="bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold px-3 py-1.5 rounded-lg border border-slate-200 transition-colors"
                    >
                      Chi Tiết & Hoàn Tiền
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Booking Detail Modal & Refund Dialog */}
      {selectedBooking && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 space-y-5 shadow-2xl animate-in fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-base">Đơn Hàng {selectedBooking.bookingCode}</h3>
              <button onClick={() => setSelectedBooking(null)} className="text-slate-400 hover:text-slate-600">
                <XCircle size={18} />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div className="p-3 bg-slate-50 rounded-xl space-y-1">
                <p><strong className="text-slate-700">Khách Hàng:</strong> {selectedBooking.userEmail || selectedBooking.userId}</p>
                <p><strong className="text-slate-700">Tour:</strong> {selectedBooking.tourTitle}</p>
                <p><strong className="text-slate-700">Tổng Số Tiền:</strong> <span className="font-bold text-rose-600">{formatCurrency(selectedBooking.totalAmount)}</span></p>
                <p><strong className="text-slate-700">Trạng Thái:</strong> {selectedBooking.status}</p>
              </div>

              {selectedBooking.status === "CONFIRMED" && (
                <div className="p-4 bg-rose-50 border border-rose-200 rounded-2xl space-y-3">
                  <h4 className="font-bold text-rose-900 flex items-center gap-1.5">
                    <RefreshCcw size={14} /> Hoàn Tiền Giao Dịch (Refund API)
                  </h4>
                  <input
                    value={refundReason}
                    onChange={(e) => setRefundReason(e.target.value)}
                    placeholder="Nhập lý do hoàn tiền..."
                    className="w-full p-2.5 bg-white border border-rose-200 rounded-xl focus:outline-none focus:border-rose-500"
                  />
                  <button
                    disabled={isRefunding || !refundReason.trim()}
                    onClick={handleRefundSimulation}
                    className="w-full py-2.5 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl shadow-md disabled:opacity-50"
                  >
                    {isRefunding ? "Đang xử lý hoàn tiền..." : "Xác Nhận Xử Lý Hoàn Tiền"}
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
