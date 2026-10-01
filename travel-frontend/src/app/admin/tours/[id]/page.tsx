"use client";

import { use, useEffect, useState } from "react";
import { Plus } from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { TourDetailVm, TourStatus } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { Button } from "@/components/ui/Button";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";

export default function AdminTourDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { notify } = useToast();
  const [tour, setTour] = useState<TourDetailVm | null>(null);
  const [status, setStatus] = useState<TourStatus>("DRAFT");
  const [savingStatus, setSavingStatus] = useState(false);
  const [showScheduleForm, setShowScheduleForm] = useState(false);
  const [schedule, setSchedule] = useState({
    departureTime: "",
    arrivalTime: "",
    priceAdult: "",
    priceChild: "",
    totalSeats: "",
  });
  const [savingSchedule, setSavingSchedule] = useState(false);

  function load() {
    tourService.getById(id).then((data) => {
      setTour(data);
      setStatus(data.status);
    });
  }

  useEffect(load, [id]);

  async function handleStatusChange(newStatus: TourStatus) {
    if (!tour) return;
    setSavingStatus(true);
    try {
      await tourService.update(id, {
        title: tour.title,
        description: tour.description,
        status: newStatus,
        destinationIds: tour.destinations.map((d) => d.id),
      });
      setStatus(newStatus);
      notify("success", "Đã cập nhật trạng thái tour.");
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
    } finally {
      setSavingStatus(false);
    }
  }

  async function handleAddSchedule(e: React.FormEvent) {
    e.preventDefault();
    setSavingSchedule(true);
    try {
      await tourService.addSchedule(id, {
        departureTime: schedule.departureTime,
        arrivalTime: schedule.arrivalTime,
        priceAdult: Number(schedule.priceAdult),
        priceChild: Number(schedule.priceChild),
        totalSeats: Number(schedule.totalSeats),
      });
      notify("success", "Đã thêm lịch khởi hành.");
      setShowScheduleForm(false);
      setSchedule({ departureTime: "", arrivalTime: "", priceAdult: "", priceChild: "", totalSeats: "" });
      load();
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
    } finally {
      setSavingSchedule(false);
    }
  }

  if (!tour) return <p className="text-ink-soft">Đang tải...</p>;

  return (
    <div className="max-w-2xl">
      <div className="flex items-center gap-3 mb-1">
        <span className="font-mono text-xs text-ink-soft">{tour.code}</span>
        <StatusBadge status={status} />
      </div>
      <h1 className="font-display text-2xl mb-4">{tour.title}</h1>
      <p className="text-sm text-ink-soft mb-6">{tour.description}</p>

      <div className="flex gap-2 mb-8">
        {(["DRAFT", "PUBLISHED", "ARCHIVED"] as TourStatus[]).map((s) => (
          <button
            key={s}
            disabled={savingStatus || s === status}
            onClick={() => handleStatusChange(s)}
            className={`text-xs rounded-full px-3 py-1.5 border transition-colors disabled:opacity-40 ${
              s === status ? "border-teal bg-teal-light text-teal" : "border-line text-ink-soft hover:border-teal"
            }`}
          >
            {s === "DRAFT" ? "Bản nháp" : s === "PUBLISHED" ? "Mở bán" : "Ngừng bán"}
          </button>
        ))}
      </div>

      <div className="flex items-center justify-between mb-3">
        <h2 className="font-medium">Lịch khởi hành</h2>
        <button
          onClick={() => setShowScheduleForm((v) => !v)}
          className="flex items-center gap-1 text-xs text-teal"
        >
          <Plus size={13} /> Thêm lịch
        </button>
      </div>

      {showScheduleForm && (
        <form onSubmit={handleAddSchedule} className="rounded-xl border border-line p-4 mb-4 space-y-3">
          <div className="grid sm:grid-cols-2 gap-3">
            <div>
              <label className="text-xs text-ink-soft mb-1 block">Ngày giờ khởi hành</label>
              <input
                required
                type="datetime-local"
                value={schedule.departureTime}
                onChange={(e) => setSchedule({ ...schedule, departureTime: e.target.value })}
                className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
              />
            </div>
            <div>
              <label className="text-xs text-ink-soft mb-1 block">Ngày giờ kết thúc</label>
              <input
                required
                type="datetime-local"
                value={schedule.arrivalTime}
                onChange={(e) => setSchedule({ ...schedule, arrivalTime: e.target.value })}
                className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
              />
            </div>
            <div>
              <label className="text-xs text-ink-soft mb-1 block">Giá người lớn</label>
              <input
                required
                type="number"
                value={schedule.priceAdult}
                onChange={(e) => setSchedule({ ...schedule, priceAdult: e.target.value })}
                className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
              />
            </div>
            <div>
              <label className="text-xs text-ink-soft mb-1 block">Giá trẻ em</label>
              <input
                required
                type="number"
                value={schedule.priceChild}
                onChange={(e) => setSchedule({ ...schedule, priceChild: e.target.value })}
                className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
              />
            </div>
            <div>
              <label className="text-xs text-ink-soft mb-1 block">Tổng số chỗ</label>
              <input
                required
                type="number"
                value={schedule.totalSeats}
                onChange={(e) => setSchedule({ ...schedule, totalSeats: e.target.value })}
                className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
              />
            </div>
          </div>
          <Button type="submit" loading={savingSchedule}>
            Lưu lịch khởi hành
          </Button>
        </form>
      )}

      <div className="space-y-2">
        {tour.schedules.map((s) => (
          <div key={s.id} className="rounded-xl border border-line p-3 flex items-center justify-between text-sm">
            <span>{formatDateTime(s.departureTime)}</span>
            <span className="text-ink-soft">
              {s.availableSeats}/{s.totalSeats} chỗ
            </span>
            <span className="text-coral">{formatCurrency(s.priceAdult)}</span>
          </div>
        ))}
        {tour.schedules.length === 0 && (
          <p className="text-sm text-ink-soft">Chưa có lịch khởi hành nào.</p>
        )}
      </div>
    </div>
  );
}
