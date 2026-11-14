"use client";

import { useEffect, useState, useMemo } from "react";
import {
  UserCheck,
  ShieldAlert,
  Users,
  Search,
  CheckCircle2,
  Loader2,
  RefreshCw,
  Phone,
  Mail,
  Shield,
  Compass,
  User,
  AlertCircle,
  X,
  Edit,
  Trash2,
  Ban,
  UserCog,
} from "lucide-react";
import { adminService } from "@/lib/services/admin-service";
import { UserVm, UserRole, UserStatus } from "@/lib/types";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function AdminUsersPage() {
  const { notify } = useToast();
  const [users, setUsers] = useState<UserVm[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKw, setSearchKw] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("ALL");
  const [approvingId, setApprovingId] = useState<string | null>(null);

  // Edit User Modal
  const [showEditModal, setShowEditModal] = useState<UserVm | null>(null);
  const [editForm, setEditForm] = useState({
    fullName: "",
    phoneNumber: "",
    status: "ACTIVE" as UserStatus,
    roles: ["ROLE_TOURIST"] as UserRole[],
  });
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    setLoading(true);
    try {
      const data = await adminService.getUsers();
      setUsers(data || []);
    } catch (err: any) {
      notify("error", err.message || "Không thể tải danh sách người dùng từ hệ thống");
    } finally {
      setLoading(false);
    }
  }

  function openEditModal(user: UserVm) {
    setShowEditModal(user);
    setEditForm({
      fullName: user.fullName || "",
      phoneNumber: user.phoneNumber || "",
      status: user.status || "ACTIVE",
      roles: user.roles || ["ROLE_TOURIST"],
    });
  }

  async function handleUpdateUser(e: React.FormEvent) {
    e.preventDefault();
    if (!showEditModal) return;
    setIsSaving(true);
    try {
      await adminService.updateUser(showEditModal.userId, {
        fullName: editForm.fullName.trim(),
        phoneNumber: editForm.phoneNumber.trim() || undefined,
        status: editForm.status,
        roles: editForm.roles,
      });
      notify("success", `Cập nhật tài khoản "${editForm.fullName}" thành công!`);
      setShowEditModal(null);
      await loadUsers();
    } catch (err: any) {
      notify("error", err.message || "Cập nhật người dùng thất bại");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteUser(user: UserVm) {
    if (user.email === "admin@example.com") {
      notify("error", "Không được phép xóa tài khoản Quản Trị Viên hệ thống mặc định!");
      return;
    }

    const confirmed = window.confirm(
      `Bạn có chắc chắn muốn xóa tài khoản "${user.fullName}" (${user.email})?\nHành động này sẽ xóa toàn bộ hồ sơ người dùng và không thể hoàn tác.`
    );
    if (!confirmed) return;

    try {
      await adminService.deleteUser(user.userId);
      notify("success", `Đã xóa tài khoản "${user.fullName}" thành công!`);
      await loadUsers();
    } catch (err: any) {
      notify("error", err.message || "Xóa người dùng thất bại");
    }
  }

  async function handleApproveGuide(userId: string, name: string) {
    setApprovingId(userId);
    try {
      await adminService.approveGuide(userId);
      notify("success", `Đã duyệt thành công tài khoản Hướng dẫn viên: ${name}`);
      await loadUsers();
    } catch (err: any) {
      notify("error", err.message || "Duyệt tài khoản thất bại");
    } finally {
      setApprovingId(null);
    }
  }

  function toggleRole(role: UserRole) {
    setEditForm((prev) => {
      const exists = prev.roles.includes(role);
      let updated: UserRole[];
      if (exists) {
        if (prev.roles.length === 1) {
          notify("error", "Người dùng phải có ít nhất 1 vai trò!");
          return prev;
        }
        updated = prev.roles.filter((r) => r !== role);
      } else {
        updated = [...prev.roles, role];
      }
      return { ...prev, roles: updated };
    });
  }

  // Statistics
  const stats = useMemo(() => {
    const total = users.length;
    const guides = users.filter((u) => u.roles?.includes("ROLE_GUIDE")).length;
    const pendingGuides = users.filter(
      (u) => u.roles?.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL"
    ).length;
    const tourists = users.filter((u) => u.roles?.includes("ROLE_TOURIST")).length;
    const admins = users.filter((u) => u.roles?.includes("ROLE_ADMIN")).length;
    return { total, guides, pendingGuides, tourists, admins };
  }, [users]);

  // Filtered users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const name = (u.fullName || "").toLowerCase();
      const email = (u.email || "").toLowerCase();
      const phone = (u.phoneNumber || "").toLowerCase();
      const query = searchKw.trim().toLowerCase();

      const matchesKw = !query || name.includes(query) || email.includes(query) || phone.includes(query);

      if (roleFilter === "GUIDE_PENDING") {
        return matchesKw && u.roles?.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL";
      }
      if (roleFilter === "GUIDE") {
        return matchesKw && u.roles?.includes("ROLE_GUIDE");
      }
      if (roleFilter === "TOURIST") {
        return matchesKw && u.roles?.includes("ROLE_TOURIST");
      }
      if (roleFilter === "ADMIN") {
        return matchesKw && u.roles?.includes("ROLE_ADMIN");
      }
      return matchesKw;
    });
  }, [users, searchKw, roleFilter]);

  function getRoleBadge(roles: string[]) {
    if (roles?.includes("ROLE_ADMIN")) {
      return (
        <span className="inline-flex items-center gap-1 bg-purple-50 text-purple-700 border border-purple-200/80 px-2 py-0.5 rounded-md font-bold text-[10px]">
          <Shield size={10} /> Quản Trị Viên
        </span>
      );
    }
    if (roles?.includes("ROLE_GUIDE")) {
      return (
        <span className="inline-flex items-center gap-1 bg-sky-50 text-sky-700 border border-sky-200/80 px-2 py-0.5 rounded-md font-bold text-[10px]">
          <Compass size={10} /> Hướng Dẫn Viên
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 bg-slate-100 text-slate-700 border border-slate-200/80 px-2 py-0.5 rounded-md font-bold text-[10px]">
        <User size={10} /> Khách Hàng
      </span>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-3xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2 text-xs uppercase tracking-widest text-teal-600 font-bold mb-1">
            <span>Quản Lý Phân Quyền Hạt Nhân</span>
            <span>•</span>
            <span className="text-slate-400">Users & Access Control</span>
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">
            Quản Lý Người Dùng & Duyệt Hướng Dẫn Viên
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Xem thông tin, chỉnh sửa hồ sơ, phân quyền tài khoản, phê duyệt hoặc xóa người dùng trong hệ thống.
          </p>
        </div>

        <button
          onClick={loadUsers}
          disabled={loading}
          className="inline-flex items-center gap-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs px-4 py-2.5 rounded-xl border border-slate-200/80 transition-colors shrink-0"
        >
          <RefreshCw size={14} className={loading ? "animate-spin text-teal-600" : ""} />
          Làm mới
        </button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-slate-500">Tổng Người Dùng</p>
          <p className="text-2xl font-black text-slate-900 mt-1">{stats.total}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-sky-600">Hướng Dẫn Viên</p>
          <p className="text-2xl font-black text-sky-600 mt-1">{stats.guides}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-amber-600">HDV Chờ Duyệt</p>
          <p className="text-2xl font-black text-amber-600 mt-1">{stats.pendingGuides}</p>
        </div>
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <p className="text-xs font-semibold text-teal-600">Khách Hàng</p>
          <p className="text-2xl font-black text-teal-600 mt-1">{stats.tourists}</p>
        </div>
      </div>

      {/* Filter Tabs & Search Toolbar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col md:flex-row gap-3 items-center justify-between">
        {/* Tabs */}
        <div className="flex flex-wrap gap-2 w-full md:w-auto">
          <button
            onClick={() => setRoleFilter("ALL")}
            className={`text-xs px-3.5 py-2 rounded-xl font-bold transition-all ${
              roleFilter === "ALL"
                ? "bg-slate-900 text-white shadow-sm"
                : "bg-slate-50 text-slate-600 hover:bg-slate-100"
            }`}
          >
            Tất Cả ({users.length})
          </button>

          <button
            onClick={() => setRoleFilter("GUIDE_PENDING")}
            className={`text-xs px-3.5 py-2 rounded-xl font-bold transition-all relative ${
              roleFilter === "GUIDE_PENDING"
                ? "bg-amber-600 text-white shadow-sm"
                : "bg-amber-50 text-amber-800 border border-amber-200/60 hover:bg-amber-100"
            }`}
          >
            <span>HDV Chờ Duyệt ({stats.pendingGuides})</span>
            {stats.pendingGuides > 0 && (
              <span className="absolute -top-1 -right-1 w-2.5 h-2.5 rounded-full bg-rose-500 animate-ping" />
            )}
          </button>

          <button
            onClick={() => setRoleFilter("GUIDE")}
            className={`text-xs px-3.5 py-2 rounded-xl font-bold transition-all ${
              roleFilter === "GUIDE"
                ? "bg-sky-600 text-white shadow-sm"
                : "bg-sky-50 text-sky-800 border border-sky-200/60 hover:bg-sky-100"
            }`}
          >
            Hướng Dẫn Viên ({stats.guides})
          </button>

          <button
            onClick={() => setRoleFilter("TOURIST")}
            className={`text-xs px-3.5 py-2 rounded-xl font-bold transition-all ${
              roleFilter === "TOURIST"
                ? "bg-teal-600 text-white shadow-sm"
                : "bg-teal-50 text-teal-800 border border-teal-200/60 hover:bg-teal-100"
            }`}
          >
            Khách Hàng ({stats.tourists})
          </button>

          <button
            onClick={() => setRoleFilter("ADMIN")}
            className={`text-xs px-3.5 py-2 rounded-xl font-bold transition-all ${
              roleFilter === "ADMIN"
                ? "bg-purple-600 text-white shadow-sm"
                : "bg-purple-50 text-purple-800 border border-purple-200/60 hover:bg-purple-100"
            }`}
          >
            Admin ({stats.admins})
          </button>
        </div>

        {/* Search */}
        <div className="relative w-full md:w-64">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            value={searchKw}
            onChange={(e) => setSearchKw(e.target.value)}
            placeholder="Tìm tên, email, sđt..."
            className="w-full pl-9 pr-7 py-2 text-xs bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 focus:bg-white transition-colors"
          />
          {searchKw && (
            <button
              onClick={() => setSearchKw("")}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
            >
              <X size={13} />
            </button>
          )}
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-slate-50/80 text-slate-500 font-bold border-b border-slate-200/80 uppercase tracking-wider text-[11px]">
                <th className="py-4 px-4 whitespace-nowrap">Họ Và Tên</th>
                <th className="py-4 px-4 whitespace-nowrap">Email Liên Hệ</th>
                <th className="py-4 px-4 whitespace-nowrap">Số Điện Thoại</th>
                <th className="py-4 px-4 whitespace-nowrap">Vai Trò (Role)</th>
                <th className="py-4 px-4 whitespace-nowrap text-center">Trạng Thái</th>
                <th className="py-4 px-4 whitespace-nowrap text-right pr-6">Thao Tác Admin</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-16 text-center text-slate-400">
                    <Loader2 size={24} className="animate-spin mx-auto mb-2 text-teal-600" />
                    Đang tải danh sách người dùng...
                  </td>
                </tr>
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-16 text-center text-slate-400">
                    <AlertCircle size={24} className="mx-auto mb-2 text-slate-300" />
                    Không tìm thấy người dùng phù hợp.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((u) => (
                  <tr key={u.userId} className="hover:bg-slate-50/70 transition-colors">
                    {/* 1. Name */}
                    <td className="py-3.5 px-4 align-middle">
                      <div className="flex items-center gap-2.5">
                        <div className="w-8 h-8 rounded-full bg-teal-50 border border-teal-200/60 flex items-center justify-center text-teal-700 font-bold text-xs uppercase shrink-0">
                          {u.fullName ? u.fullName.charAt(0) : "U"}
                        </div>
                        <div>
                          <div className="font-bold text-slate-900">
                            {u.fullName || "Chưa cập nhật tên"}
                          </div>
                          <div className="font-mono text-[10px] text-slate-400">
                            ID: {u.userId.slice(0, 8)}...
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* 2. Email */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap font-mono text-slate-700 text-[11px]">
                      <span className="inline-flex items-center gap-1.5">
                        <Mail size={12} className="text-slate-400" />
                        {u.email}
                      </span>
                    </td>

                    {/* 3. Phone */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-slate-600">
                      {u.phoneNumber ? (
                        <span className="inline-flex items-center gap-1 font-mono text-xs">
                          <Phone size={12} className="text-teal-600" />
                          {u.phoneNumber}
                        </span>
                      ) : (
                        <span className="text-slate-300">—</span>
                      )}
                    </td>

                    {/* 4. Roles */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap">
                      {getRoleBadge(u.roles as any)}
                    </td>

                    {/* 5. Status */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-center">
                      <StatusBadge status={u.status} />
                    </td>

                    {/* 6. Actions */}
                    <td className="py-3.5 px-4 align-middle whitespace-nowrap text-right pr-6">
                      <div className="inline-flex items-center gap-1.5">
                        {u.roles?.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL" && (
                          <button
                            onClick={() => handleApproveGuide(u.userId, u.fullName)}
                            disabled={approvingId === u.userId}
                            title="Duyệt tài khoản Hướng dẫn viên"
                            className="bg-emerald-50 text-emerald-700 hover:bg-emerald-100 font-bold px-2.5 py-1.5 rounded-lg border border-emerald-200 transition-colors inline-flex items-center gap-1 text-[11px]"
                          >
                            {approvingId === u.userId ? (
                              <Loader2 size={12} className="animate-spin" />
                            ) : (
                              <CheckCircle2 size={12} />
                            )}
                            Duyệt HDV
                          </button>
                        )}

                        <button
                          onClick={() => openEditModal(u)}
                          title="Chỉnh sửa thông tin, vai trò và trạng thái tài khoản"
                          className="bg-amber-50 text-amber-700 hover:bg-amber-100 active:bg-amber-200 font-bold px-2.5 py-1.5 rounded-lg border border-amber-200/80 transition-colors inline-flex items-center gap-1 text-[11px]"
                        >
                          <Edit size={12} /> Sửa
                        </button>

                        {u.email !== "admin@example.com" ? (
                          <button
                            onClick={() => handleDeleteUser(u)}
                            title="Xóa tài khoản người dùng"
                            className="bg-rose-50 text-rose-700 hover:bg-rose-100 active:bg-rose-200 font-bold px-2 py-1.5 rounded-lg border border-rose-200/80 transition-colors inline-flex items-center gap-1 text-[11px]"
                          >
                            <Trash2 size={12} /> Xóa
                          </button>
                        ) : (
                          <span
                            title="Tài khoản Quản trị viên mặc định được bảo vệ"
                            className="text-slate-300 px-2 py-1.5 inline-flex items-center text-[11px]"
                          >
                            <Shield size={12} className="text-slate-300" />
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Edit User */}
      {showEditModal && (
        <div className="fixed inset-0 bg-slate-900/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 space-y-4 shadow-2xl animate-in fade-in max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="font-extrabold text-slate-900 text-base">Chỉnh Sửa Người Dùng</h3>
                  <span className="font-mono text-xs text-teal-700 bg-teal-50 px-2 py-0.5 rounded font-bold border border-teal-200">
                    ID: {showEditModal.userId.slice(0, 8)}...
                  </span>
                </div>
                <p className="text-xs text-slate-500 mt-0.5">
                  Cập nhật thông tin định danh, số điện thoại, vai trò và trạng thái tài khoản.
                </p>
              </div>
              <button
                onClick={() => setShowEditModal(null)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-500 transition-colors"
              >
                <X size={16} />
              </button>
            </div>

            <form onSubmit={handleUpdateUser} className="space-y-4 text-xs">
              <div>
                <label className="font-bold text-slate-700 block mb-1">Email Tài Khoản (Không thể đổi)</label>
                <div className="flex items-center gap-2 p-2.5 bg-slate-100/80 border border-slate-200 rounded-xl text-slate-600 font-mono">
                  <Mail size={14} className="text-slate-400" />
                  <span>{showEditModal.email}</span>
                </div>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Họ Và Tên</label>
                <input
                  required
                  value={editForm.fullName}
                  onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                  placeholder="Nguyễn Văn A..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold"
                />
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Số Điện Thoại</label>
                <input
                  value={editForm.phoneNumber}
                  onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })}
                  placeholder="0912345678..."
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-mono"
                />
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1">Trạng Thái Tài Khoản</label>
                <select
                  value={editForm.status}
                  onChange={(e) => setEditForm({ ...editForm, status: e.target.value as UserStatus })}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 font-bold cursor-pointer"
                >
                  <option value="ACTIVE">ACTIVE - Hoạt động bình thường</option>
                  <option value="PENDING_APPROVAL">PENDING_APPROVAL - Đang chờ phê duyệt</option>
                  <option value="BLOCKED">BLOCKED - Bị khóa quyền truy cập</option>
                </select>
              </div>

              <div>
                <label className="font-bold text-slate-700 block mb-1.5">Phân Quyền Vai Trò (Roles)</label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  <button
                    type="button"
                    onClick={() => toggleRole("ROLE_TOURIST")}
                    className={`p-2.5 rounded-xl border text-left font-bold text-xs transition-all flex items-center justify-between ${
                      editForm.roles.includes("ROLE_TOURIST")
                        ? "border-teal-500 bg-teal-50 text-teal-800"
                        : "border-slate-200 bg-slate-50 text-slate-600"
                    }`}
                  >
                    <span>Khách Hàng</span>
                    {editForm.roles.includes("ROLE_TOURIST") && <CheckCircle2 size={14} className="text-teal-600" />}
                  </button>

                  <button
                    type="button"
                    onClick={() => toggleRole("ROLE_GUIDE")}
                    className={`p-2.5 rounded-xl border text-left font-bold text-xs transition-all flex items-center justify-between ${
                      editForm.roles.includes("ROLE_GUIDE")
                        ? "border-sky-500 bg-sky-50 text-sky-800"
                        : "border-slate-200 bg-slate-50 text-slate-600"
                    }`}
                  >
                    <span>Hướng Dẫn Viên</span>
                    {editForm.roles.includes("ROLE_GUIDE") && <CheckCircle2 size={14} className="text-sky-600" />}
                  </button>

                  <button
                    type="button"
                    onClick={() => toggleRole("ROLE_ADMIN")}
                    className={`p-2.5 rounded-xl border text-left font-bold text-xs transition-all flex items-center justify-between ${
                      editForm.roles.includes("ROLE_ADMIN")
                        ? "border-purple-500 bg-purple-50 text-purple-800"
                        : "border-slate-200 bg-slate-50 text-slate-600"
                    }`}
                  >
                    <span>Quản Trị</span>
                    {editForm.roles.includes("ROLE_ADMIN") && <CheckCircle2 size={14} className="text-purple-600" />}
                  </button>
                </div>
              </div>

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
    </div>
  );
}
