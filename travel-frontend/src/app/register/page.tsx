"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { useToast } from "@/lib/toast-context";
import { ApiError } from "@/lib/api-client";
import { AccountType } from "@/lib/types";
import { Button } from "@/components/ui/Button";

export default function RegisterPage() {
  const { register } = useAuth();
  const { notify } = useToast();
  const router = useRouter();

  const [form, setForm] = useState({
    email: "",
    password: "",
    fullName: "",
    phoneNumber: "",
    accountType: "TOURIST" as AccountType,
  });
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (form.password.length < 8) {
      notify("error", "Mật khẩu phải có ít nhất 8 ký tự.");
      return;
    }
    setLoading(true);
    try {
      await register(form);
      notify(
        "success",
        form.accountType === "GUIDE"
          ? "Đăng ký thành công! Tài khoản Hướng dẫn viên cần được quản trị viên duyệt trước khi đăng nhập."
          : "Đăng ký thành công! Vui lòng đăng nhập."
      );
      router.push("/login");
    } catch (err) {
      if (err instanceof ApiError) notify("error", err.message);
      else notify("error", "Không thể đăng ký, vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-sm px-4 py-20">
      <p className="text-xs uppercase tracking-[0.2em] text-[#E2603F] mb-2 text-center font-bold">Bắt đầu hành trình</p>
      <h1 className="font-display text-3xl text-center mb-8 font-extrabold text-stone-900">Tạo tài khoản</h1>

      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          required
          value={form.fullName}
          onChange={(e) => setForm({ ...form, fullName: e.target.value })}
          placeholder="Họ và tên"
          className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
        />
        <input
          required
          type="email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
          placeholder="Email"
          className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
        />
        <input
          value={form.phoneNumber}
          onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })}
          placeholder="Số điện thoại (10 số)"
          className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
        />
        <input
          required
          type="password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          placeholder="Mật khẩu (tối thiểu 8 ký tự)"
          className="w-full rounded-lg border border-[#e7e0d3] px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-[#E2603F]"
        />

        <div className="flex gap-2">
          {(["TOURIST", "GUIDE"] as AccountType[]).map((type) => (
            <button
              key={type}
              type="button"
              onClick={() => setForm({ ...form, accountType: type })}
              className={`flex-1 rounded-lg border px-3 py-2.5 text-sm font-semibold transition-colors ${
                form.accountType === type
                  ? "border-[#E2603F] bg-[#E2603F]/10 text-[#E2603F]"
                  : "border-[#e7e0d3] text-stone-600 bg-white"
              }`}
            >
              {type === "TOURIST" ? "Du khách" : "Hướng dẫn viên"}
            </button>
          ))}
        </div>

        <Button type="submit" className="w-full font-bold shadow-md" loading={loading}>
          Đăng ký
        </Button>
      </form>

      <p className="text-center text-sm text-stone-600 mt-8">
        Đã có tài khoản?{" "}
        <Link href="/login" className="text-[#E2603F] font-bold hover:underline">
          Đăng nhập
        </Link>
      </p>
    </div>
  );
}
