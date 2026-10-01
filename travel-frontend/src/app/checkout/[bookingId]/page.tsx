"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import {
  Timer,
  ShieldCheck,
  CreditCard,
  CheckCircle2,
  AlertCircle,
  Ticket,
  UserCheck,
  ArrowLeft,
  Sparkles,
} from "lucide-react";
import { bookingService } from "@/lib/services/booking-service";
import { paymentService } from "@/lib/services/payment-service";
import { BookingDetailVm, PaymentMethod } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { useCountdown } from "@/lib/use-countdown";
import { useToast } from "@/lib/toast-context";

const PAYMENT_METHODS: { value: PaymentMethod; label: string; hint: string; iconColor: string }[] = [
  { value: "VNPAY", label: "VNPAY QR & Thẻ Nội Địa", hint: "Quét mã VNPAY QR hoặc dùng thẻ ATM 40 ngân hàng", iconColor: "text-blue-500" },
  { value: "MOMO", label: "Ví Điện Tử MoMo", hint: "Thanh toán siêu tốc 1 chạm qua ứng dụng MoMo", iconColor: "text-pink-500" },
  { value: "STRIPE", label: "Thẻ Quốc Tế Visa / Mastercard", hint: "Mã hóa SSL 256-bit chuẩn bảo mật PCI-DSS", iconColor: "text-purple-500" },
];

