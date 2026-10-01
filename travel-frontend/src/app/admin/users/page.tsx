"use client";

import { useEffect, useState } from "react";
import { UserCheck, ShieldAlert, Users, Search, CheckCircle2 } from "lucide-react";
import { adminService } from "@/lib/services/admin-service";
import { UserVm } from "@/lib/types";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { useToast } from "@/lib/toast-context";

export default function AdminUsersPage() {
  const { notify } = useToast();
  const [users, setUsers] = useState<UserVm[]>([]);
  const [searchKw, setSearchKw] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("ALL");

  useEffect(() => {
    loadUsers();
  }, []);

  function loadUsers() {
    adminService.getUsers().then(setUsers);
  }

  async function handleApproveGuide(userId: string, name: string) {
    try {
      await adminService.approveGuide(userId);
      notify("success", `Đã duyệt thành công tài khoản Hướng dẫn viên: ${name}`);
      loadUsers();
    } catch (err: any) {
      notify("error", err.message || "Duyệt tài khoản thất bại");
    }
  }

  const filteredUsers = users.filter((u) => {
    const matchesKw =
      u.fullName.toLowerCase().includes(searchKw.toLowerCase()) ||
      u.email.toLowerCase().includes(searchKw.toLowerCase());

    if (roleFilter === "GUIDE_PENDING") {
      return matchesKw && u.roles.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL";
    }
    if (roleFilter === "GUIDE") {
      return matchesKw && u.roles.includes("ROLE_GUIDE");
    }
    if (roleFilter === "TOURIST") {
      return matchesKw && u.roles.includes("ROLE_TOURIST");
    }
    return matchesKw;
  });

  const pendingGuidesCount = users.filter(
    (u) => u.roles.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL"
  ).length;

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-xs uppercase tracking-widest text-teal-600 font-bold block mb-1">
            Quản Lý Phân Quyền Hạt Nhân
          </span>
          <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">
            Quản Lý Người Dùng & Duyệt Hướng Dẫn Viên
          </h1>
        </div>

        <div className="relative">
          <Search size={16} className="absolute left-3 top-3 text-slate-400" />
          <input
            value={searchKw}
            onChange={(e) => setSearchKw(e.target.value)}
            placeholder="Tìm tên, email..."
            className="pl-9 pr-3 py-2 text-xs bg-white border border-slate-200 rounded-xl focus:outline-none focus:border-teal-500 w-64 shadow-sm"
          />
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap gap-2">
        <button
          onClick={() => setRoleFilter("ALL")}
          className={`text-xs px-4 py-2 rounded-xl font-bold transition-all ${
            roleFilter === "ALL" ? "bg-slate-900 text-white" : "bg-white text-slate-600 border border-slate-200"
          }`}
        >
          Tất Cả Người Dùng ({users.length})
        </button>

        <button
          onClick={() => setRoleFilter("GUIDE_PENDING")}
          className={`text-xs px-4 py-2 rounded-xl font-bold transition-all relative ${
            roleFilter === "GUIDE_PENDING"
              ? "bg-amber-600 text-white"
              : "bg-amber-50 text-amber-800 border border-amber-200"
          }`}
        >
          <span>HDV Chờ Duyệt ({pendingGuidesCount})</span>
          {pendingGuidesCount > 0 && (
            <span className="absolute -top-1 -right-1 w-3 h-3 rounded-full bg-rose-500 animate-ping" />
          )}
        </button>

        <button
          onClick={() => setRoleFilter("GUIDE")}
          className={`text-xs px-4 py-2 rounded-xl font-bold transition-all ${
            roleFilter === "GUIDE" ? "bg-sky-600 text-white" : "bg-white text-slate-600 border border-slate-200"
          }`}
        >
          Hướng Dẫn Viên
        </button>

        <button
          onClick={() => setRoleFilter("TOURIST")}
          className={`text-xs px-4 py-2 rounded-xl font-bold transition-all ${
            roleFilter === "TOURIST" ? "bg-teal-600 text-white" : "bg-white text-slate-600 border border-slate-200"
          }`}
        >
          Khách Hàng
        </button>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 font-bold border-b border-slate-100 uppercase tracking-wider">
              <tr>
                <th className="py-3.5 px-4">Họ Và Tên</th>
                <th className="py-3.5 px-4">Email</th>
                <th className="py-3.5 px-4">Số Điện Thoại</th>
                <th className="py-3.5 px-4">Vai Trò (Role)</th>
                <th className="py-3.5 px-4">Trạng Thái Account</th>
                <th className="py-3.5 px-4 text-right">Thao Tác Admin</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {filteredUsers.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    Không tìm thấy người dùng phù hợp.
                  </td>
                </tr>
              )}
              {filteredUsers.map((u) => (
                <tr key={u.userId} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-4 px-4 font-bold text-slate-900 flex items-center gap-2">
                    <UserCheck size={16} className="text-teal-600" />
                    <span>{u.fullName}</span>
                  </td>
                  <td className="py-4 px-4 font-mono text-slate-600">{u.email}</td>
                  <td className="py-4 px-4 text-slate-500">{u.phoneNumber || "—"}</td>
                  <td className="py-4 px-4">
                    <span className="bg-slate-100 text-slate-700 px-2 py-1 rounded font-mono font-bold text-[10px]">
                      {u.roles.join(", ")}
                    </span>
                  </td>
                  <td className="py-4 px-4">
                    <StatusBadge status={u.status} />
                  </td>
                  <td className="py-4 px-4 text-right">
                    {u.roles.includes("ROLE_GUIDE") && u.status === "PENDING_APPROVAL" ? (
                      <button
                        onClick={() => handleApproveGuide(u.userId, u.fullName)}
                        className="bg-amber-600 hover:bg-amber-700 text-white font-bold px-3.5 py-1.5 rounded-xl shadow-md transition-colors inline-flex items-center gap-1"
                      >
                        <CheckCircle2 size={14} /> Duyệt HDV
                      </button>
                    ) : (
                      <span className="text-slate-400 text-[11px]">Hoạt động bình thường</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
