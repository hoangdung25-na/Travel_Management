"use client";

import { useEffect, useState } from "react";
import { Plus, Edit2, Trash2, X, Image as ImageIcon, MapPin, Search, ExternalLink, Sparkles, AlertCircle } from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { DestinationVm } from "@/lib/types";
import { useToast } from "@/lib/toast-context";

const DEFAULT_IMAGE = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80";

const PRESET_DESTINATIONS = [
  {
    name: "Đà Nẵng & Hội An",
    city: "Đà Nẵng",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=800&q=80",
  },
  {
    name: "Đảo Ngọc Phú Quốc",
    city: "Phú Quốc",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1540555700478-4be289fbecef?auto=format&fit=crop&w=800&q=80",
  },
  {
    name: "Hà Giang Loop",
    city: "Hà Giang",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80",
  },
  {
    name: "Vịnh Biển Nha Trang",
    city: "Nha Trang",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80",
  },
  {
    name: "Vịnh Hạ Long",
    city: "Quảng Ninh",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80",
  },
  {
    name: "Sapa Mờ Sương",
    city: "Lào Cai",
    country: "Việt Nam",
    imageUrl: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
  },
];

export default function AdminDestinationsPage() {
  const { notify } = useToast();
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");

  // Modal State
  const [showModal, setShowModal] = useState(false);
  const [editingDestination, setEditingDestination] = useState<DestinationVm | null>(null);
  const [saving, setSaving] = useState(false);

  // Form State
  const [form, setForm] = useState({
    name: "",
    city: "",
    country: "Việt Nam",
    imageUrl: DEFAULT_IMAGE,
  });

  // Delete Confirm State
  const [deletingId, setDeletingId] = useState<string | null>(null);

  useEffect(() => {
    loadDestinations();
  }, []);

  async function loadDestinations() {
    setLoading(true);
    try {
      const data = await tourService.listDestinations();
      setDestinations(data);
    } catch (err: any) {
      notify("error", err.message || "Không thể tải danh sách điểm đến");
    } finally {
      setLoading(false);
    }
  }

  function handleOpenCreate() {
    setEditingDestination(null);
    setForm({
      name: "",
      city: "",
      country: "Việt Nam",
      imageUrl: DEFAULT_IMAGE,
    });
    setShowModal(true);
  }

  function handleOpenEdit(dest: DestinationVm) {
    setEditingDestination(dest);
    setForm({
      name: dest.name,
      city: dest.city,
      country: dest.country || "Việt Nam",
      imageUrl: dest.imageUrl || DEFAULT_IMAGE,
    });
    setShowModal(true);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.name.trim() || !form.city.trim()) {
      notify("error", "Vui lòng nhập đầy đủ tên và thành phố của điểm đến");
      return;
    }

    setSaving(true);
    try {
      if (editingDestination) {
        // Update existing destination
        await tourService.updateDestination(editingDestination.id, {
          name: form.name.trim(),
          city: form.city.trim(),
          country: form.country.trim(),
          imageUrl: form.imageUrl.trim() || DEFAULT_IMAGE,
        });
        notify("success", `Đã cập nhật điểm đến "${form.name}" thành công!`);
      } else {
        // Create new destination
        await tourService.createDestination({
          name: form.name.trim(),
          city: form.city.trim(),
          country: form.country.trim(),
          imageUrl: form.imageUrl.trim() || DEFAULT_IMAGE,
        });
        notify("success", `Đã tạo mới điểm đến "${form.name}" thành công!`);
      }
      setShowModal(false);
      loadDestinations();
    } catch (err: any) {
      notify("error", err.message || "Thao tác điểm đến thất bại");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: string, name: string) {
    if (!confirm(`Bạn có chắc chắn muốn xóa điểm đến "${name}" không?`)) {
      return;
    }

    setDeletingId(id);
    try {
      await tourService.deleteDestination(id);
      notify("success", `Đã xóa điểm đến "${name}" thành công!`);
      loadDestinations();
    } catch (err: any) {
      notify("error", err.message || "Không thể xóa điểm đến (có thể đang được gắn với Tour)");
    } finally {
      setDeletingId(null);
    }
  }

  const filteredDestinations = destinations.filter(
    (d) =>
      d.name.toLowerCase().includes(search.toLowerCase()) ||
      d.city.toLowerCase().includes(search.toLowerCase()) ||
      d.country.toLowerCase().includes(search.toLowerCase())
  );

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
          <p className="text-xs text-slate-500 mt-1">
            Cập nhật tên, thành phố, quốc gia và hình ảnh đại diện cho các điểm đến du lịch
          </p>
        </div>

        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-xl shadow-md hover:shadow-lg transition-all shrink-0"
        >
          <Plus size={16} /> Thêm Điểm Đến Mới
        </button>
      </div>

      {/* Filter / Search Bar */}
      <div className="flex items-center gap-3 bg-white p-3 rounded-2xl border border-slate-200 shadow-sm max-w-md">
        <Search size={18} className="text-slate-400 shrink-0 ml-1" />
        <input
          type="text"
          placeholder="Tìm theo tên điểm đến, thành phố..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full text-xs bg-transparent focus:outline-none text-slate-800"
        />
        {search && (
          <button onClick={() => setSearch("")} className="text-slate-400 hover:text-slate-600 text-xs">
            Xóa
          </button>
        )}
      </div>

      {/* Loading state */}
      {loading && (
        <div className="py-12 text-center text-slate-500 text-sm">
          Đang tải danh sách điểm đến...
        </div>
      )}

      {/* Destination Grid */}
      {!loading && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredDestinations.map((d) => {
            const displayImg = d.imageUrl && d.imageUrl.trim().length > 0 ? d.imageUrl : DEFAULT_IMAGE;

            return (
              <div
                key={d.id}
                className="group bg-white rounded-3xl border border-slate-200 overflow-hidden shadow-sm hover:shadow-lg hover:border-teal-200 transition-all flex flex-col justify-between"
              >
                {/* Image & Title Card */}
                <div>
                  <div className="h-44 relative bg-slate-100 overflow-hidden">
                    <img
                      src={displayImg}
                      alt={d.name}
                      onError={(e) => {
                        e.currentTarget.src = DEFAULT_IMAGE;
                      }}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-950/85 via-slate-900/30 to-transparent" />

                    {/* Quick Badge */}
                    <div className="absolute top-3 right-3 flex items-center gap-1.5 bg-black/40 backdrop-blur-md px-2.5 py-1 rounded-full text-[11px] text-white font-medium border border-white/20">
                      <MapPin size={11} className="text-teal-400" />
                      <span>{d.city}</span>
                    </div>

                    <div className="absolute bottom-3 left-3 right-3 text-white">
                      <p className="font-bold text-base leading-tight drop-shadow-sm">{d.name}</p>
                      <p className="text-xs text-teal-300 font-medium mt-0.5">
                        {d.city}, {d.country}
                      </p>
                    </div>
                  </div>

                  <div className="p-4 flex items-center justify-between text-xs text-slate-500 border-b border-slate-100">
                    <span className="inline-flex items-center gap-1.5">
                      <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
                      Đang mở bán
                    </span>
                    <span className="font-bold text-slate-800">{d.tourCount ?? 10}+ Tour</span>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="p-3 bg-slate-50 flex items-center justify-end gap-2">
                  <button
                    onClick={() => handleOpenEdit(d)}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-white hover:bg-teal-50 text-teal-700 hover:text-teal-800 border border-slate-200 hover:border-teal-300 font-semibold text-xs rounded-xl shadow-xs transition-all"
                  >
                    <Edit2 size={13} /> Sửa ảnh & Thông tin
                  </button>

                  <button
                    onClick={() => handleDelete(d.id, d.name)}
                    disabled={deletingId === d.id}
                    className="inline-flex items-center gap-1 px-3 py-1.5 bg-white hover:bg-rose-50 text-rose-600 hover:text-rose-700 border border-slate-200 hover:border-rose-300 font-semibold text-xs rounded-xl shadow-xs transition-all disabled:opacity-50"
                  >
                    <Trash2 size={13} /> {deletingId === d.id ? "Đang xóa..." : "Xóa"}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Empty State */}
      {!loading && filteredDestinations.length === 0 && (
        <div className="bg-white rounded-3xl border border-slate-200 p-12 text-center space-y-3">
          <AlertCircle size={36} className="mx-auto text-slate-400" />
          <h3 className="text-base font-bold text-slate-800">Không tìm thấy điểm đến phù hợp</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Hãy thử tìm với từ khóa khác hoặc nhấn "Thêm Điểm Đến Mới" để tạo điểm đến cho danh mục.
          </p>
        </div>
      )}

      {/* Modal: Create / Edit Destination */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-900/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 space-y-5 shadow-2xl animate-in fade-in max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="font-bold text-slate-900 text-base">
                  {editingDestination ? "Chỉnh Sửa Điểm Đến Du Lịch" : "Thêm Điểm Đến Mới"}
                </h3>
                <p className="text-[11px] text-slate-500">
                  {editingDestination
                    ? `PUT /api/v1/destinations/${editingDestination.id}`
                    : "POST /api/v1/destinations"}
                </p>
              </div>
              <button
                onClick={() => setShowModal(false)}
                className="text-slate-400 hover:text-slate-600 p-1 rounded-lg"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Tên Điểm Đến / Danh Thắng <span className="text-rose-500">*</span>
                </label>
                <input
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="Ví dụ: Bà Nà Hills - Cầu Vàng"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-medium"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 block mb-1">
                    Thành Phố / Tỉnh Thành <span className="text-rose-500">*</span>
                  </label>
                  <input
                    required
                    value={form.city}
                    onChange={(e) => setForm({ ...form, city: e.target.value })}
                    placeholder="Ví dụ: Đà Nẵng"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-semibold"
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
              </div>

              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Link Ảnh Minh Họa (Image URL)
                </label>
                <input
                  value={form.imageUrl}
                  onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                  placeholder="https://images.unsplash.com/photo-..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono text-[11px]"
                />
              </div>

              {/* Real-time Image Preview */}
              <div>
                <label className="font-semibold text-slate-700 block mb-1">
                  Xem Trước Hình Ảnh Hiển Thị (Live Preview)
                </label>
                <div className="h-36 rounded-2xl overflow-hidden bg-slate-100 border border-slate-200 relative group">
                  <img
                    src={form.imageUrl || DEFAULT_IMAGE}
                    alt="Preview"
                    onError={(e) => {
                      e.currentTarget.src = DEFAULT_IMAGE;
                    }}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent flex items-end p-3">
                    <p className="text-white text-xs font-bold drop-shadow">
                      {form.name || "Tên Điểm Đến"} ({form.city || "Thành Phố"})
                    </p>
                  </div>
                </div>
              </div>

              {/* Quick Preset Images */}
              <div>
                <label className="font-semibold text-slate-600 block mb-1.5 flex items-center gap-1">
                  <Sparkles size={12} className="text-teal-600" /> Chọn nhanh ảnh mẫu danh lam thắng cảnh Việt Nam:
                </label>
                <div className="flex flex-wrap gap-1.5">
                  {PRESET_DESTINATIONS.map((preset) => (
                    <button
                      key={preset.name}
                      type="button"
                      onClick={() =>
                        setForm((prev) => ({
                          ...prev,
                          imageUrl: preset.imageUrl,
                          city: prev.city ? prev.city : preset.city,
                        }))
                      }
                      className="px-2.5 py-1 bg-slate-100 hover:bg-teal-50 hover:text-teal-700 border border-slate-200 rounded-lg text-[11px] font-medium transition-all"
                    >
                      {preset.name}
                    </button>
                  ))}
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-all"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md transition-all disabled:opacity-50"
                >
                  {saving
                    ? "Đang lưu..."
                    : editingDestination
                    ? "Lưu Thay Đổi"
                    : "Tạo Điểm Đến"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