export default function CheckoutPage({ params }: { params: Promise<{ bookingId: string }> }) {
  const { bookingId } = use(params);
  const { notify } = useToast();
  const [booking, setBooking] = useState<BookingDetailVm | null>(null);
  const [method, setMethod] = useState<PaymentMethod>("VNPAY");
  const [isProcessing, setIsProcessing] = useState(false);
  const [paymentSuccessModal, setPaymentSuccessModal] = useState(false);

  const { minutes, seconds, expired } = useCountdown(booking?.createdAt ?? null);

  useEffect(() => {
    bookingService.getById(bookingId).then(setBooking).catch(() => setBooking(null));
  }, [bookingId]);

  async function handleProcessPayment() {
    if (!booking) return;
    setIsProcessing(true);
    try {
      const paymentInfo = await paymentService.createUrl({
        bookingId,
        paymentMethod: method,
        amount: booking.totalAmount,
      });

      if (paymentInfo.paymentUrl && paymentInfo.paymentUrl.startsWith("http") && !paymentInfo.paymentUrl.includes(window.location.host)) {
        window.location.href = paymentInfo.paymentUrl;
        return;
      }

      const updated = await bookingService.getById(bookingId);
      setBooking(updated);
      setPaymentSuccessModal(true);
      notify("success", `Thanh toán qua cổng ${method} ghi nhận thành công!`);
    } catch (err: any) {
      notify("error", err.message || "Kết nối cổng thanh toán thất bại!");
    } finally {
      setIsProcessing(false);
    }
  }

  if (!booking) {
    return (
      <div className="mx-auto max-w-xl px-4 py-24 text-center space-y-4">
        <div className="w-10 h-10 border-4 border-teal-600 border-t-transparent rounded-full animate-spin mx-auto" />
        <p className="text-slate-500 font-medium">Đang tải thông tin đơn giữ chỗ...</p>
      </div>
    );
  }

  if (booking.status === "CONFIRMED" && !paymentSuccessModal) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-center space-y-6">
        <div className="w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center mx-auto shadow-md">
          <CheckCircle2 size={36} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-extrabold text-slate-900">Đơn Hàng Đã Được Xác Nhận!</h1>
          <p className="text-slate-500 text-sm">
            Mã vé điện tử của bạn: <strong className="text-slate-800">{booking.bookingCode}</strong>
          </p>
        </div>
        <div className="pt-2 flex justify-center gap-4">
          <Link
            href="/account/bookings"
            className="bg-teal-600 hover:bg-teal-700 text-white font-semibold text-sm px-6 py-3 rounded-xl shadow-md transition-all"
          >
            Quản Lý Đơn Hàng Của Tôi
          </Link>
        </div>
      </div>
    );
  }

  if (booking.status === "CANCELLED" || expired) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-center space-y-6">
        <div className="w-16 h-16 bg-rose-100 text-rose-600 rounded-full flex items-center justify-center mx-auto shadow-md">
          <AlertCircle size={36} />
        </div>
        <div className="space-y-2">
          <h1 className="text-2xl font-extrabold text-slate-900">
            {expired ? "Đã Hết Thời Gian Giữ Chỗ 15 Phút" : "Đơn Đặt Tour Đã Bị Hủy"}
          </h1>
          <p className="text-slate-500 text-sm max-w-md mx-auto">
            Hệ thống Saga Saga Coordinator đã tự động hoàn trả lại số chỗ cho cộng đồng. Vui lòng chọn lại lịch đi khác.
          </p>
        </div>
        <Link
          href="/tours"
          className="inline-flex items-center gap-2 bg-slate-900 text-white font-semibold text-sm px-6 py-3 rounded-xl hover:bg-slate-800 transition-all"
        >
          <ArrowLeft size={16} /> Chọn Tour Khác
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl px-4 sm:px-6 py-10 space-y-8">
      {/* Top Countdown Header */}
      <div className="rounded-3xl bg-[#0D2B2B] text-white p-6 sm:p-8 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl border border-[#123A3A]">
        <div className="space-y-1 text-center sm:text-left">
          <span className="text-xs uppercase tracking-widest text-[#D4A24C] font-bold">
            Saga Hold Coordinator
          </span>
          <h1 className="text-2xl font-extrabold tracking-tight">
            Thanh Toán Đơn Tour {booking.bookingCode}
          </h1>
        </div>

        {/* 15-min Hold Countdown Badge */}
        <div className="flex items-center gap-3 bg-[#D4A24C]/20 border border-[#D4A24C]/40 text-[#D4A24C] px-4 py-2.5 rounded-2xl backdrop-blur-md">
          <Timer size={20} className="animate-spin text-[#D4A24C]" />
          <div className="text-left">
            <p className="text-[10px] uppercase font-bold tracking-wider text-[#D4A24C]">Thời gian giữ chỗ</p>
            <p className="font-mono font-extrabold text-lg leading-none">
              {minutes !== null ? `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}` : "14:59"}
            </p>
          </div>
        </div>
      </div>

      {/* Tour Summary & Passenger List */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-6">
        <h2 className="text-lg font-bold text-stone-900 border-b border-[#f5f0e6] pb-3 flex items-center gap-2">
          <Ticket size={20} className="text-[#E2603F]" /> Chi Tiết Đơn Hàng & Hành Khách
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs font-medium text-stone-600 bg-[#FBF8F2] p-4 rounded-2xl border border-[#e7e0d3]">
          <div>
            <span className="text-stone-400 block text-[11px]">Tên Tour:</span>
            <span className="font-bold text-stone-800 text-sm">{booking.tourTitle}</span>
          </div>
          <div>
            <span className="text-stone-400 block text-[11px]">Lịch Khởi Hành:</span>
            <span className="font-bold text-stone-800">{formatDateTime(booking.departureTime || booking.createdAt)}</span>
          </div>
        </div>

        {/* Passenger Roster */}
        <div className="space-y-2">
          <span className="text-xs font-bold text-stone-700 block">Danh Sách Hành Khách:</span>
          <div className="space-y-2">
            {(booking.passengers || []).map((p, idx) => (
              <div key={p.id || idx} className="flex items-center justify-between text-xs p-3 bg-[#FBF8F2] rounded-xl border border-[#e7e0d3]">
                <div className="flex items-center gap-2">
                  <UserCheck size={14} className="text-[#E2603F]" />
                  <span className="font-bold text-stone-800">{p.fullName}</span>
                  <span className="bg-[#e7e0d3] text-stone-700 text-[10px] px-2 py-0.5 rounded font-semibold">
                    {p.passengerType === "ADULT" ? "Người lớn" : "Trẻ em"}
                  </span>
                </div>
                <span className="font-bold text-stone-700">{formatCurrency(p.price)}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Total Payment Amount */}
        <div className="pt-4 border-t border-[#f5f0e6] flex items-baseline justify-between">
          <span className="text-sm font-semibold text-stone-600">Tổng Số Tiền Thanh Toán:</span>
          <span className="text-3xl font-extrabold text-[#E2603F]">{formatCurrency(booking.totalAmount)}</span>
        </div>
      </div>

      {/* Payment Gateway Selector */}
      <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-6">
        <h2 className="text-lg font-bold text-stone-900 border-b border-[#f5f0e6] pb-3 flex items-center gap-2">
          <CreditCard size={20} className="text-[#E2603F]" /> Chọn Phương Thức Thanh Toán
        </h2>

        <div className="space-y-3">
          {PAYMENT_METHODS.map((m) => (
            <button
              key={m.value}
              onClick={() => setMethod(m.value)}
              className={`w-full text-left p-4 rounded-2xl border transition-all flex items-center gap-4 ${
                method === m.value
                  ? "border-[#E2603F] bg-[#E2603F]/10 shadow-sm"
                  : "border-[#e7e0d3] hover:border-[#E2603F]/40"
              }`}
            >
              <div className={`p-3 rounded-xl bg-white shadow-sm text-[#E2603F]`}>
                <CreditCard size={20} />
              </div>
              <div className="flex-1">
                <p className="text-sm font-bold text-stone-800">{m.label}</p>
                <p className="text-xs text-stone-500 mt-0.5">{m.hint}</p>
              </div>
            </button>
          ))}
        </div>

        {/* Action Button */}
        <div className="pt-4 space-y-3">
          <button
            disabled={isProcessing}
            onClick={handleProcessPayment}
            className="w-full bg-[#E2603F] hover:bg-[#d55333] text-white font-extrabold text-base py-4 rounded-2xl shadow-xl shadow-[#E2603F]/30 transition-all flex items-center justify-center gap-2"
          >
            {isProcessing ? (
              <span>Đang kết nối cổng {method}...</span>
            ) : (
              <>
                <ShieldCheck size={20} />
                <span>Thanh Toán Ngay {formatCurrency(booking.totalAmount)}</span>
              </>
            )}
          </button>

          <p className="text-center text-xs text-stone-400 flex items-center justify-center gap-1">
            <ShieldCheck size={14} className="text-[#E2603F]" /> Kết nối thanh toán an toàn 256-bit SSL chuẩn PCI-DSS qua Cổng VNPAY & Ví MoMo.
          </p>
        </div>
      </div>

      {/* Success Modal */}
      {paymentSuccessModal && (
        <div className="fixed inset-0 bg-[#0D2B2B]/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 text-center space-y-5 shadow-2xl animate-in fade-in zoom-in">
            <div className="w-16 h-16 bg-[#E2603F]/10 text-[#E2603F] rounded-full flex items-center justify-center mx-auto shadow-lg border border-[#E2603F]/20">
              <CheckCircle2 size={36} />
            </div>
            <div className="space-y-1">
              <h3 className="text-xl font-extrabold text-stone-900">Thanh Toán Thành Công!</h3>
              <p className="text-stone-500 text-xs">
                Mã đơn hàng: <strong className="text-stone-800">{booking.bookingCode}</strong>
              </p>
            </div>
            <p className="text-xs text-stone-600 bg-[#FBF8F2] p-3 rounded-xl border border-[#e7e0d3]">
              Cổng thanh toán {method} đã ghi nhận giao dịch. Trạng thái Booking đã được đổi sang <strong>CONFIRMED</strong>!
            </p>
            <div className="flex gap-3">
              <Link
                href="/account/bookings"
                className="flex-1 bg-[#E2603F] hover:bg-[#d55333] text-white font-semibold text-xs py-3 rounded-xl transition-all"
              >
                Xem Vé Của Tôi
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
