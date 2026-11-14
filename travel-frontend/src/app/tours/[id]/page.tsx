"use client";

import { use, useEffect, useState, useMemo } from "react";
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
  ChevronLeft,
  ChevronRight,
  X,
  Image as ImageIcon,
  Maximize2,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { bookingService } from "@/lib/services/booking-service";
import { useAuth } from "@/lib/auth-context";
import { TourDetailVm, TourScheduleVm, PassengerRequest } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";

export default function TourDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { user } = useAuth();

  const [tour, setTour] = useState<TourDetailVm | null | "error">(null);
  const [selectedSchedule, setSelectedSchedule] = useState<TourScheduleVm | null>(null);
  const [activeLightboxIndex, setActiveLightboxIndex] = useState<number | null>(null);

  // Passenger counts
  const [adultCount, setAdultCount] = useState(1);
  const [childCount, setChildCount] = useState(0);
  const [passengers, setPassengers] = useState<PassengerRequest[]>([
    { fullName: user?.fullName ?? "Họ và tên hành khách", passengerType: "ADULT", idCardNumber: "" },
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
          setTour("error");
        }
      })
      .catch(() => {
        setTour("error");
      });
  }, [id]);

  // Resolve all images for this tour (Hook called unconditionally)
  const tourImages = useMemo(() => {
    if (!tour || tour === "error") return [];

    // 1. From database tour_images table (tour.images)
    if (tour.images && tour.images.length > 0) {
      const sorted = [...tour.images].sort((a, b) => {
        if (a.isPrimary) return -1;
        if (b.isPrimary) return 1;
        return (a.displayOrder ?? 0) - (b.displayOrder ?? 0);
      });
      return sorted
        .filter((img) => img.imageUrl && img.imageUrl.trim().length > 0)
        .map((img) => ({
          url: img.imageUrl,
          caption: img.caption || tour.title,
        }));
    }

    // 2. From tour.galleryUrls if exists
    if (tour.galleryUrls && tour.galleryUrls.length > 0) {
      return tour.galleryUrls.map((url, i) => ({
        url,
        caption: `${tour.title} (Ảnh ${i + 1})`,
      }));
    }

    // 3. From tour.imageUrl and destinations images (smart contextual fallback)
    const list: { url: string; caption?: string }[] = [];
    if (tour.imageUrl) {
      list.push({ url: tour.imageUrl, caption: tour.title });
    }
    if (tour.destinations && tour.destinations.length > 0) {
      tour.destinations.forEach((d) => {
        if (d.imageUrl && !list.some((item) => item.url === d.imageUrl)) {
          list.push({ url: d.imageUrl, caption: `${d.name}, ${d.city}` });
        }
      });
    }

    if (list.length === 0) {
      list.push({
        url: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=1200&q=80",
        caption: tour.title,
      });
    }
    return list;
  }, [tour]);

  // Keyboard navigation for lightbox (Hook called unconditionally)
  useEffect(() => {
    function handleKeyDown(e: KeyboardEvent) {
      if (activeLightboxIndex === null) return;
      if (e.key === "Escape") setActiveLightboxIndex(null);
      if (e.key === "ArrowLeft") {
        setActiveLightboxIndex((prev) => (prev !== null && prev > 0 ? prev - 1 : tourImages.length - 1));
      }
      if (e.key === "ArrowRight") {
        setActiveLightboxIndex((prev) => (prev !== null && prev < tourImages.length - 1 ? prev + 1 : 0));
      }
    }
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [activeLightboxIndex, tourImages.length]);

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

      {/* Dynamic Image Gallery */}
      <div className="relative">
        {tourImages.length === 1 && (
          <div
            onClick={() => setActiveLightboxIndex(0)}
            className="h-96 w-full rounded-3xl overflow-hidden shadow-lg relative group cursor-pointer border border-[#e7e0d3]"
          >
            <img
              src={tourImages[0].url}
              alt={tourImages[0].caption || tour.title}
              className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-80 group-hover:opacity-90 transition-opacity" />
            <div className="absolute bottom-4 left-6 right-6 flex items-center justify-between text-white">
              <span className="text-sm font-semibold line-clamp-1">{tourImages[0].caption}</span>
              <span className="text-xs font-bold bg-white/20 backdrop-blur-md px-3 py-1.5 rounded-xl flex items-center gap-1.5">
                <Maximize2 size={13} /> Phóng to ảnh
              </span>
            </div>
          </div>
        )}

        {tourImages.length === 2 && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 rounded-3xl overflow-hidden shadow-lg h-96 border border-[#e7e0d3]">
            {tourImages.map((img, i) => (
              <div
                key={i}
                onClick={() => setActiveLightboxIndex(i)}
                className="relative h-full group cursor-pointer overflow-hidden"
              >
                <img
                  src={img.url}
                  alt={img.caption || `Ảnh ${i + 1}`}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-90 transition-opacity" />
                <div className="absolute bottom-3 left-4 right-4 text-white opacity-0 group-hover:opacity-100 transition-opacity">
                  <span className="text-xs font-semibold">{img.caption}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        {tourImages.length >= 3 && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 rounded-3xl overflow-hidden shadow-lg h-96 border border-[#e7e0d3]">
            {/* Main large image on left */}
            <div
              onClick={() => setActiveLightboxIndex(0)}
              className="md:col-span-2 relative h-full group cursor-pointer overflow-hidden"
            >
              <img
                src={tourImages[0].url}
                alt={tourImages[0].caption || tour.title}
                className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-80 group-hover:opacity-90 transition-opacity" />
              <div className="absolute bottom-4 left-6 right-6 text-white">
                <span className="text-xs uppercase tracking-wider text-[#D4A24C] font-bold block mb-0.5">Ảnh Đại Diện Tour</span>
                <span className="text-sm font-semibold line-clamp-1">{tourImages[0].caption}</span>
              </div>
            </div>

            {/* 2 stacked images on right */}
            <div className="hidden md:flex flex-col gap-4 h-full">
              <div
                onClick={() => setActiveLightboxIndex(1)}
                className="relative h-1/2 rounded-tr-2xl group cursor-pointer overflow-hidden"
              >
                <img
                  src={tourImages[1].url}
                  alt={tourImages[1].caption || "Ảnh 2"}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 rounded-tr-2xl"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                {tourImages[1].caption && (
                  <div className="absolute bottom-2 left-3 right-3 text-white text-[11px] font-medium opacity-0 group-hover:opacity-100 transition-opacity line-clamp-1">
                    {tourImages[1].caption}
                  </div>
                )}
              </div>

              <div
                onClick={() => setActiveLightboxIndex(2)}
                className="relative h-1/2 rounded-br-2xl group cursor-pointer overflow-hidden"
              >
                <img
                  src={tourImages[2].url}
                  alt={tourImages[2].caption || "Ảnh 3"}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 rounded-br-2xl"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                {tourImages.length > 3 && (
                  <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px] flex items-center justify-center text-white font-bold text-sm gap-1.5 transition-colors hover:bg-black/50">
                    <ImageIcon size={16} />
                    <span>+{tourImages.length - 3} ảnh khác</span>
                  </div>
                )}
                {tourImages.length === 3 && tourImages[2].caption && (
                  <div className="absolute bottom-2 left-3 right-3 text-white text-[11px] font-medium opacity-0 group-hover:opacity-100 transition-opacity line-clamp-1">
                    {tourImages[2].caption}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* View all photos pill button */}
        <button
          onClick={() => setActiveLightboxIndex(0)}
          className="absolute bottom-4 right-4 bg-white/90 hover:bg-white text-stone-900 text-xs font-bold px-3.5 py-2 rounded-xl shadow-lg backdrop-blur-md border border-stone-200 flex items-center gap-1.5 transition-all hover:scale-105"
        >
          <ImageIcon size={14} className="text-[#E2603F]" /> Xem toàn bộ ảnh ({tourImages.length})
        </button>
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

      {/* Interactive Lightbox Modal */}
      {activeLightboxIndex !== null && (
        <div className="fixed inset-0 bg-black/95 z-50 flex flex-col justify-between p-4 sm:p-6 backdrop-blur-md animate-in fade-in">
          {/* Top Bar */}
          <div className="flex items-center justify-between text-white border-b border-white/10 pb-3">
            <div>
              <h3 className="font-bold text-sm sm:text-base line-clamp-1">{tour.title}</h3>
              <p className="text-xs text-stone-400">
                Ảnh {activeLightboxIndex + 1} trên {tourImages.length}
              </p>
            </div>
            <button
              onClick={() => setActiveLightboxIndex(null)}
              className="w-9 h-9 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors"
            >
              <X size={18} />
            </button>
          </div>

          {/* Center: Image & Controls */}
          <div className="relative flex-1 flex items-center justify-center my-4">
            {tourImages.length > 1 && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setActiveLightboxIndex((prev) => (prev !== null && prev > 0 ? prev - 1 : tourImages.length - 1));
                }}
                className="absolute left-2 sm:left-4 z-10 w-11 h-11 rounded-full bg-white/15 hover:bg-white/30 text-white flex items-center justify-center backdrop-blur-md transition-all hover:scale-110"
              >
                <ChevronLeft size={22} />
              </button>
            )}

            <div className="max-h-[70vh] flex flex-col items-center justify-center max-w-4xl px-2">
              <img
                src={tourImages[activeLightboxIndex].url}
                alt={tourImages[activeLightboxIndex].caption || "Tour Photo"}
                className="max-h-[65vh] max-w-full object-contain rounded-2xl shadow-2xl"
              />
              {tourImages[activeLightboxIndex].caption && (
                <p className="mt-3 text-center text-xs sm:text-sm text-stone-300 font-medium bg-black/40 px-4 py-1.5 rounded-full border border-white/10">
                  {tourImages[activeLightboxIndex].caption}
                </p>
              )}
            </div>

            {tourImages.length > 1 && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setActiveLightboxIndex((prev) => (prev !== null && prev < tourImages.length - 1 ? prev + 1 : 0));
                }}
                className="absolute right-2 sm:right-4 z-10 w-11 h-11 rounded-full bg-white/15 hover:bg-white/30 text-white flex items-center justify-center backdrop-blur-md transition-all hover:scale-110"
              >
                <ChevronRight size={22} />
              </button>
            )}
          </div>

          {/* Bottom Thumbnails Strip */}
          {tourImages.length > 1 && (
            <div className="flex items-center justify-center gap-2 overflow-x-auto py-2 border-t border-white/10 max-w-2xl mx-auto w-full">
              {tourImages.map((img, idx) => (
                <button
                  key={idx}
                  onClick={() => setActiveLightboxIndex(idx)}
                  className={`w-14 h-14 rounded-xl overflow-hidden shrink-0 border-2 transition-all ${
                    idx === activeLightboxIndex
                      ? "border-[#D4A24C] scale-105 opacity-100"
                      : "border-transparent opacity-50 hover:opacity-80"
                  }`}
                >
                  <img src={img.url} alt={`thumb ${idx}`} className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
