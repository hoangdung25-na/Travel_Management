"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { ArrowLeft, Users } from "lucide-react";
import { bookingService } from "@/lib/services/booking-service";
import { BookingDetailVm } from "@/lib/types";
import { formatCurrency, formatDate } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Button } from "@/components/ui/Button";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";

const PASSENGER_TYPE_LABEL: Record<string, string> = {
  ADULT: "Người lớn",
  CHILD: "Trẻ em",
  INFANT: "Em bé",
};

export default function BookingDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { notify } = useToast();
  const [booking, setBooking] = useState<BookingDetailVm | null>(null);
  const [cancelling, setCancelling] = useState(false);
  const [showCancelForm, setShowCancelForm] = useState(false);
  const [reason, setReason] = useState("");

  function load() {
    bookingService.getById(id).then(setBooking).catch(() => setBooking(null));
  }

  useEffect(load, [id]);

  async function handleCancel() {
    if (!reason.trim()) {
      notify("error", "Vui lòng nhập lý do hủy đơn.");
      return;
    }
    setCancelling(true);
    try {
      await bookingService.cancel(id, { reason });
      notify("success", "Đã gửi yêu cầu hủy đơn.");
      load();
      setShowCancelForm(false);
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể hủy đơn, vui lòng thử lại.");
    } finally {
      setCancelling(false);
    }
  }

  if (!booking) {
    return <div className="mx-auto max-w-2xl px-4 py-24 text-center text-ink-soft">Đang tải...</div>;
  }

  const canCancel = booking.status === "CONFIRMED" || booking.status === "PENDING";
  const showTicket = booking.status === "CONFIRMED";

  return (
    <div className="mx-auto max-w-2xl px-4 sm:px-6 py-10">
      <Link href="/account/bookings" className="inline-flex items-center gap-1.5 text-sm text-ink-soft hover:text-teal mb-6">
        <ArrowLeft size={15} /> Đơn đặt tour của tôi
      </Link>

      <div className="flex items-center gap-3 mb-1">
        <h1 className="font-display text-2xl">{booking.bookingCode}</h1>
        <StatusBadge status={booking.status} />
      </div>
      <p className="text-sm text-ink-soft mb-8">Đặt ngày {formatDate(booking.createdAt)}</p>

      {booking.status === "PAYMENT_PENDING" && (
        <Link
          href={`/checkout/${booking.bookingId}`}
          className="block rounded-2xl bg-gold-light text-gold p-4 text-sm mb-6 hover:bg-gold-light/70"
        >
          Đơn này đang chờ thanh toán — bấm để tiếp tục thanh toán.
        </Link>
      )}

      {showTicket && (
        <div className="ticket-stub relative rounded-2xl border border-line bg-white overflow-hidden mb-6 pr-32">
          <div className="p-5 flex items-center gap-4">
            <Image
              src={`https://api.qrserver.com/v1/create-qr-code/?size=96x96&data=${encodeURIComponent(
                booking.bookingCode
              )}`}
              alt="Mã QR check-in"
              width={72}
              height={72}
              unoptimized
              className="rounded-lg border border-line shrink-0"
            />
            <div>
              <p className="text-xs text-ink-soft mb-1">Mã check-in</p>
              <p className="font-mono text-lg">{booking.bookingCode}</p>
            </div>
          </div>
          <div className="ticket-notch" />
        </div>
      )}

      <div className="rounded-2xl border border-line bg-white p-5 mb-6">
        <p className="text-sm font-medium mb-4 flex items-center gap-2">
          <Users size={15} className="text-teal" /> Hành khách ({booking.passengers?.length ?? 0})
        </p>
        <div className="space-y-3">
          {(booking.passengers || []).map((p) => (
            <div key={p.id} className="flex items-center justify-between text-sm border-b border-line last:border-0 pb-3 last:pb-0">
              <div>
                <p className="font-medium">{p.fullName}</p>
                <p className="text-xs text-ink-soft">
                  {PASSENGER_TYPE_LABEL[p.passengerType]}
                  {p.idCardNumber ? ` · ${p.idCardNumber}` : ""}
                </p>
              </div>
              <span className="text-ink-soft">{formatCurrency(p.price)}</span>
            </div>
          ))}
        </div>
        <div className="flex justify-between items-center mt-4 pt-4 border-t border-line">
          <span className="text-ink-soft text-sm">Tổng cộng</span>
          <span className="font-display text-xl text-coral">{formatCurrency(booking.totalAmount)}</span>
        </div>
      </div>

      {canCancel && !showCancelForm && (
        <Button variant="danger" onClick={() => setShowCancelForm(true)}>
          Yêu cầu hủy đơn
        </Button>
      )}

      {showCancelForm && (
        <div className="rounded-2xl border border-coral/30 bg-coral/5 p-5">
          <p className="text-sm font-medium mb-3">Lý do hủy đơn</p>
          <textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-coral mb-3"
            placeholder="Vui lòng cho biết lý do bạn muốn hủy đơn..."
          />
          <div className="flex gap-2">
            <Button variant="danger" loading={cancelling} onClick={handleCancel}>
              Xác nhận hủy
            </Button>
            <Button variant="ghost" onClick={() => setShowCancelForm(false)}>
              Đóng
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
