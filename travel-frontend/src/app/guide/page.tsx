"use client";

import { useEffect, useState } from "react";
import {
  ShieldCheck,
  Users,
  CalendarDays,
  CheckCircle2,
  XCircle,
  PhoneCall,
  Search,
  Bell,
} from "lucide-react";
import { guideService } from "@/lib/services/guide-service";
import { GuideAssignedTourVm } from "@/lib/types";
import { formatDateTime } from "@/lib/format";
import { useToast } from "@/lib/toast-context";

export default function GuideDashboardPage() {
  const { notify } = useToast();
  const [tours, setTours] = useState<GuideAssignedTourVm[]>([]);
  const [selectedScheduleId, setSelectedScheduleId] = useState<string | null>(null);
  const [searchKw, setSearchKw] = useState("");

  useEffect(() => {
    guideService.getAssignedTours().then((res) => {
      setTours(res);
      if (res.length > 0) setSelectedScheduleId(res[0].scheduleId);
    });
  }, []);

  const activeTour = tours.find((t) => t.scheduleId === selectedScheduleId);

  async function handleToggleCheckIn(passengerId: string) {
    if (!selectedScheduleId) return;
    const newCheckedState = await guideService.toggleCheckIn(selectedScheduleId, passengerId);
    setTours((prev) =>
      prev.map((t) => {
        if (t.scheduleId !== selectedScheduleId) return t;
        const updatedPassengers = t.passengers.map((p) =>
          p.id === passengerId ? { ...p, checkedIn: newCheckedState } : p
        );
        const checkedInCount = updatedPassengers.filter((p) => p.checkedIn).length;
        return { ...t, passengers: updatedPassengers, checkedInCount };
      })
    );
    notify(
      "success",
      newCheckedState ? "Đã xác nhận điểm danh hành khách." : "Đã hủy điểm danh hành khách."
    );
  }

  const filteredPassengers = activeTour?.passengers.filter(
    (p) =>
      p.fullName.toLowerCase().includes(searchKw.toLowerCase()) ||
      p.bookingCode.toLowerCase().includes(searchKw.toLowerCase())
  );

  return (
    <div className="mx-auto max-w-7xl px-4 sm:px-6 py-10 space-y-8">
      {/* Header */}
      <div className="rounded-3xl bg-[#0D2B2B] text-white p-8 sm:p-10 relative overflow-hidden shadow-xl border border-[#123A3A]">
        <div className="absolute right-0 top-0 w-80 h-80 bg-[#E2603F]/10 rounded-full blur-3xl" />
        <div className="relative max-w-2xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4A24C]/20 border border-[#D4A24C]/30 text-[#D4A24C] text-xs font-semibold">
            <ShieldCheck size={14} className="text-[#D4A24C]" /> Cổng Hướng Dẫn Viên
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Quản Lý Đoàn Tour Phụ Trách
          </h1>
          <p className="text-stone-300 text-sm">
            Theo dõi danh sách hành khách, điểm danh lên xe / check-in khách sạn và xem thông tin liên hệ khẩn cấp.
          </p>
        </div>
      </div>

      {/* Main Grid: Tour List Sidebar + Passenger Roster */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* Sidebar: Assigned Tours */}
        <div className="lg:col-span-1 space-y-4">
          <h3 className="font-bold text-stone-800 text-sm uppercase tracking-wider">
            Lịch Dẫn Đoàn Của Tôi ({tours.length})
          </h3>
          <div className="space-y-3">
            {tours.map((t) => (
              <button
                key={t.scheduleId}
                onClick={() => setSelectedScheduleId(t.scheduleId)}
                className={`w-full text-left p-4 rounded-2xl border transition-all ${
                  selectedScheduleId === t.scheduleId
                    ? "border-[#E2603F] bg-[#E2603F]/10 shadow-sm"
                    : "border-[#e7e0d3] bg-white hover:border-[#E2603F]/40"
                }`}
              >
                <span className="font-mono text-[10px] font-bold bg-[#0D2B2B] text-[#D4A24C] px-2 py-0.5 rounded border border-[#D4A24C]/30">
                  {t.tourCode}
                </span>
                <h4 className="font-bold text-stone-900 text-sm line-clamp-2 mt-2 mb-2 leading-snug">
                  {t.tourTitle}
                </h4>
                <div className="flex items-center justify-between text-xs text-stone-500 pt-2 border-t border-[#f5f0e6]">
                  <span className="flex items-center gap-1">
                    <CalendarDays size={12} /> {formatDateTime(t.departureTime).split(",")[0]}
                  </span>
                  <span className="font-bold text-[#E2603F]">
                    {t.checkedInCount}/{t.totalPassengers} Check-in
                  </span>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Passenger Roster */}
        <div className="lg:col-span-3 space-y-6">
          {activeTour ? (
            <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-6">
              {/* Tour Status & Progress Bar */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-[#f5f0e6] pb-6">
                <div>
                  <span className="text-xs uppercase tracking-wider text-[#E2603F] font-bold block mb-1">
                    Lịch Trình: {activeTour.tourCode}
                  </span>
                  <h2 className="text-xl font-extrabold text-stone-900">{activeTour.tourTitle}</h2>
                  <p className="text-xs text-stone-500 mt-1">
                    Khởi hành: {formatDateTime(activeTour.departureTime)} | Kết thúc: {formatDateTime(activeTour.arrivalTime)}
                  </p>
                </div>

                <button
                  onClick={() => notify("info", "Đã phát thông báo nhắc nhở đến toàn bộ khách đoàn!")}
                  className="inline-flex items-center gap-2 bg-[#0D2B2B] hover:bg-[#123A3A] text-white text-xs font-semibold px-4 py-2.5 rounded-xl shadow-md transition-colors shrink-0"
                >
                  <Bell size={14} className="text-[#D4A24C]" /> Phát Thông Báo Đoàn
                </button>
              </div>

              {/* Progress */}
              <div className="bg-[#FBF8F2] p-4 rounded-2xl space-y-2 border border-[#e7e0d3]">
                <div className="flex justify-between text-xs font-bold">
                  <span className="text-stone-700">Tỷ lệ điểm danh đoàn:</span>
                  <span className="text-[#E2603F]">
                    {activeTour.checkedInCount} / {activeTour.totalPassengers} Hành khách (
                    {Math.round((activeTour.checkedInCount / (activeTour.totalPassengers || 1)) * 100)}%)
                  </span>
                </div>
                <div className="h-2.5 w-full bg-[#e7e0d3] rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-[#E2603F] to-[#D4A24C] rounded-full transition-all duration-500"
                    style={{
                      width: `${Math.round((activeTour.checkedInCount / (activeTour.totalPassengers || 1)) * 100)}%`,
                    }}
                  />
                </div>
              </div>

              {/* Passenger Filter & Search */}
              <div className="flex items-center justify-between gap-4 pt-2">
                <div className="relative flex-1 max-w-sm">
                  <Search size={16} className="absolute left-3 top-3 text-slate-400" />
                  <input
                    value={searchKw}
                    onChange={(e) => setSearchKw(e.target.value)}
                    placeholder="Tìm theo tên hành khách, mã đơn..."
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-sky-500"
                  />
                </div>
              </div>

              {/* Table */}
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-100 uppercase tracking-wider">
                    <tr>
                      <th className="py-3 px-4">Mã Đơn</th>
                      <th className="py-3 px-4">Họ Và Tên</th>
                      <th className="py-3 px-4">Loại Vé</th>
                      <th className="py-3 px-4">CMND/CCCD</th>
                      <th className="py-3 px-4 text-center">Trạng Thái Check-in</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
                    {filteredPassengers?.length === 0 && (
                      <tr>
                        <td colSpan={5} className="py-8 text-center text-slate-400">
                          Không tìm thấy hành khách nào.
                        </td>
                      </tr>
                    )}
                    {filteredPassengers?.map((p) => (
                      <tr key={p.id} className="hover:bg-slate-50/80 transition-colors">
                        <td className="py-3.5 px-4 font-mono font-bold text-sky-600">{p.bookingCode}</td>
                        <td className="py-3.5 px-4 font-bold text-slate-900">{p.fullName}</td>
                        <td className="py-3.5 px-4">
                          <span className="bg-slate-100 text-slate-700 px-2 py-0.5 rounded font-semibold">
                            {p.passengerType === "ADULT" ? "Người lớn" : "Trẻ em"}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-slate-500 font-mono">
                          {p.idCardNumber || "—"}
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          <button
                            onClick={() => handleToggleCheckIn(p.id)}
                            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold transition-all ${
                              p.checkedIn
                                ? "bg-emerald-100 text-emerald-700 hover:bg-emerald-200"
                                : "bg-slate-100 text-slate-500 hover:bg-slate-200"
                            }`}
                          >
                            {p.checkedIn ? (
                              <>
                                <CheckCircle2 size={14} className="text-emerald-600" />
                                <span>Đã Check-in</span>
                              </>
                            ) : (
                              <>
                                <XCircle size={14} className="text-slate-400" />
                                <span>Chưa Check-in</span>
                              </>
                            )}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className="bg-white p-12 rounded-3xl border border-slate-200 text-center text-slate-400">
              Chọn một đoàn tour ở cột bên trái để quản lý danh sách hành khách.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
