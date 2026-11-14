"use client";

import { useEffect, useState, useMemo } from "react";
import {
  Plus,
  Calendar,
  MapPin,
  Clock,
  Tag,
  CheckCircle2,
  X,
  Edit,
  Trash2,
  Eye,
  Image as ImageIcon,
  Search,
  Filter,
  Loader2,
  AlertCircle,
  ExternalLink,
  Star,
} from "lucide-react";
import { tourService } from "@/lib/services/tour-service";
import { TourDetailVm, TourVm, DestinationVm, TourStatus, TourImageVm } from "@/lib/types";
import { formatCurrency, formatDateTime } from "@/lib/format";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function AdminToursPage() {
  const { notify } = useToast();
  const [tours, setTours] = useState<TourVm[]>([]);
  const [destinations, setDestinations] = useState<DestinationVm[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingEditId, setLoadingEditId] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Search & Filter state
  const [searchKeyword, setSearchKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState<TourDetailVm | null>(null);
  const [showScheduleModal, setShowScheduleModal] = useState<string | null>(null);

  // New Tour Form
  const [newTourForm, setNewTourForm] = useState({
    code: "",
    title: "",
    description: "",
    duration: "3 Ngày 2 Đêm",
    destinationId: "",
    imageUrl: "",
    status: "PUBLISHED" as TourStatus,
    day1Title: "Khám phá địa danh nổi tiếng",
    day1Content: "Xe và HDV đưa đón tham quan các danh thắng và thưởng thức đặc sản.",
    images: [] as TourImageVm[],
  });

  // Edit Tour Form
  const [editTourForm, setEditTourForm] = useState({
    title: "",
    description: "",
    duration: "3 Ngày 2 Đêm",
    destinationId: "",
    imageUrl: "",
    status: "PUBLISHED" as TourStatus,
    images: [] as TourImageVm[],
  });

  // New Schedule Form
  const [newSchForm, setNewSchForm] = useState({
    departureTime: "2026-11-01T07:30",
    arrivalTime: "2026-11-03T18:00",
    priceAdult: "4500000",
    priceChild: "3150000",
    totalSeats: "30",
  });

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [tourRes, destRes] = await Promise.all([
        tourService.search({ size: 100, allStatus: true } as any),
        tourService.listDestinations().catch(() => []),
      ]);
      setTours(tourRes.content || []);
      setDestinations(destRes || []);
    } catch (err: any) {
      notify("error", err.message || "Không thể tải danh sách tour");
    } finally {
      setLoading(false);
    }
  }

  // Filtered tours
  const filteredTours = useMemo(() => {
    return tours.filter((t) => {
      const matchKeyword =
        !searchKeyword.trim() ||
        t.title?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        t.code?.toLowerCase().includes(searchKeyword.toLowerCase()) ||
        t.destinationCity?.toLowerCase().includes(searchKeyword.toLowerCase());

      const matchStatus =
        statusFilter === "ALL" || t.status === statusFilter;

      return matchKeyword && matchStatus;
    });
  }, [tours, searchKeyword, statusFilter]);

  // Statistics
  const stats = useMemo(() => {
    const total = tours.length;
    const published = tours.filter((t) => t.status === "PUBLISHED").length;
    const drafts = tours.filter((t) => t.status === "DRAFT").length;
    const archived = tours.filter((t) => t.status === "ARCHIVED").length;
    return { total, published, drafts, archived };
  }, [tours]);

  function handleDestinationChange(destId: string, isEdit = false) {
    const selectedDest = destinations.find((d) => d.id === destId);
    if (isEdit) {
      setEditTourForm((prev) => ({
        ...prev,
        destinationId: destId,
        imageUrl: prev.imageUrl || selectedDest?.imageUrl || "",
      }));
    } else {
      setNewTourForm((prev) => ({
        ...prev,
        destinationId: destId,
        imageUrl: prev.imageUrl || selectedDest?.imageUrl || "",
      }));
    }
  }

  function addNewImage() {
    setNewTourForm((prev) => ({
      ...prev,
      images: [
        ...prev.images,
        {
          imageUrl: "",
          caption: "",
          isPrimary: prev.images.length === 0,
          displayOrder: prev.images.length,
        },
      ],
    }));
  }

  function updateNewImage(index: number, field: string, val: any) {
    setNewTourForm((prev) => {
      const updated = [...prev.images];
      updated[index] = { ...updated[index], [field]: val };
      if (field === "isPrimary" && val === true) {
        updated.forEach((img, i) => {
          if (i !== index) img.isPrimary = false;
        });
      }
      return { ...prev, images: updated };
    });
  }

  function removeNewImage(index: number) {
    setNewTourForm((prev) => {
      const updated = prev.images.filter((_, i) => i !== index);
      if (updated.length > 0 && !updated.some((i) => i.isPrimary)) {
        updated[0].isPrimary = true;
      }
      return { ...prev, images: updated };
    });
  }

  function addEditImage() {
    setEditTourForm((prev) => ({
      ...prev,
      images: [
        ...prev.images,
        {
          imageUrl: "",
          caption: "",
          isPrimary: prev.images.length === 0,
          displayOrder: prev.images.length,
        },
      ],
    }));
  }

  function updateEditImage(index: number, field: string, val: any) {
    setEditTourForm((prev) => {
      const updated = [...prev.images];
      updated[index] = { ...updated[index], [field]: val };
      if (field === "isPrimary" && val === true) {
        updated.forEach((img, i) => {
          if (i !== index) img.isPrimary = false;
        });
      }
      return { ...prev, images: updated };
    });
  }

  function removeEditImage(index: number) {
    setEditTourForm((prev) => {
      const updated = prev.images.filter((_, i) => i !== index);
      if (updated.length > 0 && !updated.some((i) => i.isPrimary)) {
        updated[0].isPrimary = true;
      }
      return { ...prev, images: updated };
    });
  }

  async function handleCreateTour(e: React.FormEvent) {
    e.preventDefault();
    setIsSaving(true);
    try {
      const primaryImg = newTourForm.images.find((img) => img.isPrimary) || newTourForm.images[0];
      const mainImageUrl = primaryImg?.imageUrl || newTourForm.imageUrl.trim() || undefined;

      await tourService.create({
        code: newTourForm.code.trim().toUpperCase() || `TOUR-${Date.now().toString().slice(-4)}`,
        title: newTourForm.title.trim(),
        description: newTourForm.description.trim(),
        duration: newTourForm.duration.trim(),
        imageUrl: mainImageUrl,
        status: newTourForm.status,
        destinationIds: newTourForm.destinationId ? [newTourForm.destinationId] : [],
        images: newTourForm.images.map((img, idx) => ({
          imageUrl: img.imageUrl.trim(),
          caption: img.caption?.trim() || "",
          displayOrder: img.displayOrder ?? idx,
          isPrimary: Boolean(img.isPrimary || (idx === 0 && !newTourForm.images.some((i) => i.isPrimary))),
        })),
        itineraries: [
          {
            dayNumber: 1,
            title: newTourForm.day1Title.trim(),
            content: newTourForm.day1Content.trim(),
          },
        ],
      });
      notify("success", "Khởi tạo Tour mới thành công và đã cập nhật danh mục!");
      setShowCreateModal(false);
      setNewTourForm({
        code: "",
        title: "",
        description: "",
        duration: "3 Ngày 2 Đêm",
        destinationId: "",
        imageUrl: "",
        status: "PUBLISHED",
        day1Title: "Khám phá địa danh nổi tiếng",
        day1Content: "Xe và HDV đưa đón tham quan các danh thắng và thưởng thức đặc sản.",
        images: [],
      });
      loadData();
    } catch (err: any) {
      notify("error", err.message || "Tạo tour thất bại");
    } finally {
      setIsSaving(false);
    }
  }

  async function openEditModal(tour: TourVm) {
    setLoadingEditId(tour.tourId);
    try {
      const detail = await tourService.getById(tour.tourId);
      setShowEditModal(detail);

      let initialImages: TourImageVm[] = [];
      if (detail.images && detail.images.length > 0) {
        initialImages = detail.images.map((img, idx) => ({
          ...img,
          displayOrder: img.displayOrder ?? idx,
        }));
      } else if (detail.imageUrl) {
        initialImages = [
          {
            imageUrl: detail.imageUrl,
            caption: detail.title,
            isPrimary: true,
            displayOrder: 0,
          },
        ];
      }

      setEditTourForm({
        title: detail.title || "",
        description: detail.description || "",
        duration: detail.duration || "3 Ngày 2 Đêm",
        destinationId: detail.destinations?.[0]?.id || "",
        imageUrl: detail.imageUrl || initialImages[0]?.imageUrl || "",
        status: (detail.status as TourStatus) || "PUBLISHED",
        images: initialImages,
      });
    } catch (err: any) {
      notify("error", err.message || "Không thể tải chi tiết tour");
    } finally {
      setLoadingEditId(null);
    }
  }

  async function handleUpdateTour(e: React.FormEvent) {
    e.preventDefault();
    if (!showEditModal) return;
    setIsSaving(true);
    try {
      const primaryImg = editTourForm.images.find((img) => img.isPrimary) || editTourForm.images[0];
      const mainImageUrl = primaryImg?.imageUrl || editTourForm.imageUrl.trim() || undefined;

      await tourService.update(showEditModal.tourId, {
        title: editTourForm.title.trim(),
        description: editTourForm.description.trim(),
        status: editTourForm.status,
        duration: editTourForm.duration.trim(),
        imageUrl: mainImageUrl,
        destinationIds: editTourForm.destinationId ? [editTourForm.destinationId] : [],
        images: editTourForm.images.map((img, idx) => ({
          imageUrl: img.imageUrl.trim(),
          caption: img.caption?.trim() || "",
          displayOrder: img.displayOrder ?? idx,
          isPrimary: Boolean(img.isPrimary || (idx === 0 && !editTourForm.images.some((i) => i.isPrimary))),
        })),
      });
      notify("success", `Cập nhật Tour "${editTourForm.title}" thành công!`);
      setShowEditModal(null);
      loadData();
    } catch (err: any) {
      notify("error", err.message || "Cập nhật tour thất bại");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteTour(tour: TourVm) {
    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn xóa tour "${tour.title}" (${tour.code}) không?\nHành động này không thể hoàn tác.`
    );
    if (!confirmed) return;

    try {
      await tourService.delete(tour.tourId);
      notify("success", "Đã xóa tour thành công!");
      loadData();
    } catch (err: any) {
      notify("error", err.message || "Xóa tour thất bại");
    }
  }

  async function handleAddSchedule(e: React.FormEvent) {
    e.preventDefault();
    if (!showScheduleModal) return;
    setIsSaving(true);
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
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-teal-600 font-bold mb-1">
            <span>Danh Mục Quản Trị</span>
            <span>•</span>
            <span className="text-slate-400">Tours & Schedules</span>
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            Quản Lý Tour & Lịch Khởi Hành
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Theo dõi, chỉnh sửa giá bán, cập nhật lịch trình khởi hành và quản lý trạng thái tour toàn hệ thống.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center justify-center gap-2 bg-gradient-to-r from-teal-600 to-emerald-600 hover:from-teal-500 hover:to-emerald-500 text-white font-bold text-xs px-5 py-3 rounded-2xl shadow-md shadow-teal-600/20 hover:shadow-lg transition-all shrink-0"
        >
          <Plus size={16} /> Tạo Tour Danh Mục Mới
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-slate-500">Tổng Số Tour</p>
          <p className="text-2xl font-black text-slate-900 mt-1">{stats.total}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-teal-600">Đang Mở Bán</p>
          <p className="text-2xl font-black text-teal-600 mt-1">{stats.published}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-amber-600">Bản Nháp (Draft)</p>
          <p className="text-2xl font-black text-amber-600 mt-1">{stats.drafts}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-slate-400">Đã Lưu Trữ</p>
          <p className="text-2xl font-black text-slate-500 mt-1">{stats.archived}</p>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative flex-1 w-full sm:max-w-md">
          <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            placeholder="Tìm theo tên tour, mã tour hoặc điểm đến..."
            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs focus:outline-none focus:border-teal-500 focus:bg-white transition-colors"
          />
          {searchKeyword && (
            <button
              onClick={() => setSearchKeyword("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X size={14} />
            </button>
          )}
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-xl text-xs">
            <Filter size={14} className="text-slate-400" />
            <span className="font-semibold text-slate-600">Trạng thái:</span>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="bg-transparent font-bold text-slate-800 focus:outline-none cursor-pointer"
            >
              <option value="ALL">Tất cả ({tours.length})</option>
              <option value="PUBLISHED">Mở bán ({stats.published})</option>
              <option value="DRAFT">Nháp ({stats.drafts})</option>
              <option value="ARCHIVED">Lưu trữ ({stats.archived})</option>
            </select>
          </div>
        </div>
      </div>

      {/* Tour List Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 font-bold border-b border-slate-200/80 uppercase tracking-wider text-[11px]">
                <th className="py-4 px-4 w-16 text-center">Ảnh</th>
                <th className="py-4 px-4 whitespace-nowrap">Mã Tour</th>
                <th className="py-4 px-4 min-w-[260px]">Tên Tour Danh Mục</th>
                <th className="py-4 px-4 whitespace-nowrap">Điểm Đến</th>
                <th className="py-4 px-4 whitespace-nowrap text-right">Giá Khởi Điểm</th>
                <th className="py-4 px-4 whitespace-nowrap text-center">Chỗ Khả Dụng</th>
                <th className="py-4 px-4 whitespace-nowrap text-center">Trạng Thái</th>
                <th className="py-4 px-4 whitespace-nowrap text-right pr-6">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-16 text-center text-slate-400">
                    <Loader2 size={24} className="animate-spin mx-auto mb-2 text-teal-600" />
                    Đang tải dữ liệu tour...
                  </td>
                </tr>
              ) : filteredTours.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-16 text-center text-slate-400">
                    <AlertCircle size={24} className="mx-auto mb-2 text-slate-300" />
                    Không tìm thấy tour nào phù hợp với bộ lọc.
                  </td>
                </tr>
              ) : (
                filteredTours.map((t) => (
                  <tr key={t.tourId} className="hover:bg-slate-50/70 transition-colors">
                    {/* 1. Image */}
                    <td className="py-3 px-4 align-middle text-center">
                      <div className="w-12 h-10 rounded-lg overflow-hidden border border-slate-200 shadow-xs mx-auto bg-slate-100">
                        <img
                          src={
                            t.imageUrl ||
                            "https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?auto=format&fit=crop&w=300&q=80"
                          }
                          alt={t.title}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    </td>

                    {/* 2. Tour Code */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap">
                      <span className="font-mono text-xs font-bold text-teal-700 bg-teal-50 px-2.5 py-1 rounded-md border border-teal-200/60 inline-block">
                        {t.code}
                      </span>
                    </td>

                    {/* 3. Title & Duration */}
                    <td className="py-3.5 px-4 align-middle">
                      <div className="font-bold text-slate-900 line-clamp-1 max-w-md">
                        {t.title}
                      </div>
                      <div className="flex items-center gap-2 mt-0.5 text-[11px] text-slate-400">
                        <span className="inline-flex items-center gap-1">
                          <Clock size={11} className="text-teal-600" />
                          {t.duration || "3 Ngày 2 Đêm"}
                        </span>
                      </div>
                    </td>

                    {/* 4. Destination */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-slate-600">
                      <span className="inline-flex items-center gap-1 font-medium">
                        <MapPin size={12} className="text-rose-500" />
                        {t.destinationCity || "Việt Nam"}
                      </span>
                    </td>

                    {/* 5. Price */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-right">
                      <span className="font-extrabold text-rose-600 font-mono text-[13px]">
                        {formatCurrency(t.minPrice)}
                      </span>
                    </td>

                    {/* 6. Seats */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-center">
                      <span className="bg-slate-100 text-slate-700 px-2.5 py-1 rounded-full font-bold text-[11px] inline-block">
                        {t.availableSeats} chỗ
                      </span>
                    </td>

                    {/* 7. Status */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-center">
                      <StatusBadge status={t.status} />
                    </td>

                    {/* 8. Actions */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-right pr-6">
                      <div className="inline-flex items-center gap-1.5">
                        <button
                          onClick={() => setShowScheduleModal(t.tourId)}
                          title="Thêm lịch khởi hành mới cho tour này"
                          className="bg-teal-50 text-teal-700 hover:bg-teal-100 active:bg-teal-200 font-bold px-2.5 py-1.5 rounded-lg border border-teal-200/80 transition-colors inline-flex items-center gap-1 text-[11px]"
                        >
                          + Lịch Đi
                        </button>

                        <button
                          onClick={() => openEditModal(t)}
                          disabled={loadingEditId === t.tourId}
                          title="Chỉnh sửa thông tin Tour"
                          className="bg-amber-50 text-amber-700 hover:bg-amber-100 active:bg-amber-200 font-bold px-2.5 py-1.5 rounded-lg border border-amber-200/80 transition-colors inline-flex items-center gap-1 text-[11px] disabled:opacity-50"
                        >
                          {loadingEditId === t.tourId ? (
                            <Loader2 size={12} className="animate-spin text-amber-700" />
                          ) : (
                            <Edit size={12} />
                          )}
                          Sửa
                        </button>

                        <button
                          onClick={() => handleDeleteTour(t)}
                          title="Xóa Tour khỏi hệ thống"
                          className="bg-rose-50 text-rose-700 hover:bg-rose-100 active:bg-rose-200 font-bold px-2 py-1.5 rounded-lg border border-rose-200/80 transition-colors inline-flex items-center gap-1 text-[11px]"
                        >
                          <Trash2 size={12} /> Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal 1: Create Tour */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-xl w-full p-6 space-y-4 shadow-2xl animate-in fade-in max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="font-extrabold text-slate-900 text-base">Thêm Tour Mới Vào Danh Mục</h3>
                <p className="text-xs text-slate-500">Khởi tạo sản phẩm du lịch mới cho khách hàng đặt mua.</p>
              </div>
              <button
                onClick={() => setShowCreateModal(false)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
              >
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleCreateTour} className="space-y-3.5 text-xs">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Mã Tour (Unique Code)</label>
                  <input
                    required
                    value={newTourForm.code}
                    onChange={(e) => setNewTourForm({ ...newTourForm, code: e.target.value.toUpperCase() })}
                    placeholder="VD: TOUR-NA-3N2D"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono font-bold"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Thời Lượng</label>
                  <input
                    value={newTourForm.duration}
                    onChange={(e) => setNewTourForm({ ...newTourForm, duration: e.target.value })}
                    placeholder="VD: 3 Ngày 2 Đêm"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-medium"
                  />
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Tên Tour Chi Tiết</label>
                <input
                  required
                  value={newTourForm.title}
                  onChange={(e) => setNewTourForm({ ...newTourForm, title: e.target.value })}
                  placeholder="VD: Tour Khám Phá Nghệ An - Cửa Lò - Quê Bác 3N2Đ..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Mô Tả Tour</label>
                <textarea
                  required
                  value={newTourForm.description}
                  onChange={(e) => setNewTourForm({ ...newTourForm, description: e.target.value })}
                  placeholder="Mô tả hành trình, điểm đến nổi bật, dịch vụ bao gồm..."
                  rows={3}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 leading-relaxed"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Điểm Đến Liên Kết</label>
                  <select
                    value={newTourForm.destinationId}
                    onChange={(e) => handleDestinationChange(e.target.value, false)}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-medium cursor-pointer"
                  >
                    <option value="">-- Chọn điểm đến --</option>
                    {destinations.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.city} ({d.name})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Trạng Thái Khởi Tạo</label>
                  <select
                    value={newTourForm.status}
                    onChange={(e) => setNewTourForm({ ...newTourForm, status: e.target.value as TourStatus })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold cursor-pointer"
                  >
                    <option value="PUBLISHED">PUBLISHED - Đang mở bán</option>
                    <option value="DRAFT">DRAFT - Lưu nháp (Ẩn)</option>
                    <option value="ARCHIVED">ARCHIVED - Dừng mở bán</option>
                  </select>
                </div>
              </div>

              <div className="space-y-2 p-3 bg-slate-50 rounded-2xl border border-slate-200/80">
                <div className="flex items-center justify-between">
                  <label className="font-bold text-slate-800 text-xs flex items-center gap-1.5">
                    <ImageIcon size={14} className="text-teal-600" /> Thư Viện Ảnh Tour (Gallery)
                  </label>
                  <button
                    type="button"
                    onClick={addNewImage}
                    className="text-teal-700 hover:text-teal-800 font-bold text-[11px] bg-teal-100/70 hover:bg-teal-100 px-2 py-0.5 rounded-lg flex items-center gap-1 transition-colors"
                  >
                    <Plus size={12} /> Thêm ảnh
                  </button>
                </div>
                <p className="text-[11px] text-slate-500">
                  Thêm các hình ảnh thực tế (khách sạn, cảnh quan, ẩm thực) để hiển thị trên trang chi tiết.
                </p>

                {newTourForm.images.length === 0 ? (
                  <div className="space-y-2">
                    <input
                      value={newTourForm.imageUrl}
                      onChange={(e) => setNewTourForm({ ...newTourForm, imageUrl: e.target.value })}
                      placeholder="https://images.unsplash.com/... (URL ảnh đại diện)"
                      className="w-full p-2 bg-white border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono text-[11px]"
                    />
                    <button
                      type="button"
                      onClick={() => {
                        if (newTourForm.imageUrl.trim()) {
                          addNewImage();
                          updateNewImage(0, "imageUrl", newTourForm.imageUrl.trim());
                        } else {
                          addNewImage();
                        }
                      }}
                      className="w-full py-1.5 text-xs text-teal-700 font-semibold border border-dashed border-teal-300 rounded-xl bg-teal-50/50 hover:bg-teal-50 transition-colors"
                    >
                      + Tạo bộ sưu tập nhiều ảnh cho Tour này
                    </button>
                  </div>
                ) : (
                  <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                    {newTourForm.images.map((img, idx) => (
                      <div key={idx} className="bg-white p-2.5 rounded-xl border border-slate-200/80 space-y-1.5 shadow-sm">
                        <div className="flex items-center gap-2">
                          {img.imageUrl ? (
                            <img src={img.imageUrl} alt="preview" className="w-10 h-10 rounded-lg object-cover border border-slate-200 shrink-0" />
                          ) : (
                            <div className="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400 shrink-0 border border-slate-200">
                              <ImageIcon size={14} />
                            </div>
                          )}
                          <div className="flex-1 space-y-1">
                            <input
                              value={img.imageUrl}
                              onChange={(e) => updateNewImage(idx, "imageUrl", e.target.value)}
                              placeholder="URL ảnh (https://...)"
                              className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded-lg font-mono text-[10px]"
                            />
                            <input
                              value={img.caption || ""}
                              onChange={(e) => updateNewImage(idx, "caption", e.target.value)}
                              placeholder="Chú thích ảnh (VD: Cầu Vàng Bà Nà Hills...)"
                              className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded-lg text-[10px]"
                            />
                          </div>
                          <div className="flex flex-col gap-1 shrink-0">
                            <button
                              type="button"
                              onClick={() => updateNewImage(idx, "isPrimary", true)}
                              title={img.isPrimary ? "Ảnh đại diện chính" : "Đặt làm ảnh chính"}
                              className={`p-1.5 rounded-lg border text-[10px] flex items-center justify-center ${
                                img.isPrimary
                                  ? "bg-amber-50 text-amber-600 border-amber-300 font-bold"
                                  : "bg-slate-50 text-slate-400 border-slate-200 hover:text-slate-600"
                              }`}
                            >
                              <Star size={12} className={img.isPrimary ? "fill-amber-500 text-amber-500" : ""} />
                            </button>
                            <button
                              type="button"
                              onClick={() => removeNewImage(idx)}
                              title="Xóa ảnh này"
                              className="p-1.5 rounded-lg border border-red-200 bg-red-50 text-red-600 hover:bg-red-100 flex items-center justify-center"
                            >
                              <Trash2 size={12} />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-4 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {isSaving && <Loader2 size={14} className="animate-spin" />}
                  {isSaving ? "Đang tạo..." : "Tạo Tour Mới"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 2: Edit Tour */}
      {showEditModal && (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-xl w-full p-6 space-y-4 shadow-2xl animate-in fade-in max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="font-extrabold text-slate-900 text-base">Cập Nhật Thông Tin Tour</h3>
                  <span className="font-mono text-xs text-teal-700 bg-teal-50 px-2 py-0.5 rounded font-bold border border-teal-200">
                    {showEditModal.code}
                  </span>
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  Chỉnh sửa chi tiết nội dung, trạng thái mở bán và hình ảnh.
                </p>
              </div>
              <button
                onClick={() => setShowEditModal(null)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
              >
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleUpdateTour} className="space-y-3.5 text-xs">
              <div>
                <label className="font-bold text-slate-700 block mb-1">Tên Tour Chi Tiết</label>
                <input
                  required
                  value={editTourForm.title}
                  onChange={(e) => setEditTourForm({ ...editTourForm, title: e.target.value })}
                  placeholder="Tên tour chi tiết..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Mô Tả Tour</label>
                <textarea
                  required
                  value={editTourForm.description}
                  onChange={(e) => setEditTourForm({ ...editTourForm, description: e.target.value })}
                  rows={3}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 leading-relaxed"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Thời Lượng</label>
                  <input
                    value={editTourForm.duration}
                    onChange={(e) => setEditTourForm({ ...editTourForm, duration: e.target.value })}
                    placeholder="3 Ngày 2 Đêm"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-medium"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Trạng Thái Tour</label>
                  <select
                    value={editTourForm.status}
                    onChange={(e) => setEditTourForm({ ...editTourForm, status: e.target.value as TourStatus })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold cursor-pointer"
                  >
                    <option value="PUBLISHED">PUBLISHED - Đang mở bán</option>
                    <option value="DRAFT">DRAFT - Lưu nháp (Ẩn)</option>
                    <option value="ARCHIVED">ARCHIVED - Dừng bán / Lưu trữ</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Điểm Đến Liên Kết</label>
                <select
                  value={editTourForm.destinationId}
                  onChange={(e) => handleDestinationChange(e.target.value, true)}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-medium cursor-pointer"
                >
                  <option value="">-- Chọn điểm đến --</option>
                  {destinations.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.city} ({d.name})
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-2 p-3 bg-slate-50 rounded-2xl border border-slate-200/80">
                <div className="flex items-center justify-between">
                  <label className="font-bold text-slate-800 text-xs flex items-center gap-1.5">
                    <ImageIcon size={14} className="text-teal-600" /> Thư Viện Ảnh Tour (Gallery)
                  </label>
                  <button
                    type="button"
                    onClick={addEditImage}
                    className="text-teal-700 hover:text-teal-800 font-bold text-[11px] bg-teal-100/70 hover:bg-teal-100 px-2 py-0.5 rounded-lg flex items-center gap-1 transition-colors"
                  >
                    <Plus size={12} /> Thêm ảnh
                  </button>
                </div>
                <p className="text-[11px] text-slate-500">
                  Quản lý danh sách ảnh của Tour. Bấm vào ngôi sao <Star size={10} className="inline fill-amber-500 text-amber-500" /> để chọn ảnh đại diện chính.
                </p>

                {editTourForm.images.length === 0 ? (
                  <div className="space-y-2">
                    <input
                      value={editTourForm.imageUrl}
                      onChange={(e) => setEditTourForm({ ...editTourForm, imageUrl: e.target.value })}
                      placeholder="https://images.unsplash.com/... (URL ảnh đại diện)"
                      className="w-full p-2 bg-white border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono text-[11px]"
                    />
                    <button
                      type="button"
                      onClick={() => {
                        if (editTourForm.imageUrl.trim()) {
                          addEditImage();
                          updateEditImage(0, "imageUrl", editTourForm.imageUrl.trim());
                        } else {
                          addEditImage();
                        }
                      }}
                      className="w-full py-1.5 text-xs text-teal-700 font-semibold border border-dashed border-teal-300 rounded-xl bg-teal-50/50 hover:bg-teal-50 transition-colors"
                    >
                      + Tạo bộ sưu tập nhiều ảnh cho Tour này
                    </button>
                  </div>
                ) : (
                  <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                    {editTourForm.images.map((img, idx) => (
                      <div key={idx} className="bg-white p-2.5 rounded-xl border border-slate-200/80 space-y-1.5 shadow-sm">
                        <div className="flex items-center gap-2">
                          {img.imageUrl ? (
                            <img src={img.imageUrl} alt="preview" className="w-10 h-10 rounded-lg object-cover border border-slate-200 shrink-0" />
                          ) : (
                            <div className="w-10 h-10 rounded-lg bg-slate-100 flex items-center justify-center text-slate-400 shrink-0 border border-slate-200">
                              <ImageIcon size={14} />
                            </div>
                          )}
                          <div className="flex-1 space-y-1">
                            <input
                              value={img.imageUrl}
                              onChange={(e) => updateEditImage(idx, "imageUrl", e.target.value)}
                              placeholder="URL ảnh (https://...)"
                              className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded-lg font-mono text-[10px]"
                            />
                            <input
                              value={img.caption || ""}
                              onChange={(e) => updateEditImage(idx, "caption", e.target.value)}
                              placeholder="Chú thích ảnh (VD: Cầu Vàng Bà Nà Hills...)"
                              className="w-full p-1.5 bg-slate-50 border border-slate-200 rounded-lg text-[10px]"
                            />
                          </div>
                          <div className="flex flex-col gap-1 shrink-0">
                            <button
                              type="button"
                              onClick={() => updateEditImage(idx, "isPrimary", true)}
                              title={img.isPrimary ? "Ảnh đại diện chính" : "Đặt làm ảnh chính"}
                              className={`p-1.5 rounded-lg border text-[10px] flex items-center justify-center ${
                                img.isPrimary
                                  ? "bg-amber-50 text-amber-600 border-amber-300 font-bold"
                                  : "bg-slate-50 text-slate-400 border-slate-200 hover:text-slate-600"
                              }`}
                            >
                              <Star size={12} className={img.isPrimary ? "fill-amber-500 text-amber-500" : ""} />
                            </button>
                            <button
                              type="button"
                              onClick={() => removeEditImage(idx)}
                              title="Xóa ảnh này"
                              className="p-1.5 rounded-lg border border-red-200 bg-red-50 text-red-600 hover:bg-red-100 flex items-center justify-center"
                            >
                              <Trash2 size={12} />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Existing Schedules Preview */}
              {showEditModal.schedules && showEditModal.schedules.length > 0 && (
                <div className="bg-slate-50 p-3 rounded-2xl border border-slate-200/80">
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-bold text-slate-700 flex items-center gap-1">
                      <Calendar size={13} className="text-teal-600" /> Lịch Khởi Hành Hiện Có ({showEditModal.schedules.length})
                    </span>
                    <button
                      type="button"
                      onClick={() => {
                        const id = showEditModal.tourId;
                        setShowEditModal(null);
                        setShowScheduleModal(id);
                      }}
                      className="text-teal-600 hover:text-teal-700 font-bold text-[11px]"
                    >
                      + Thêm Lịch Mới
                    </button>
                  </div>
                  <div className="space-y-1.5 max-h-32 overflow-y-auto pr-1">
                    {showEditModal.schedules.map((sch) => (
                      <div
                        key={sch.id}
                        className="flex items-center justify-between bg-white px-3 py-1.5 rounded-lg border border-slate-100 text-[11px]"
                      >
                        <span className="text-slate-600">
                          {formatDateTime(sch.departureTime)}
                        </span>
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-rose-600 font-mono">
                            {formatCurrency(sch.priceAdult)}
                          </span>
                          <span className="bg-teal-50 text-teal-700 px-1.5 py-0.5 rounded text-[10px] font-bold">
                            {sch.availableSeats}/{sch.totalSeats} chỗ
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="pt-4 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowEditModal(null)}
                  className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="flex-1 py-2.5 bg-gradient-to-r from-amber-600 to-amber-500 hover:from-amber-500 hover:to-amber-400 text-white font-bold rounded-xl shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {isSaving && <Loader2 size={14} className="animate-spin" />}
                  {isSaving ? "Đang lưu..." : "Lưu Thay Đổi"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal 3: Add Departure Schedule */}
      {showScheduleModal && (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in fade-in">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <h3 className="font-extrabold text-slate-900 text-base">Thêm Lịch Khởi Hành & Giá Bán</h3>
                <p className="text-xs text-slate-500">Mở bán chuyến đi với hạn mức số lượng vé.</p>
              </div>
              <button
                onClick={() => setShowScheduleModal(null)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
              >
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleAddSchedule} className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-slate-700 block mb-1">Thời Gian Khởi Hành</label>
                <input
                  type="datetime-local"
                  required
                  value={newSchForm.departureTime}
                  onChange={(e) => setNewSchForm({ ...newSchForm, departureTime: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500"
                />
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Thời Gian Kết Thúc</label>
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
                  <label className="font-bold text-slate-700 block mb-1">Giá Vé Người Lớn (VND)</label>
                  <input
                    type="number"
                    required
                    value={newSchForm.priceAdult}
                    onChange={(e) => setNewSchForm({ ...newSchForm, priceAdult: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono font-bold"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-700 block mb-1">Giá Vé Trẻ Em (VND)</label>
                  <input
                    type="number"
                    required
                    value={newSchForm.priceChild}
                    onChange={(e) => setNewSchForm({ ...newSchForm, priceChild: e.target.value })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Hạn Mức Số Chỗ Khả Dụng (Total Seats)</label>
                <input
                  type="number"
                  required
                  value={newSchForm.totalSeats}
                  onChange={(e) => setNewSchForm({ ...newSchForm, totalSeats: e.target.value })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div className="pt-4 border-t border-slate-100 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowScheduleModal(null)}
                  className="flex-1 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-xl transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="flex-1 py-2.5 bg-teal-600 hover:bg-teal-700 text-white font-bold rounded-xl shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {isSaving && <Loader2 size={14} className="animate-spin" />}
                  {isSaving ? "Đang lưu..." : "Lưu Lịch Đi"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
