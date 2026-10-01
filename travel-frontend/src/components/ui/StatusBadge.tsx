import clsx from "clsx";

const STATUS_LABELS: Record<string, string> = {
  DRAFT: "Bản nháp",
  PUBLISHED: "Đang mở bán",
  ARCHIVED: "Ngừng bán",
  PENDING: "Chờ xử lý",
  SEATS_RESERVED: "Đã giữ chỗ",
  PAYMENT_PENDING: "Chờ thanh toán",
  CONFIRMED: "Đã xác nhận",
  CANCELLED: "Đã hủy",
  SUCCESS: "Thành công",
  FAILED: "Thất bại",
  REFUNDED: "Đã hoàn tiền",
  ACTIVE: "Hoạt động",
  BLOCKED: "Đã khóa",
  PENDING_APPROVAL: "Chờ duyệt",
};

const STATUS_STYLES: Record<string, string> = {
  DRAFT: "bg-[#f5f0e6] text-stone-600",
  PUBLISHED: "bg-[#FBF8F2] border border-[#E2603F]/30 text-[#E2603F] font-semibold",
  ARCHIVED: "bg-[#f5f0e6] text-stone-600",
  PENDING: "bg-[#D4A24C]/15 text-[#a87823] font-semibold",
  SEATS_RESERVED: "bg-[#D4A24C]/15 text-[#a87823] font-semibold",
  PAYMENT_PENDING: "bg-[#D4A24C]/15 text-[#a87823] font-semibold",
  CONFIRMED: "bg-[#E2603F]/10 text-[#E2603F] font-bold border border-[#E2603F]/20",
  CANCELLED: "bg-stone-200 text-stone-700",
  SUCCESS: "bg-[#E2603F]/10 text-[#E2603F] font-bold border border-[#E2603F]/20",
  FAILED: "bg-stone-200 text-stone-700",
  REFUNDED: "bg-[#f5f0e6] text-stone-600",
  ACTIVE: "bg-[#FBF8F2] border border-[#E2603F]/30 text-[#E2603F] font-semibold",
  BLOCKED: "bg-stone-200 text-stone-700",
  PENDING_APPROVAL: "bg-[#D4A24C]/15 text-[#a87823] font-semibold",
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={clsx(
        "inline-flex items-center rounded-full px-2.5 py-1 text-xs font-medium",
        STATUS_STYLES[status] ?? "bg-[#f5f0e6] text-stone-600"
      )}
    >
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}
