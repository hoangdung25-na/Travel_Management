"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, Trash2 } from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { CreateItineraryRequest } from "@/lib/types";
import { Button } from "@/components/ui/Button";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";

export default function NewTourPage() {
  const router = useRouter();
  const { notify } = useToast();
  const [code, setCode] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [itineraries, setItineraries] = useState<CreateItineraryRequest[]>([
    { dayNumber: 1, title: "", content: "" },
  ]);
  const [saving, setSaving] = useState(false);

  function updateItinerary(idx: number, patch: Partial<CreateItineraryRequest>) {
    setItineraries((prev) => prev.map((it, i) => (i === idx ? { ...it, ...patch } : it)));
  }

  function addDay() {
    setItineraries((prev) => [...prev, { dayNumber: prev.length + 1, title: "", content: "" }]);
  }

  function removeDay(idx: number) {
    setItineraries((prev) => prev.filter((_, i) => i !== idx));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      const tour = await tourService.create({ code, title, description, itineraries });
      notify("success", "Đã tạo tour mới.");
      router.push(`/admin/tours/${tour.tourId}`);
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể tạo tour, vui lòng thử lại.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="font-display text-2xl mb-6">Tạo tour mới</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid sm:grid-cols-2 gap-3">
          <div>
            <label className="text-xs text-ink-soft mb-1 block">Mã tour (VD: DL-DALAT-01)</label>
            <input
              required
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              pattern="^[A-Z0-9-]+$"
              className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
            />
          </div>
          <div>
            <label className="text-xs text-ink-soft mb-1 block">Tiêu đề (tối thiểu 10 ký tự)</label>
            <input
              required
              minLength={10}
              maxLength={250}
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
            />
          </div>
        </div>

        <div>
          <label className="text-xs text-ink-soft mb-1 block">Mô tả</label>
          <textarea
            required
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={4}
            className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
          />
        </div>

        <div>
          <div className="flex items-center justify-between mb-2">
            <label className="text-sm font-medium">Lịch trình chi tiết</label>
            <button type="button" onClick={addDay} className="flex items-center gap-1 text-xs text-teal">
              <Plus size={13} /> Thêm ngày
            </button>
          </div>
          <div className="space-y-3">
            {itineraries.map((it, idx) => (
              <div key={idx} className="rounded-xl border border-line p-3">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs text-ink-soft">Ngày {it.dayNumber}</span>
                  {itineraries.length > 1 && (
                    <button type="button" onClick={() => removeDay(idx)} className="text-coral/70">
                      <Trash2 size={13} />
                    </button>
                  )}
                </div>
                <input
                  required
                  minLength={3}
                  value={it.title}
                  onChange={(e) => updateItinerary(idx, { title: e.target.value })}
                  placeholder="Tiêu đề ngày (VD: Khám phá thác Datanla)"
                  className="w-full rounded-lg border border-line px-3 py-2 text-sm mb-2 focus:outline-none focus:border-teal"
                />
                <textarea
                  required
                  value={it.content}
                  onChange={(e) => updateItinerary(idx, { content: e.target.value })}
                  rows={2}
                  placeholder="Nội dung chi tiết trong ngày..."
                  className="w-full rounded-lg border border-line px-3 py-2 text-sm focus:outline-none focus:border-teal"
                />
              </div>
            ))}
          </div>
        </div>

        <Button type="submit" loading={saving}>
          Tạo tour
        </Button>
      </form>
    </div>
  );
}
