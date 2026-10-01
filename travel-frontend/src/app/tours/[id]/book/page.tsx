"use client";

import { use, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Plus, Trash2, ArrowLeft } from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { bookingService } from "@/lib/services/booking-service";
import { TourDetailVm, TourScheduleVm, PassengerRequest, PassengerType } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/lib/auth-context";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";

const PASSENGER_TYPE_LABEL: Record<PassengerType, string> = {
  ADULT: "Người lớn",
  CHILD: "Trẻ em",
  INFANT: "Em bé",
};

function emptyPassenger(type: PassengerType): PassengerRequest {
  return { fullName: "", dateOfBirth: "", passengerType: type, idCardNumber: "" };
}

export default function BookTourPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const searchParams = useSearchParams();
  const scheduleId = searchParams.get("scheduleId");
  const router = useRouter();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const { notify } = useToast();

  const [tour, setTour] = useState<TourDetailVm | null>(null);
  const [schedule, setSchedule] = useState<TourScheduleVm | null>(null);
  const [passengers, setPassengers] = useState<PassengerRequest[]>([emptyPassenger("ADULT")]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push(`/login?redirect=/tours/${id}/book?scheduleId=${scheduleId}`);
    }
  }, [authLoading, isAuthenticated, id, scheduleId, router]);

  useEffect(() => {
    tourService.getById(id).then((data) => {
      setTour(data);
      setSchedule(data.schedules.find((s) => s.id === scheduleId) ?? null);
    });
  }, [id, scheduleId]);

  function updatePassenger(index: number, patch: Partial<PassengerRequest>) {
    setPassengers((prev) => prev.map((p, i) => (i === index ? { ...p, ...patch } : p)));
  }

  function addPassenger(type: PassengerType) {
    setPassengers((prev) => [...prev, emptyPassenger(type)]);
  }

  function removePassenger(index: number) {
    setPassengers((prev) => prev.filter((_, i) => i !== index));
  }

  const totalAmount = schedule
    ? passengers.reduce(
        (sum, p) => sum + (p.passengerType === "CHILD" ? schedule.priceChild : schedule.priceAdult),
        0
      )
    : 0;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!schedule) return;
    if (passengers.some((p) => !p.fullName.trim())) {
      notify("error", "Vui lòng nhập họ tên cho tất cả hành khách.");
      return;
    }
    setSubmitting(true);
    try {
      const booking = await bookingService.create({
        tourScheduleId: schedule.id,
        passengers,
      });
      notify("success", "Đã giữ chỗ thành công! Vui lòng thanh toán trong 15 phút.");
      router.push(`/checkout/${booking.bookingId}`);
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể tạo đơn đặt tour, vui lòng thử lại.");
    } finally {
      setSubmitting(false);
    }
  }

  if (!tour || !schedule) {
    return <div className="mx-auto max-w-3xl px-4 py-24 text-center text-ink-soft">Đang tải...</div>;
  }

  return (
    <div className="mx-auto max-w-3xl px-4 sm:px-6 py-10">
      <button
        onClick={() => router.back()}
        className="inline-flex items-center gap-1.5 text-sm text-ink-soft hover:text-teal mb-6"
      >
        <ArrowLeft size={15} /> Quay lại
      </button>

      <p className="text-xs uppercase tracking-[0.2em] text-coral mb-2">Đặt tour</p>
      <h1 className="font-display text-2xl md:text-3xl mb-1">{tour.title}</h1>
      <p className="text-sm text-ink-soft mb-8">
        Khởi hành {formatDateTime(schedule.departureTime)} · Còn {schedule.availableSeats} chỗ
      </p>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-4">
          {passengers.map((p, idx) => (
            <div key={idx} className="rounded-2xl border border-line bg-white p-4">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium">
                  Hành khách {idx + 1} · {PASSENGER_TYPE_LABEL[p.passengerType]}
                </span>
                {passengers.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removePassenger(idx)}
                    className="text-coral/70 hover:text-coral"
                  >
                    <Trash2 size={15} />
                  </button>
                )}
              </div>
              <div className="grid sm:grid-cols-2 gap-3">
                <input
                  required
                  value={p.fullName}
                  onChange={(e) => updatePassenger(idx, { fullName: e.target.value })}
                  placeholder="Họ và tên"
                  className="rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal sm:col-span-2"
                />
                <input
                  type="date"
                  value={p.dateOfBirth}
                  onChange={(e) => updatePassenger(idx, { dateOfBirth: e.target.value })}
                  className="rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
                />
                <input
                  value={p.idCardNumber}
                  onChange={(e) => updatePassenger(idx, { idCardNumber: e.target.value })}
                  placeholder="Số CCCD / Hộ chiếu"
                  className="rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
                />
                <select
                  value={p.passengerType}
                  onChange={(e) =>
                    updatePassenger(idx, { passengerType: e.target.value as PassengerType })
                  }
                  className="rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal sm:col-span-2"
                >
                  <option value="ADULT">Người lớn</option>
                  <option value="CHILD">Trẻ em</option>
                  <option value="INFANT">Em bé</option>
                </select>
              </div>
            </div>
          ))}
        </div>

        <button
          type="button"
          onClick={() => addPassenger("ADULT")}
          className="flex items-center gap-1.5 text-sm text-teal hover:text-coral"
        >
          <Plus size={15} /> Thêm hành khách
        </button>

        <div className="rounded-2xl border border-line bg-white p-5">
          <div className="flex justify-between items-center">
            <span className="text-ink-soft text-sm">Tổng cộng ({passengers.length} khách)</span>
            <span className="font-display text-2xl text-coral">{formatCurrency(totalAmount)}</span>
          </div>
        </div>

        <Button type="submit" className="w-full" loading={submitting}>
          Giữ chỗ &amp; tiếp tục thanh toán
        </Button>
      </form>
    </div>
  );
}
