"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import {
  Search,
  SlidersHorizontal,
  ChevronLeft,
  ChevronRight,
  MapPin,
  Tag,
  RotateCcw,
  Sparkles,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { PageResponse, TourVm, DestinationVm } from "@/lib/types";
import { TourCard } from "@/components/tours/TourCard";
import { TourCardSkeleton } from "@/components/tours/TourCardSkeleton";
import { MockStore } from "@/lib/mock-store";
import { MOCK_DESTINATIONS } from "@/lib/mock-data";

function ToursContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [keyword, setKeyword] = useState(searchParams.get("keyword") ?? "");
  const [destination, setDestination] = useState(searchParams.get("destination") ?? "");
  const [minPrice, setMinPrice] = useState(searchParams.get("minPrice") ?? "");
  const [maxPrice, setMaxPrice] = useState(searchParams.get("maxPrice") ?? "");
  const [page, setPage] = useState(Number(searchParams.get("page") ?? 0));
  const [result, setResult] = useState<PageResponse<TourVm> | null>(null);
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);

  useEffect(() => {
    tourService
      .listDestinations()
      .then((res) => {
        if (res && res.length > 0) setDestinations(res);
        else setDestinations(MOCK_DESTINATIONS);
      })
      .catch(() => setDestinations(MOCK_DESTINATIONS));
  }, []);

  useEffect(() => {
    setResult(null);
    const criteria = {
      keyword: keyword || undefined,
      destination: destination || undefined,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
      page,
      size: 9,
    };

    tourService
      .search(criteria)
      .then((res) => {
        if (res && res.content && res.content.length > 0) {
          setResult(res);
        } else {
          setResult(MockStore.getTours(criteria));
        }
      })
      .catch(() => setResult(MockStore.getTours(criteria)));
  }, [keyword, destination, minPrice, maxPrice, page]);

  function handleReset() {
    setKeyword("");
    setDestination("");
    setMinPrice("");
    setMaxPrice("");
    setPage(0);
    router.push("/tours");
  }

  return (
    <div className="mx-auto max-w-7xl px-4 sm:px-6 py-10 space-y-8">
      {/* Header Banner */}
      <div className="rounded-3xl bg-[#0D2B2B] text-white p-8 sm:p-10 relative overflow-hidden shadow-xl border border-[#123A3A]">
        <div className="absolute right-0 top-0 w-80 h-80 bg-[#E2603F]/10 rounded-full blur-3xl" />
        <div className="relative max-w-2xl space-y-3">
          <span className="text-xs uppercase tracking-widest text-[#D4A24C] font-bold block">
            Danh Mục Đa Dạng 2026
          </span>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Tìm Kiếm & Lọc Tour Du Lịch Toàn Quốc
          </h1>
          <p className="text-stone-300 text-sm">
            Hơn 50+ lịch trình du lịch nghỉ dưỡng, phượt trải nghiệm và nghỉ dưỡng biển cao cấp. Giữ chỗ ngay 15 phút không lo mất vé!
          </p>
        </div>
      </div>

      {/* Main Grid: Sidebar Filters + Tour List */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* Sidebar Filter */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-[#e7e0d3] shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-[#f5f0e6] pb-4">
              <h3 className="font-bold text-stone-800 text-base flex items-center gap-2">
                <SlidersHorizontal size={18} className="text-[#E2603F]" /> Bộ Lọc Tìm Kiếm
              </h3>
              <button
                onClick={handleReset}
                className="text-xs text-stone-400 hover:text-[#E2603F] flex items-center gap-1 transition-colors"
                title="Đặt lại bộ lọc"
              >
                <RotateCcw size={12} /> Đặt lại
              </button>
            </div>

            {/* Keyword */}
            <div className="space-y-2">
              <label className="text-xs font-semibold text-stone-700">Từ khóa tìm kiếm</label>
              <div className="relative">
                <Search size={16} className="absolute left-3 top-3 text-stone-400" />
                <input
                  value={keyword}
                  onChange={(e) => { setKeyword(e.target.value); setPage(0); }}
                  placeholder="Nhập tên tour, mã tour..."
                  className="w-full pl-9 pr-3 py-2 text-sm bg-[#FBF8F2] border border-[#e7e0d3] rounded-xl focus:outline-none focus:border-[#E2603F]"
                />
              </div>
            </div>

            {/* Quick Destination Filter Chips */}
            <div className="space-y-2">
              <label className="text-xs font-semibold text-stone-700">Điểm đến phổ biến</label>
              <div className="flex flex-wrap gap-1.5">
                <button
                  onClick={() => { setDestination(""); setPage(0); }}
                  className={`text-xs px-2.5 py-1 rounded-lg border transition-all ${!destination ? "bg-[#E2603F] text-white border-[#E2603F] font-semibold" : "bg-[#FBF8F2] text-stone-600 border-[#e7e0d3] hover:bg-[#f5f0e6]"}`}
                >
                  Tất cả
                </button>
                {destinations.map((d) => (
                  <button
                    key={d.id}
                    onClick={() => { setDestination(d.city); setPage(0); }}
                    className={`text-xs px-2.5 py-1 rounded-lg border transition-all ${destination.toLowerCase() === d.city.toLowerCase() ? "bg-[#E2603F] text-white border-[#E2603F] font-semibold" : "bg-[#FBF8F2] text-stone-600 border-[#e7e0d3] hover:bg-[#f5f0e6]"}`}
                  >
                    {d.city}
                  </button>
                ))}
              </div>
            </div>

            {/* Price Filter */}
            <div className="space-y-3 pt-2 border-t border-[#f5f0e6]">
              <label className="text-xs font-semibold text-stone-700">Khoảng giá (VNĐ)</label>
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="number"
                  value={minPrice}
                  onChange={(e) => { setMinPrice(e.target.value); setPage(0); }}
                  placeholder="Giá từ"
                  className="w-full px-3 py-2 text-sm bg-[#FBF8F2] border border-[#e7e0d3] rounded-xl focus:outline-none focus:border-[#E2603F]"
                />
                <input
                  type="number"
                  value={maxPrice}
                  onChange={(e) => { setMaxPrice(e.target.value); setPage(0); }}
                  placeholder="Giá đến"
                  className="w-full px-3 py-2 text-sm bg-[#FBF8F2] border border-[#e7e0d3] rounded-xl focus:outline-none focus:border-[#E2603F]"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Tour List Content */}
        <div className="lg:col-span-3 space-y-6">
          <div className="flex items-center justify-between text-sm text-stone-500">
            <span>
              Tìm thấy <strong className="text-stone-800">{result?.totalElements ?? 0}</strong> tour phù hợp
            </span>
          </div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {result === null &&
              Array.from({ length: 9 }).map((_, i) => <TourCardSkeleton key={i} />)}
            {result?.empty && (
              <div className="col-span-full py-16 text-center space-y-3 bg-white rounded-2xl border border-[#e7e0d3]">
                <p className="text-stone-500 text-sm font-medium">
                  Không tìm thấy tour phù hợp với bộ lọc hiện tại.
                </p>
                <button
                  onClick={handleReset}
                  className="text-xs font-semibold text-[#E2603F] hover:text-[#d55333] underline"
                >
                  Xóa bộ lọc để xem toàn bộ Tour
                </button>
              </div>
            )}
            {result?.content.map((tour) => (
              <TourCard key={tour.tourId} tour={tour} />
            ))}
          </div>

          {/* Pagination */}
          {result && result.totalPages > 1 && (
            <div className="flex items-center justify-center gap-3 pt-6 border-t border-[#e7e0d3]">
              <button
                disabled={result.first}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="p-2.5 rounded-xl border border-[#e7e0d3] text-stone-600 disabled:opacity-30 hover:bg-[#f5f0e6] transition-colors"
              >
                <ChevronLeft size={18} />
              </button>
              <span className="text-sm font-medium text-stone-600">
                Trang {result.number + 1} / {result.totalPages}
              </span>
              <button
                disabled={result.last}
                onClick={() => setPage((p) => p + 1)}
                className="p-2.5 rounded-xl border border-[#e7e0d3] text-stone-600 disabled:opacity-30 hover:bg-[#f5f0e6] transition-colors"
              >
                <ChevronRight size={18} />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function ToursPage() {
  return (
    <Suspense>
      <ToursContent />
    </Suspense>
  );
}
