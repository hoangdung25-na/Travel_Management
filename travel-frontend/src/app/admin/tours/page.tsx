"use client";

import { useEffect, useState } from "react";
import {
  Plus,
  Calendar,
  MapPin,
  Clock,
  Tag,
  CheckCircle2,
  X,
  Edit,
  Eye,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { TourDetailVm, TourVm, DestinationVm } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function AdminToursPage() {
  const { notify } = useToast();
  const [tours, setTours] = useState<TourVm[]>([]);
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showScheduleModal, setShowScheduleModal] = useState<string | null>(null);

  // New Tour Form
  const [newTourForm, setNewTourForm] = useState({
    code: "",
    title: "",
    description: "",
    duration: "3 Ngày 2 Đêm",
    destinationId: "",
    day1Title: "Khám phá địa danh nổi tiếng",
    day1Content: "Xe và HDV đưa đón tham quan các danh thắng và thưởng thức đặc sản.",
  });

  // New Schedule Form
  const [newSchForm, setNewSchForm] = useState({
    departureTime: "2026-10-01T07:30",
    arrivalTime: "2026-10-03T18:00",
    priceAdult: "4500000",
    priceChild: "3150000",
    totalSeats: "30",
  });

  useEffect(() => {
    loadData();
  }, []);

  function loadData() {
    tourService.search({ size: 100 }).then((res) => setTours(res.content));
    tourService.listDestinations().then(setDestinations);
  }

  async function handleCreateTour(e: React.FormEvent) {
    e.preventDefault();
    try {
      await tourService.create({
        code: newTourForm.code || `TOUR-${Date.now().toString().slice(-4)}`,
        title: newTourForm.title,
        description: newTourForm.description,
        duration: newTourForm.duration,
        destinationIds: newTourForm.destinationId ? [newTourForm.destinationId] : [],
        itineraries: [
          {
            dayNumber: 1,
            title: newTourForm.day1Title,
            content: newTourForm.day1Content,
          },
        ],
      });
      notify("success", "Khởi tạo Tour mới thành công!");
      setShowCreateModal(false);
      loadData();
    } catch (err: any) {
      notify("error", err.message || "Tạo tour thất bại");
    }
  }

  async function handleAddSchedule(e: React.FormEvent) {
    e.preventDefault();
    if (!showScheduleModal) return;
    try {
      await tourService.addSchedule(showScheduleModal, {
        departureTime: new Date(newSchForm.departureTime).toISOString(),
        arrivalTime: new Date(newSchForm.arrivalTime).toISOString(),
        priceAdult: Number(newSchForm.priceAdult),
        priceChild: Number(newSchForm.priceChild),
        totalSeats: Number(newSchForm.totalSeats),
      });
      notify("success", "Đã thêm lịch khởi hành thành công!");
      setShowScheduleModal(null);
      loadData();
    } catch (err: any) {
      notify("error", err.message || "Thêm lịch thất bại");
    }
  }

  return (
    <div className="space-y-8">
      {/* Top Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-widest text-teal-600 font-bold block mb-1">
            Danh Mục Quản Trị
          </span>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Quản Lý Tour & Lịch Khởi Hành
          </h1>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-xl shadow-md transition-all shrink-0"
        >
          <Plus size={16} /> Tạo Tour Danh Mục Mới
        </button>
      </div>

      {/* Tour List Table */}
      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-100 uppercase tracking-wider">
              <tr>
                <th className="py-3.5 px-4">Mã Tour</th>
                <th className="py-3.5 px-4">Tên Tour Danh Mục</th>
                <th className="py-3.5 px-4">Điểm Đến</th>
                <th className="py-3.5 px-4">Giá Từ</th>
                <th className="py-3.5 px-4">Tổng Chỗ Khả Dụng</th>
                <th className="py-3.5 px-4">Trạng Thái</th>
                <th className="py-3.5 px-4 text-right">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {tours.length === 0 && (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-400">
                    Chưa có tour nào trong hệ thống.
                  </td>
                </tr>
              )}
              {tours.map((t) => (
                <tr key={t.tourId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-4 px-4 font-mono font-bold text-teal-600">{t.code}</td>
                  <td className="py-4 px-4 font-bold text-slate-900 max-w-xs">{t.title}</td>
                  <td className="py-4 px-4 text-slate-500">{t.destinationCity || "Việt Nam"}</td>
                  <td className="py-4 px-4 font-bold text-rose-600">{formatCurrency(t.minPrice)}</td>
                  <td className="py-4 px-4">
                    <span className="bg-slate-100 text-slate-700 px-2 py-1 rounded font-bold">
                      {t.availableSeats} chỗ
                    </span>
                  </td>
                  <td className="py-4 px-4">
                    <StatusBadge status={t.status} />
                  </td>
                  <td className="py-4 px-4 text-right space-x-2">
                    <button
                      onClick={() => setShowScheduleModal(t.tourId)}
                      className="bg-teal-50 text-teal-700 hover:bg-teal-100 font-bold px-3 py-1.5 rounded-lg border border-teal-200 transition-colors"
                    >
                      + Lịch Đi
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal 1: Create Tour */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 space-y-5 shadow-2xl animate-in fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-base">Thêm Tour Vào Danh Mục (POST /api/v1/tours)</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-slate-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleCreateTour} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Mã Tour Unique (Code)</label>
                <input
                  required
                  value={newTourForm.code}
                  onChange={(e) => setNewTourForm({ ...newTourForm, code: e.target.value })}
                  placeholder="TOUR-DN-3N2D"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Tên Tour Chi Tiết</label>
                <input
                  required
                  value={newTourForm.title}
                  onChange={(e) => setNewTourForm({ ...newTourForm, title: e.target.value })}
                  placeholder="Tour Khám Phá Đà Nẵng - Bà Nà Hills..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Mô Tả Tour</label>
                <textarea
                  required
                  value={newTourForm.description}
                  onChange={(e) => setNewTourForm({ ...newTourForm, description: e.target.value })}
                  placeholder="Mô tả hành trình, điểm đến nổi bật..."
                  rows={3}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Điểm Đến Hàng Đầu</label>
                <select
                  value={newTourForm.destinationId}
                  onChange={(e) => setNewTourForm({ ...newTourForm, destinationId: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                >
                  <option value="">-- Chọn điểm đến --</option>
                  {destinations.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.city} ({d.name})
                    </option>
                  ))}
                </select>
              </div>

              <div className="pt-3 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 py-2.5 bg-slate-100 text-slate-700 font-semibold rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md"
                >
                  Tạo Tour Mới
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 2: Add Departure Schedule */}
      {showScheduleModal && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 space-y-5 shadow-2xl animate-in fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-base">Thêm Lịch Khởi Hành & Hạn Mức Chỗ</h3>
              <button onClick={() => setShowScheduleModal(null)} className="text-slate-400 hover:text-slate-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleAddSchedule} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Thời Gian Khởi Hành</label>
                <input
                  type="datetime-local"
                  required
                  value={newSchForm.departureTime}
                  onChange={(e) => setNewSchForm({ ...newSchForm, departureTime: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Thời Gian Kết Thúc</label>
                <input
                  type="datetime-local"
                  required
                  value={newSchForm.arrivalTime}
                  onChange={(e) => setNewSchForm({ ...newSchForm, arrivalTime: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Giá Người Lớn</label>
                  <input
                    type="number"
                    required
                    value={newSchForm.priceAdult}
                    onChange={(e) => setNewSchForm({ ...newSchForm, priceAdult: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">Giá Trẻ Em</label>
                  <input
                    type="number"
                    required
                    value={newSchForm.priceChild}
                    onChange={(e) => setNewSchForm({ ...newSchForm, priceChild: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                  />
                </div>
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Hạn Mức Số Chỗ Khả Dụng (Total Seats)</label>
                <input
                  type="number"
                  required
                  value={newSchForm.totalSeats}
                  onChange={(e) => setNewSchForm({ ...newSchForm, totalSeats: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div className="pt-3 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowScheduleModal(null)}
                  className="flex-1 py-2.5 bg-slate-100 text-slate-700 font-semibold rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md"
                >
                  Lưu Lịch Đi
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
