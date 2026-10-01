"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import {
  Search,
  MapPin,
  Sparkles,
  ArrowRight,
  ShieldCheck,
  Zap,
  CreditCard,
  Star,
  Users,
  Compass,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { TourVm, DestinationVm } from "@/lib/types";
import { TourCard } from "@/components/tours/TourCard";
import { TourCardSkeleton } from "@/components/tours/TourCardSkeleton";

import { MOCK_TOURS, MOCK_DESTINATIONS } from "@/lib/mock-data";

export default function HomePage() {
  const router = useRouter();
  const [keyword, setKeyword] = useState("");
  const [destination, setDestination] = useState("");
  const [featured, setFeatured] = useState<TourVm[] | null>(null);
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);

  useEffect(() => {
    tourService
      .search({ page: 0, size: 6 })
      .then((res) => {
        if (res?.content && res.content.length > 0) {
          setFeatured(res.content);
        } else {
          setFeatured(MOCK_TOURS as any);
        }
      })
      .catch(() => setFeatured(MOCK_TOURS as any));

    tourService
      .listDestinations()
      .then((res) => {
        if (res && res.length > 0) {
          setDestinations(res);
        } else {
          setDestinations(MOCK_DESTINATIONS);
        }
      })
      .catch(() => setDestinations(MOCK_DESTINATIONS));
  }, []);

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    const params = new URLSearchParams();
    if (keyword) params.set("keyword", keyword);
    if (destination) params.set("destination", destination);
    router.push(`/tours?${params.toString()}`);
  }

  return (
    <div className="space-y-16 pb-16">
      {/* Hero Section */}
      <section className="relative overflow-hidden hero-gradient text-white pt-16 pb-24 md:pt-24 md:pb-32">
        <div className="absolute inset-0 opacity-10 [background-image:radial-gradient(circle_at_1px_1px,#fff_1px,transparent_0)] [background-size:24px_24px]" />
        <div className="relative mx-auto max-w-7xl px-4 sm:px-6">
          <div className="max-w-3xl space-y-6">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-[#D4A24C]/20 border border-[#D4A24C]/40 text-[#D4A24C] text-xs font-semibold backdrop-blur-md">
              <Sparkles size={14} className="text-[#D4A24C] animate-pulse" />
              <span>Nền Tảng Đặt Tour Du Lịch Việt Nam 2026</span>
            </div>

            <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight leading-tight">
              Khám Phá Việt Nam.
              <br />
              <span className="text-[#D4A24C]">
                Hành Trình Trọn Vẹn Niềm Vui.
              </span>
            </h1>

            <p className="text-stone-300 text-base sm:text-lg leading-relaxed max-w-2xl">
              Hệ thống tìm kiếm, giữ chỗ trực tuyến trong 15 phút, thanh toán VNPAY/MoMo an toàn và gợi ý lịch trình tự động bằng Trợ lý AI thông minh.
            </p>

            {/* Quick Search Form */}
            <form
              onSubmit={handleSearch}
              className="pt-4 flex flex-col sm:flex-row gap-3 bg-white/95 p-3 rounded-2xl shadow-2xl shadow-black/30 backdrop-blur-md border border-[#e7e0d3] text-stone-800"
            >
              <div className="flex items-center gap-3 flex-1 px-3 py-2 border-b sm:border-b-0 sm:border-r border-stone-200">
                <Search size={20} className="text-[#E2603F] shrink-0" />
                <input
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="Tên tour hoặc từ khóa (Đà Nẵng, Phú Quốc...)"
                  className="w-full bg-transparent text-stone-800 text-sm font-medium placeholder:text-stone-400 focus:outline-none"
                />
              </div>

              <div className="flex items-center gap-3 sm:w-56 px-3 py-2">
                <MapPin size={20} className="text-[#D4A24C] shrink-0" />
                <input
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  placeholder="Điểm đến mong muốn"
                  className="w-full bg-transparent text-stone-800 text-sm font-medium placeholder:text-stone-400 focus:outline-none"
                />
              </div>

              <button
                type="submit"
                className="bg-[#E2603F] hover:bg-[#d55333] text-white font-semibold text-sm px-7 py-3.5 rounded-xl shadow-lg shadow-[#E2603F]/30 transition-all shrink-0 flex items-center justify-center gap-2"
              >
                <span>Tìm kiếm</span>
                <ArrowRight size={16} />
              </button>
            </form>
          </div>

          {/* Quick System Metric Highlights */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-12 text-xs font-medium text-stone-200">
            <div className="flex items-center gap-2.5 bg-[#123A3A]/80 border border-[#1f5252] p-3 rounded-xl backdrop-blur-md">
              <Zap size={18} className="text-[#D4A24C] shrink-0" />
              <span>Giữ Chỗ Trực Tuyến 15 Phút</span>
            </div>
            <div className="flex items-center gap-2.5 bg-[#123A3A]/80 border border-[#1f5252] p-3 rounded-xl backdrop-blur-md">
              <CreditCard size={18} className="text-[#E2603F] shrink-0" />
              <span>VNPAY & MoMo Tự Động</span>
            </div>
            <div className="flex items-center gap-2.5 bg-[#123A3A]/80 border border-[#1f5252] p-3 rounded-xl backdrop-blur-md">
              <Sparkles size={18} className="text-[#D4A24C] shrink-0" />
              <span>Gợi ý lịch trình thông minh</span>
            </div>
            <div className="flex items-center gap-2.5 bg-[#123A3A]/80 border border-[#1f5252] p-3 rounded-xl backdrop-blur-md">
              <ShieldCheck size={18} className="text-[#D4A24C] shrink-0" />
              <span>Phân Quyền Admin, HDV, Tourist</span>
            </div>
          </div>
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="flex items-end justify-between mb-8">
          <div>
            <span className="text-xs uppercase tracking-widest text-[#E2603F] font-bold block mb-1">
              Điểm Đến Hấp Dẫn
            </span>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-stone-900 tracking-tight">
              Khám Phá Các Thành Phố Du Lịch HOT
            </h2>
          </div>
          <Link
            href="/tours"
            className="hidden sm:flex items-center gap-1.5 text-sm font-semibold text-[#E2603F] hover:text-[#d55333] transition-colors"
          >
            Xem tất cả điểm đến <ArrowRight size={16} />
          </Link>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {destinations.slice(0, 6).map((dest) => (
            <Link
              key={dest.id}
              href={`/tours?destination=${encodeURIComponent(dest.city)}`}
              className="group relative h-44 rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-all duration-300 block"
            >
              <img
                src={dest.imageUrl}
                alt={dest.name}
                className="h-full w-full object-cover group-hover:scale-110 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-[#0D2B2B]/90 via-[#0D2B2B]/30 to-transparent" />
              <div className="absolute bottom-3 left-3 right-3 text-white">
                <p className="font-bold text-sm leading-tight group-hover:text-[#D4A24C] transition-colors">
                  {dest.city}
                </p>
                <p className="text-[11px] text-stone-300 mt-0.5">{dest.tourCount ?? 10}+ Tour khả dụng</p>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Tours Catalog Grid */}
      <section className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="flex items-end justify-between mb-8">
          <div>
            <span className="text-xs uppercase tracking-widest text-[#D4A24C] font-bold block mb-1">
              Khuyến Mãi Đặc Biệt
            </span>
            <h2 className="text-2xl sm:text-3xl font-extrabold text-stone-900 tracking-tight">
              Danh Mục Tour Mới Nhất 2026
            </h2>
          </div>
          <Link
            href="/tours"
            className="flex items-center gap-1.5 text-sm font-semibold text-[#E2603F] hover:text-[#d55333] transition-colors"
          >
            Xem toàn bộ Tour <ArrowRight size={16} />
          </Link>
        </div>

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {featured === null &&
            Array.from({ length: 6 }).map((_, i) => <TourCardSkeleton key={i} />)}
          {featured?.length === 0 && (
            <p className="col-span-full text-stone-500 text-sm">
              Chưa có tour nào để hiển thị.
            </p>
          )}
          {featured?.map((tour) => (
            <TourCard key={tour.tourId} tour={tour} />
          ))}
        </div>
      </section>

      {/* AI Assistant Callout Banner */}
      <section className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="relative rounded-3xl bg-gradient-to-r from-[#0D2B2B] via-[#123A3A] to-[#0D2B2B] text-white p-8 sm:p-12 overflow-hidden shadow-2xl border border-[#1f5252]">
          <div className="absolute right-0 top-0 w-96 h-96 bg-[#E2603F]/10 rounded-full blur-3xl" />

          <div className="relative max-w-2xl space-y-4">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-md bg-[#D4A24C]/20 border border-[#D4A24C]/30 text-[#D4A24C] text-xs font-semibold">
              <Sparkles size={14} className="text-[#D4A24C]" />
              <span>Gợi ý lịch trình thông minh</span>
            </div>

            <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
              Chưa Biết Chọn Tour Nào?
              <br />
              <span className="text-[#D4A24C]">Hãy Nhờ Trợ Lý AI Tư Vấn Thông Minh!</span>
            </h2>

            <p className="text-stone-300 text-sm sm:text-base leading-relaxed">
              Nhập nhu cầu cá nhân của bạn (ví dụ: &ldquo;Đi Đà Nẵng 3 ngày 2 đêm cho gia đình 4 người, ngân sách dưới 10 triệu&rdquo;). AI sẽ tự động phân tích và gợi ý lịch trình tối ưu kèm link đặt tour thật!
            </p>

            <div className="pt-2">
              <Link
                href="/ai-assistant"
                className="inline-flex items-center gap-2 bg-[#E2603F] hover:bg-[#d55333] text-white font-semibold text-sm px-6 py-3.5 rounded-xl shadow-lg transition-all"
              >
                <span>Mở Trợ Lý AI Tư Vấn Ngay</span>
                <ArrowRight size={16} />
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
