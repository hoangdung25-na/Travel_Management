"use client";

import { useState } from "react";
import Image from "next/image";
import { useAuth } from "@/lib/auth-context";
import { authService } from "@/lib/services/auth-service";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";
import { Button } from "@/components/ui/Button";

export default function ProfilePage() {
  const { user, isLoading } = useAuth();

  if (isLoading || !user) {
    return <div className="mx-auto max-w-lg px-4 py-24 text-center text-ink-soft">Đang tải...</div>;
  }

  // key theo userId để form re-mount (và reset đúng state ban đầu) mỗi khi đổi user,
  // thay vì dùng useEffect để đồng bộ state — tránh setState trong effect.
  return <ProfileForm key={user.userId} user={user} />;
}

function ProfileForm({ user }: { user: NonNullable<ReturnType<typeof useAuth>["user"]> }) {
  const { refreshUser } = useAuth();
  const { notify } = useToast();

  const [form, setForm] = useState({
    fullName: user.fullName ?? "",
    phoneNumber: user.phoneNumber ?? "",
    dateOfBirth: user.dateOfBirth ?? "",
    emergencyContact: user.emergencyContact ?? "",
  });
  const [avatar, setAvatar] = useState<File | null>(null);
  const [saving, setSaving] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await authService.updateProfile(form, avatar ?? undefined);
      await refreshUser();
      notify("success", "Đã cập nhật hồ sơ.");
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể cập nhật hồ sơ.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg px-4 sm:px-6 py-10">
      <p className="text-xs uppercase tracking-[0.2em] text-coral mb-2">Tài khoản</p>
      <h1 className="font-display text-3xl mb-8">Hồ sơ cá nhân</h1>

      <div className="flex items-center gap-4 mb-8">
        <Image
          src={user.avatarUrl || "https://api.dicebear.com/9.x/initials/svg?seed=" + user.fullName}
          alt="Avatar"
          width={64}
          height={64}
          unoptimized
          className="w-16 h-16 rounded-full object-cover border border-line"
        />
        <label className="text-sm text-teal hover:text-coral cursor-pointer">
          Đổi ảnh đại diện
          <input
            type="file"
            accept="image/*"
            className="hidden"
            onChange={(e) => setAvatar(e.target.files?.[0] ?? null)}
          />
        </label>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="text-xs text-ink-soft mb-1 block">Email</label>
          <input
            disabled
            value={user.email}
            className="w-full rounded-lg border border-line px-4 py-2.5 text-sm bg-paper-dim text-ink-soft"
          />
        </div>
        <div>
          <label className="text-xs text-ink-soft mb-1 block">Họ và tên</label>
          <input
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            className="w-full rounded-lg border border-line px-4 py-2.5 text-sm focus:outline-none focus:border-teal"
          />
        </div>
        <div>
          <label className="text-xs text-ink-soft mb-1 block">Số điện thoại</label>
          <input
            value={form.phoneNumber}
            onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
            className="w-full rounded-lg border border-line px-4 py-2.5 text-sm focus:outline-none focus:border-teal"
          />
        </div>
        <div>
          <label className="text-xs text-ink-soft mb-1 block">Ngày sinh</label>
          <input
            type="date"
            value={form.dateOfBirth}
            onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })}
            className="w-full rounded-lg border border-line px-4 py-2.5 text-sm focus:outline-none focus:border-teal"
          />
        </div>
        <div>
          <label className="text-xs text-ink-soft mb-1 block">Liên hệ khẩn cấp</label>
          <input
            value={form.emergencyContact}
            onChange={(e) => setForm({ ...form, emergencyContact: e.target.value })}
            className="w-full rounded-lg border border-line px-4 py-2.5 text-sm focus:outline-none focus:border-teal"
          />
        </div>
        <Button type="submit" loading={saving}>
          Lưu thay đổi
        </Button>
      </form>
    </div>
  );
}
