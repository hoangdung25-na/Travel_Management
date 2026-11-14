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

  // Tách nội dung thành các dòng
  const lines = content.split("\n");

  return (
    <div className="space-y-2 text-stone-800 text-sm leading-relaxed">
      {lines.map((line, lineIndex) => {
        const trimmed = line.trim();

        // 1. Dòng trống
        if (!trimmed) {
          return <div key={lineIndex} className="h-1.5" />;
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

/**
 * Phân tích và render các thành phần nội dòng (Link Markdown [Text](url), in đậm **text**, hotline)
 */
function renderFormattedInline(text: string): React.ReactNode {
  // Regex tìm kiếm các đoạn [Text](URL) hoặc **Text** hoặc Hotline 1900 6868
  const tokenRegex = /(\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|1900\s*6868)/g;
  const parts = text.split(tokenRegex);

  return parts.map((part, index) => {
    if (!part) return null;

    // A. Xử lý Link Markdown [Tên link](Đường dẫn)
    const linkMatch = part.match(/^\[([^\]]+)\]\(([^)]+)\)$/);
    if (linkMatch) {
      const linkText = linkMatch[1];
      const linkUrl = linkMatch[2];

      const isTourLink = linkUrl.startsWith("/tours/") || linkUrl.includes("/tours/");

      return (
        <Link
          key={index}
          href={linkUrl}
          className={`inline-flex items-center gap-1.5 mx-1 px-2.5 py-1 rounded-lg text-xs font-bold transition-all shadow-sm ${
            isTourLink
              ? "bg-[#E2603F] text-white hover:bg-[#c94f31] hover:shadow-md hover:scale-[1.02]"
              : "bg-[#0D2B2B] text-amber-200 hover:bg-[#184646]"
          }`}
          title={`Xem chi tiết ${linkText}`}
        >
          <Compass size={13} className="shrink-0 animate-spin-slow" />
          <span>{linkText}</span>
          <ExternalLink size={12} className="shrink-0 opacity-80" />
        </Link>
      );
    }

    // B. Xử lý Text in đậm **in đậm**
    const boldMatch = part.match(/^\*\*([^*]+)\*\*$/);
    if (boldMatch) {
      return (
        <strong key={index} className="font-bold text-[#0D2B2B]">
          {boldMatch[1]}
        </strong>
      );
    }

    // C. Xử lý Hotline 1900 6868
    if (part.includes("1900") && part.includes("6868")) {
      return (
        <a
          key={index}
          href="tel:19006868"
          className="inline-flex items-center gap-1 font-bold text-[#E2603F] hover:underline mx-1"
          title="Gọi Hotline CSKH 1900 6868"
        >
          <Phone size={13} className="inline" />
          <span>{part}</span>
        </a>
      );
    }

    // D. Text thường
    return <span key={index}>{part}</span>;
  });
}
