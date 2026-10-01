"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import {
  MapPin,
  Users,
  CalendarDays,
  ArrowLeft,
  Star,
  CheckCircle2,
  XCircle,
  Clock,
  ShieldCheck,
  Plus,
  Minus,
  Sparkles,
  Ticket,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { bookingService } from "@/lib/services/booking-service";
import { useAuth } from "@/lib/auth-context";
import { TourDetailVm, TourScheduleVm, PassengerRequest } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";

import { MOCK_TOURS } from "@/lib/mock-data";

export default function TourDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { user } = useAuth();

  const [tour, setTour] = useState<TourDetailVm | null | "error">(null);
  const [selectedSchedule, setSelectedSchedule] = useState<TourScheduleVm | null>(null);

  // Passenger counts
  const [adultCount, setAdultCount] = useState(1);
  const [childCount, setChildCount] = useState(0);
  const [passengers, setPassengers] = useState<PassengerRequest[]>([
    { fullName: user?.fullName ?? "Nguyễn Văn Tourist", passengerType: "ADULT", idCardNumber: "040099123456" },
  ]);

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    tourService
      .getById(id)
      .then((data) => {
        if (data && data.tourId) {
          setTour(data);
          const validSch = data.schedules?.find((s) => s.availableSeats > 0) ?? data.schedules?.[0] ?? null;
          setSelectedSchedule(validSch);
        } else {
          const mock = MOCK_TOURS.find((t) => t.tourId === id) ?? MOCK_TOURS[0];
          setTour(mock as any);
          const validSch = mock.schedules?.find((s) => s.availableSeats > 0) ?? mock.schedules?.[0] ?? null;
          setSelectedSchedule(validSch as any);
        }
      })
      .catch(() => {
        const mock = MOCK_TOURS.find((t) => t.tourId === id) ?? MOCK_TOURS[0];
        if (mock) {
          setTour(mock as any);
          const validSch = mock.schedules?.find((s) => s.availableSeats > 0) ?? mock.schedules?.[0] ?? null;
          setSelectedSchedule(validSch as any);
        } else {
          setTour("error");
        }
      });
  }, [id]);

  function updatePassengerCounts(newAdults: number, newChildren: number) {
    if (newAdults < 1) return;
    setAdultCount(newAdults);
    setChildCount(newChildren);

    const newPassengers: PassengerRequest[] = [];
    for (let i = 0; i < newAdults; i++) {
      newPassengers.push({
        fullName: i === 0 ? user?.fullName ?? "Nguyễn Văn Tourist" : `Hành khách Người lớn ${i + 1}`,
        passengerType: "ADULT",
        idCardNumber: i === 0 ? "040099123456" : "",
      });
    }
    for (let i = 0; i < newChildren; i++) {
      newPassengers.push({
        fullName: `Trẻ em ${i + 1}`,
        passengerType: "CHILD",
      });
    }
    setPassengers(newPassengers);
  }

  async function handleBookTour() {
    if (!selectedSchedule) return;
    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const booking = await bookingService.create(
        {
          tourScheduleId: selectedSchedule.id,
          passengers,
        },
        user?.userId ?? "11111111-1111-1111-1111-111111111111"
      );
      router.push(`/checkout/${booking.bookingId}`);
    } catch (err: any) {
      setErrorMessage(err.message || "Tạo đơn hàng thất bại. Vui lòng thử lại!");
    } finally {
      setIsSubmitting(false);
    }
  }

  if (tour === "error") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-24 text-center space-y-4">
        <p className="text-slate-500 font-medium">Không tìm thấy thông tin tour yêu cầu.</p>
        <Link href="/tours" className="inline-flex items-center gap-1.5 text-sm text-teal-600 hover:text-teal-700 font-semibold underline">
          <ArrowLeft size={16} /> Quay lại danh sách Tour
        </Link>
      </div>
    );
  }

  if (!tour) {
    return (
      <div className="mx-auto max-w-7xl px-4 sm:px-6 py-16 animate-pulse space-y-8">
        <div className="h-6 w-32 bg-slate-200 rounded" />
        <div className="h-10 w-2/3 bg-slate-200 rounded" />
        <div className="h-96 bg-slate-200 rounded-3xl" />
      </div>
    );
  }

  const totalPrice = selectedSchedule
    ? adultCount * selectedSchedule.priceAdult + childCount * selectedSchedule.priceChild
    : 0;

  return (
    <div className="mx-auto max-w-7xl px-4 sm:px-6 py-10 space-y-8">
      {/* Back Link */}
      <Link href="/tours" className="inline-flex items-center gap-2 text-sm font-semibold text-stone-500 hover:text-[#E2603F] transition-colors">
        <ArrowLeft size={16} /> Danh mục Tour
      </Link>

      {/* Header Info */}
      <div className="space-y-4">
        <div className="flex flex-wrap items-center gap-3">
          <span className="font-mono text-xs uppercase font-bold tracking-wider bg-[#0D2B2B] text-[#D4A24C] px-3 py-1 rounded-md border border-[#D4A24C]/30">
            {tour.code}
          </span>
          <StatusBadge status={tour.status} />
          <div className="flex items-center gap-1 text-xs text-[#D4A24C] font-bold bg-[#D4A24C]/15 px-2.5 py-1 rounded-md border border-[#D4A24C]/30">
            <Star size={14} className="fill-[#D4A24C] text-[#D4A24C]" />
            <span>{tour.rating ?? 4.9}</span>
            <span className="text-stone-500 font-normal">({tour.reviewCount ?? 128} đánh giá)</span>
          </div>
        </div>

        <h1 className="text-3xl sm:text-5xl font-extrabold text-stone-900 tracking-tight leading-tight">
          {tour.title}
        </h1>

        <div className="flex flex-wrap gap-4 text-xs font-medium text-stone-600">
          <span className="flex items-center gap-1.5 bg-[#FBF8F2] border border-[#e7e0d3] px-3 py-1.5 rounded-lg">
            <Clock size={14} className="text-[#D4A24C]" /> Thời gian: {tour.duration ?? "3 Ngày 2 Đêm"}
          </span>
          {tour.destinations.map((d) => (
            <span key={d.id} className="flex items-center gap-1.5 bg-[#FBF8F2] border border-[#e7e0d3] px-3 py-1.5 rounded-lg">
              <MapPin size={14} className="text-[#E2603F]" /> Điểm đến: {d.name}, {d.city}
            </span>
          ))}
        </div>
      </div>

      {/* Main Image Gallery */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 rounded-3xl overflow-hidden shadow-lg h-96">
        <div className="md:col-span-2 relative h-full">
          <img
            src={tour.imageUrl ?? "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80"}
            alt={tour.title}
            className="w-full h-full object-cover"
          />
        </div>
        <div className="hidden md:flex flex-col gap-4 h-full">
          <img
            src={tour.galleryUrls?.[1] ?? "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80"}
            alt="Gallery 1"
            className="w-full h-1/2 object-cover rounded-tr-2xl"
          />
          <img
            src={tour.galleryUrls?.[2] ?? "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80"}
            alt="Gallery 2"
            className="w-full h-1/2 object-cover rounded-br-2xl"
          />
        </div>
      </div>

      {/* Grid Content: Details & Booking Sidebar */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
        {/* Left Column: Description, Itinerary, Services */}
        <div className="lg:col-span-2 space-y-10">
          {/* Description */}
          <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-4">
            <h2 className="text-xl font-bold text-stone-900 border-b border-[#f5f0e6] pb-3">
              Mô Tả Tổng Quan Tour
            </h2>
            <p className="text-stone-600 leading-relaxed text-sm sm:text-base whitespace-pre-line">
              {tour.description}
            </p>
          </div>

          {/* Detailed Itinerary */}
          {tour.itineraries.length > 0 && (
            <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-6">
              <h2 className="text-xl font-bold text-stone-900 border-b border-[#f5f0e6] pb-3">
                Lịch Trình Chi Tiết Theo Ngày
              </h2>
              <div className="space-y-6">
                {tour.itineraries
                  .sort((a, b) => a.dayNumber - b.dayNumber)
                  .map((it, idx) => (
                    <div key={it.id} className="relative pl-8 pb-6 border-l-2 border-[#E2603F] last:border-l-0 last:pb-0">
                      <div className="absolute -left-[17px] top-0 w-8 h-8 rounded-full bg-[#0D2B2B] text-[#D4A24C] font-bold text-xs flex items-center justify-center shadow-md border border-[#D4A24C]/30">
                        N{it.dayNumber}
                      </div>
                      <div className="space-y-1.5">
                        <h4 className="font-bold text-stone-900 text-base">{it.title}</h4>
                        <p className="text-stone-600 text-sm leading-relaxed">{it.content}</p>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          )}

          {/* Included / Excluded Services */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div className="bg-[#FBF8F2] border border-[#e7e0d3] p-6 rounded-3xl space-y-3">
              <h3 className="font-bold text-stone-900 text-sm flex items-center gap-2">
                <CheckCircle2 size={18} className="text-[#E2603F]" /> Dịch Vụ Bao Gồm
              </h3>
              <ul className="space-y-2 text-xs text-stone-700">
                {(tour.includedServices ?? ["Vé tham quan", "Khách sạn 4 sao", "Xe đời mới", "HDV"]).map((inc, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-[#E2603F] shrink-0" />
                    <span>{inc}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="bg-[#FBF8F2] border border-[#e7e0d3] p-6 rounded-3xl space-y-3">
              <h3 className="font-bold text-stone-900 text-sm flex items-center gap-2">
                <XCircle size={18} className="text-stone-400" /> Chưa Bao Gồm
              </h3>
              <ul className="space-y-2 text-xs text-stone-600">
                {(tour.excludedServices ?? ["Vé máy bay khứ hồi", "Chi phí cá nhân ngoài chương trình"]).map((exc, i) => (
                  <li key={i} className="flex items-center gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-stone-400 shrink-0" />
                    <span>{exc}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        {/* Right Sidebar: Schedule Selector & Passenger Form */}
        <div className="lg:col-span-1">
          <div className="sticky top-24 bg-white p-6 rounded-3xl border border-[#e7e0d3] shadow-xl space-y-6">
            <div className="border-b border-[#f5f0e6] pb-4">
              <span className="text-xs uppercase tracking-wider text-[#E2603F] font-bold block mb-1">
                Giữ Chỗ Trực Tuyến
              </span>
              <h3 className="font-extrabold text-stone-900 text-xl flex items-center gap-2">
                <CalendarDays size={20} className="text-[#E2603F]" /> Chọn Lịch Khởi Hành
              </h3>
            </div>

            {errorMessage && (
              <div className="p-3 bg-red-50 text-red-600 border border-red-200 rounded-xl text-xs font-medium">
                {errorMessage}
              </div>
            )}

            {/* Schedule List */}
            <div className="space-y-2.5 max-h-64 overflow-y-auto pr-1">
              {tour.schedules.length === 0 && (
                <p className="text-xs text-stone-400 py-4 text-center">Chưa có lịch khởi hành.</p>
              )}
              {tour.schedules.map((s) => (
                <button
                  key={s.id}
                  onClick={() => setSelectedSchedule(s)}
                  disabled={s.availableSeats === 0}
                  className={`w-full text-left rounded-2xl border p-3.5 transition-all disabled:opacity-40 disabled:cursor-not-allowed ${
                    selectedSchedule?.id === s.id
                      ? "border-[#E2603F] bg-[#E2603F]/10 shadow-sm"
                      : "border-[#e7e0d3] hover:border-[#E2603F]/50"
                  }`}
                >
                  <p className="text-xs font-bold text-stone-800">{formatDateTime(s.departureTime)}</p>
                  <div className="flex items-center justify-between mt-2 text-xs">
                    <span className="text-stone-500 flex items-center gap-1">
                      <Users size={12} />
                      {s.availableSeats > 0 ? `Còn ${s.availableSeats} chỗ` : "Hết chỗ"}
                    </span>
                    <span className="font-extrabold text-[#E2603F]">
                      {formatCurrency(s.priceAdult)}
                    </span>
                  </div>
                </button>
              ))}
            </div>

            {/* Passenger Count Selector */}
            {selectedSchedule && (
              <div className="space-y-4 pt-4 border-t border-[#f5f0e6]">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs font-bold text-stone-800">Người lớn (≥ 12 tuổi)</p>
                    <p className="text-[11px] text-stone-400">{formatCurrency(selectedSchedule.priceAdult)}/vé</p>
                  </div>
                  <div className="flex items-center gap-2 bg-[#FBF8F2] p-1 rounded-xl border border-[#e7e0d3]">
                    <button
                      onClick={() => updatePassengerCounts(Math.max(1, adultCount - 1), childCount)}
                      className="w-7 h-7 rounded-lg bg-white shadow-sm flex items-center justify-center text-stone-700 font-bold"
                    >
                      <Minus size={14} />
                    </button>
                    <span className="w-6 text-center text-xs font-bold text-stone-800">{adultCount}</span>
                    <button
                      onClick={() => updatePassengerCounts(adultCount + 1, childCount)}
                      className="w-7 h-7 rounded-lg bg-white shadow-sm flex items-center justify-center text-stone-700 font-bold"
                    >
                      <Plus size={14} />
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs font-bold text-stone-800">Trẻ em (5 - 11 tuổi)</p>
                    <p className="text-[11px] text-stone-400">{formatCurrency(selectedSchedule.priceChild)}/vé</p>
                  </div>
                  <div className="flex items-center gap-2 bg-[#FBF8F2] p-1 rounded-xl border border-[#e7e0d3]">
                    <button
                      onClick={() => updatePassengerCounts(adultCount, Math.max(0, childCount - 1))}
                      className="w-7 h-7 rounded-lg bg-white shadow-sm flex items-center justify-center text-stone-700 font-bold"
                    >
                      <Minus size={14} />
                    </button>
                    <span className="w-6 text-center text-xs font-bold text-stone-800">{childCount}</span>
                    <button
                      onClick={() => updatePassengerCounts(adultCount, childCount + 1)}
                      className="w-7 h-7 rounded-lg bg-white shadow-sm flex items-center justify-center text-stone-700 font-bold"
                    >
                      <Plus size={14} />
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Total & Submit Button */}
            <div className="pt-4 border-t border-[#f5f0e6] space-y-4">
              <div className="flex items-baseline justify-between">
                <span className="text-xs font-semibold text-stone-500">Tổng tiền tạm tính:</span>
                <span className="text-2xl font-extrabold text-[#E2603F]">{formatCurrency(totalPrice)}</span>
              </div>

              <button
                disabled={!selectedSchedule || selectedSchedule.availableSeats === 0 || isSubmitting}
                onClick={handleBookTour}
                className="w-full bg-[#E2603F] hover:bg-[#d55333] text-white font-bold text-sm py-4 rounded-2xl shadow-lg shadow-[#E2603F]/30 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {isSubmitting ? (
                  <span>Đang khởi tạo đơn giữ chỗ...</span>
                ) : (
                  <>
                    <Ticket size={18} />
                    <span>Tiến Hành Đặt Tour ngay</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
