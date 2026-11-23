"use client";

import React from "react";
import Link from "next/link";
import { Compass, ExternalLink, Phone } from "lucide-react";

interface MarkdownMessageProps {
  content: string;
}

/**
 * Component phân tích và render Markdown an toàn, chuyển đổi link tour [Tên Tour](/tours/{id})
 * thành thẻ điều hướng Next.js Link tương tác trực tiếp.
 */
export function MarkdownMessage({ content }: MarkdownMessageProps) {
  if (!content) return null;

  // Chuẩn hóa trường hợp AI chèn khoảng trắng/xuống dòng giữa ] và (
  const normalizedContent = content.replace(/\]\s+\(((?:https?:\/\/[^\s)]+)?\/tours\/[^\s)]+)\)/g, "]($1)");

  // Tách nội dung thành các dòng
  const lines = normalizedContent.split("\n");

  return (
    <div className="space-y-2 text-stone-800 text-sm leading-relaxed">
      {lines.map((line, lineIndex) => {
        const trimmed = line.trim();

        // 1. Dòng trống
        if (!trimmed) {
          return <div key={lineIndex} className="h-1.5" />;
        }

        // 1b. Đường kẻ phân cách ngang (--- hoặc ***)
        if (/^(-{3,}|\*{3,})$/.test(trimmed)) {
          return <hr key={lineIndex} className="border-t border-[#e7e0d3] my-2" />;
        }

        // 2. Tiêu đề (### hoặc ## hoặc in đậm đầu dòng)
        if (trimmed.startsWith("### ") || trimmed.startsWith("## ")) {
          const headerText = trimmed.replace(/^#{2,3}\s+/, "");
          return (
            <h4
              key={lineIndex}
              className="text-sm sm:text-base font-extrabold text-[#0D2B2B] pt-2 pb-1 border-b border-[#e7e0d3]/60 flex items-center gap-1.5"
            >
              <Compass size={16} className="text-[#E2603F]" />
              {renderFormattedInline(headerText)}
            </h4>
          );
        }

        // 3. Dòng danh sách gạch đầu dòng (•, -, *)
        if (
          trimmed.startsWith("•") ||
          trimmed.startsWith("- ") ||
          trimmed.startsWith("* ")
        ) {
          const itemText = trimmed.replace(/^[•\-*]\s*/, "");
          return (
            <div key={lineIndex} className="flex items-start gap-2 pl-1.5">
              <span className="w-1.5 h-1.5 rounded-full bg-[#E2603F] mt-2 shrink-0" />
              <div className="flex-1">{renderFormattedInline(itemText)}</div>
            </div>
          );
        }

        // 4. Dòng danh sách số thứ tự (1., 2., ...)
        const numberedMatch = trimmed.match(/^(\d+)\.\s+(.*)$/);
        if (numberedMatch) {
          const number = numberedMatch[1];
          const text = numberedMatch[2];
          return (
            <div key={lineIndex} className="flex items-start gap-2 pl-1.5 mt-1">
              <span className="w-5 h-5 rounded-full bg-[#D4A24C]/20 text-[#0D2B2B] text-[11px] font-bold flex items-center justify-center shrink-0 mt-0.5 border border-[#D4A24C]/40">
                {number}
              </span>
              <div className="flex-1">{renderFormattedInline(text)}</div>
            </div>
          );
        }

        // 5. Dòng bình thường
        return (
          <p key={lineIndex} className="leading-relaxed">
            {renderFormattedInline(line)}
          </p>
        );
      })}
    </div>
  );
}

function normalizeTourHref(rawUrl: string): string {
  const trimmed = rawUrl.trim();
  const tourPathMatch = trimmed.match(/\/tours\/[a-zA-Z0-9-]+/);
  if (tourPathMatch && (trimmed.startsWith("/tours/") || trimmed.includes("localhost") || trimmed.startsWith("http"))) {
    return tourPathMatch[0];
  }
  return trimmed;
}

function renderTourLink(linkText: string, linkUrl: string, key: React.Key): React.ReactNode {
  const cleanLabel = linkText.replace(/^\*\*|\*\*$/g, "").trim();
  const href = normalizeTourHref(linkUrl);
  const isTourLink = href.startsWith("/tours/");

  return (
    <Link
      key={key}
      href={href}
      className={
        isTourLink
          ? "inline font-bold text-blue-600 hover:text-blue-800 underline decoration-blue-400/80 hover:decoration-blue-700 underline-offset-4 transition-colors"
          : "inline font-semibold text-blue-600 hover:text-blue-800 underline underline-offset-4 transition-colors"
      }
      title={`Xem chi tiết ${cleanLabel}`}
    >
      <span>{cleanLabel}</span>
      <ExternalLink size={13} className="inline-block ml-1 mb-0.5 opacity-80" />
    </Link>
  );
}

/**
 * Phân tích và render các thành phần nội dòng:
 * - Link Markdown [Text](url) (kể cả khi bị bọc trong **...**)
 * - Đường dẫn trần /tours/{id}
 * - Chữ in đậm **text** (hỗ trợ đệ quy link bên trong)
 * - Mã inline `code`
 * - Hotline 1900 6868
 */
function renderFormattedInline(text: string): React.ReactNode {
  const tokenRegex =
    /(\*\*\[[^\]]+\]\([^)]+\)\*\*|\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|`[^`]+`|\/tours\/[0-9a-fA-F-]{8,}|1900\s*6868)/g;
  const parts = text.split(tokenRegex);

  return parts.map((part, index) => {
    if (!part) return null;

    // A1. Link Markdown bị bọc trong in đậm **[Tên link](Đường dẫn)**
    const boldLinkMatch = part.match(/^\*\*\[([^\]]+)\]\(([^)]+)\)\*\*$/);
    if (boldLinkMatch) {
      return renderTourLink(boldLinkMatch[1], boldLinkMatch[2], index);
    }

    // A2. Link Markdown chuẩn [Tên link](Đường dẫn)
    const linkMatch = part.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
    if (linkMatch) {
      return renderTourLink(linkMatch[1], linkMatch[2], index);
    }

    // A3. Đường dẫn trần /tours/{uuid} nếu AI không bọc ngoặc Markdown
    if (/^\/tours\/[0-9a-fA-F-]{8,}$/.test(part)) {
      return renderTourLink("Xem chi tiết tour", part, index);
    }

    // B. Text in đậm **in đậm** (đệ quy để vẫn parse được link hoặc code bên trong)
    const boldMatch = part.match(/^\*\*([^*]+)\*\*$/);
    if (boldMatch) {
      return (
        <strong key={index} className="font-bold text-[#0D2B2B]">
          {renderFormattedInline(boldMatch[1])}
        </strong>
      );
    }

    // C. Mã inline `TOUR-XXX`
    const codeMatch = part.match(/^`([^`]+)`$/);
    if (codeMatch) {
      return (
        <code
          key={index}
          className="px-1.5 py-0.5 mx-0.5 rounded bg-stone-100 text-[#0D2B2B] font-mono text-xs font-semibold border border-stone-200/80"
        >
          {codeMatch[1]}
        </code>
      );
    }

    // D. Hotline 1900 6868
    if (part.includes("1900") && part.includes("6868")) {
      return (
        <a
          key={index}
          href="tel:19006868"
          className="inline-flex items-center gap-1 font-bold text-[#E2603F] hover:underline mx-1"
          title="Gọi Hotline CSKH 24/7: 1900 6868"
        >
          <Phone size={13} className="inline" />
          <span>{part}</span>
        </a>
      );
    }

    // E. Text thường
    return <span key={index}>{part}</span>;
  });
}
