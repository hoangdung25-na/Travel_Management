"use client";

import { useEffect, useState } from "react";
import { Plus, MapPin, X, Image as ImageIcon } from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { DestinationVm } from "@/lib/types";
import { useToast } from "@/lib/toast-context";

export default function AdminDestinationsPage() {
  const { notify } = useToast();
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    name: "",
    city: "",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80",
  });

  useEffect(() => {
    loadDestinations();
  }, []);

  function loadDestinations() {
    tourService.listDestinations().then(setDestinations);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    try {
      await tourService.createDestination(form);
      notify("success", "Đã khởi tạo điểm đến thành công!");
      setForm({
        name: "",
        city: "",
        country: "Việt Nam",
        imageUrl: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80",
      });
      setShowModal(false);
      loadDestinations();
    } catch (err: any) {
      notify("error", err.message || "Tạo điểm đến thất bại");
    }
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-widest text-teal-600 font-bold block mb-1">
            Danh Mục Địa Lý
          </span>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Quản Lý Danh Sách Điểm Đến (Destinations)
          </h1>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-xl shadow-md transition-all shrink-0"
        >
          <Plus size={16} /> Thêm Điểm Đến Mới
        </button>
      </div>

      {/* Destination Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {destinations.map((d) => (
          <div key={d.id} className="bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-md transition-all">
            <div className="h-36 relative bg-slate-100">
              <img src={d.imageUrl} alt={d.name} className="w-full h-full object-cover" />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent" />
              <div className="absolute bottom-3 left-3 text-white">
                <p className="font-bold text-base leading-tight">{d.name}</p>
                <p className="text-xs text-teal-300">{d.city}, {d.country}</p>
              </div>
            </div>
            <div className="p-4 flex items-center justify-between text-xs text-slate-500">
              <span>Đang mở bán</span>
              <span className="font-bold text-slate-800">{d.tourCount ?? 10}+ Tour</span>
            </div>
          </div>
        ))}
      </div>

      {/* Modal: Create Destination */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 space-y-5 shadow-2xl animate-in fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="font-bold text-slate-900 text-base">Thêm Điểm Đến Mới (POST /api/v1/destinations)</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">Tên Điểm Đến / Danh Thắng</label>
                <input
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="Ví dụ: Bà Nà Hills - Cầu Vàng"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Thành Phố / Tỉnh Thành</label>
                <input
                  required
                  value={form.city}
                  onChange={(e) => setForm({ ...form, city: e.target.value })}
                  placeholder="Ví dụ: Đà Nẵng"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Quốc Gia</label>
                <input
                  required
                  value={form.country}
                  onChange={(e) => setForm({ ...form, country: e.target.value })}
                  placeholder="Việt Nam"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">Link Ảnh Minh Họa (Image URL)</label>
                <input
                  value={form.imageUrl}
                  onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                  placeholder="https://images.unsplash.com/..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono text-[11px]"
                />
              </div>

              <div className="pt-3 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 py-2.5 bg-slate-100 text-slate-700 font-semibold rounded-xl"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md"
                >
                  Lưu Điểm Đến
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
