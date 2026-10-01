"use client";

import { useEffect, useState } from "react";

const HOLD_DURATION_MS = 15 * 60 * 1000; // giữ chỗ 15 phút, tính từ thời điểm booking.createdAt

/** Đếm ngược dựa trên mốc thời gian server (createdAt) để tránh lệch giờ máy khách. */
export function useCountdown(createdAtIso: string | null) {
  const [remainingMs, setRemainingMs] = useState<number | null>(null);

  useEffect(() => {
    if (!createdAtIso) return;
    const deadline = new Date(createdAtIso).getTime() + HOLD_DURATION_MS;

    function tick() {
      setRemainingMs(Math.max(0, deadline - Date.now()));
    }
    tick();
    const interval = setInterval(tick, 1000);
    return () => clearInterval(interval);
  }, [createdAtIso]);

  const expired = remainingMs !== null && remainingMs <= 0;
  const minutes = remainingMs !== null ? Math.floor(remainingMs / 60000) : null;
  const seconds = remainingMs !== null ? Math.floor((remainingMs % 60000) / 1000) : null;

  return { remainingMs, minutes, seconds, expired };
}
