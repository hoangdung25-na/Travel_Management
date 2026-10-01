import Link from "next/link";
import { Users, Star, Clock, MapPin, ArrowRight } from "lucide-react";
import { TourVm } from "@/lib/types";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { formatCurrency } from "@/lib/format";

export function TourCard({ tour }: { tour: TourVm }) {
  const imageUrl =
    tour.imageUrl ??
    "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80";

  const minPrice =
    tour.minPrice ??
    ((tour as any).schedules && (tour as any).schedules.length > 0
      ? (tour as any).schedules[0].priceAdult
      : 0);

  const availableSeats =
    tour.availableSeats ??
    ((tour as any).schedules && (tour as any).schedules.length > 0
      ? (tour as any).schedules[0].availableSeats
      : 0);

  return (
    <Link
      href={`/tours/${tour.tourId}`}
      className="group block rounded-2xl bg-white border border-[#e7e0d3] overflow-hidden shadow-sm hover:shadow-xl hover:border-[#E2603F]/40 transition-all duration-300 flex flex-col h-full"
    >
      {/* Tour Image & Badges */}
      <div className="relative h-48 w-full overflow-hidden bg-stone-100">
        <img
          src={imageUrl}
          alt={tour.title}
          className="h-full w-full object-cover group-hover:scale-105 transition-transform duration-500"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0D2B2B]/90 via-transparent to-transparent" />

        {/* Top Badges */}
        <div className="absolute top-3 left-3 right-3 flex items-center justify-between">
          <span className="font-mono text-[10px] uppercase font-bold tracking-wider bg-[#0D2B2B]/90 text-[#D4A24C] px-2.5 py-1 rounded-md backdrop-blur-md border border-[#D4A24C]/30">
            {tour.code}
          </span>
          <StatusBadge status={tour.status} />
        </div>

        {/* Bottom Location & Duration */}
        <div className="absolute bottom-3 left-3 right-3 flex items-center justify-between text-xs text-white">
          <span className="flex items-center gap-1 bg-black/40 px-2 py-0.5 rounded-md backdrop-blur-sm">
            <MapPin size={12} className="text-[#E2603F]" />
            {tour.destinationCity ?? "Việt Nam"}
          </span>
          <span className="flex items-center gap-1 bg-black/40 px-2 py-0.5 rounded-md backdrop-blur-sm">
            <Clock size={12} className="text-[#D4A24C]" />
            {tour.duration ?? "3N2Đ"}
          </span>
        </div>
      </div>

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col justify-between bg-white">
        <div>
          {/* Rating */}
          <div className="flex items-center gap-1 text-xs text-[#D4A24C] font-semibold mb-1.5">
            <Star size={14} className="fill-[#D4A24C] text-[#D4A24C]" />
            <span>{tour.rating ?? 4.9}</span>
            <span className="text-stone-400 font-normal">({tour.reviewCount ?? 120} đánh giá)</span>
          </div>

          {/* Title */}
          <h3 className="font-bold text-stone-900 text-base leading-snug line-clamp-2 mb-3 group-hover:text-[#E2603F] transition-colors">
            {tour.title}
          </h3>
        </div>

        {/* Footer info & Price */}
        <div className="pt-3 border-t border-[#f5f0e6] flex items-center justify-between mt-2">
          <div className="flex items-center gap-1 text-xs font-medium text-stone-500">
            <Users size={14} className="text-stone-400" />
            <span>{availableSeats > 0 ? `Còn ${availableSeats} chỗ` : "Hết chỗ"}</span>
          </div>

          <div className="text-right">
            <span className="text-[10px] text-stone-400 block uppercase tracking-wider">Từ</span>
            <span className="font-bold text-lg text-[#E2603F] leading-none">
              {formatCurrency(minPrice)}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}
