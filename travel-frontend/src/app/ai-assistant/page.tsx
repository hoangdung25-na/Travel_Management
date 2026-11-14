"use client";

import { useState, useRef, useEffect } from "react";
import {
  Sparkles,
  Send,
  Bot,
  User,
  RotateCcw,
  RefreshCw,
  Compass,
  FileQuestion,
  CreditCard,
  Mountain,
  Waves,
  ShieldAlert,
} from "lucide-react";
import { aiService } from "@/lib/services/ai-service";
import { ChatMessage } from "@/lib/types";
import { useToast } from "@/lib/toast-context";
import { MarkdownMessage } from "@/components/ai/MarkdownMessage";

// Danh sách chip gợi ý câu hỏi thông minh
const QUICK_PROMPTS = [
  {
    icon: Waves,
    label: "Tour biển Đà Nẵng 3N2Đ",
    query: "Tìm cho tôi tour khám phá Đà Nẵng - Hội An 3 ngày 2 đêm với ngân sách khoảng 5 triệu",
  },
  {
    icon: Mountain,
    label: "Tour Sa Pa săn mây Fansipan",
    query: "Gợi ý tour Sa Pa 3 ngày 2 đêm cho gia đình có trẻ em, muốn đi cáp treo Fansipan",
  },
  {
    icon: FileQuestion,
    label: "Chính sách hủy tour & hoàn tiền",
    query: "Quy định hủy tour trước ngày khởi hành và thời gian nhận lại tiền cọc thế nào?",
  },
  {
    icon: CreditCard,
    label: "Phương thức cọc & thanh toán",
    query: "Tôi có thể thanh toán tiền tour qua những hình thức nào? Có cần cọc trước không?",
  },
  {
    icon: ShieldAlert,
    label: "Giấy tờ tùy thân cần chuẩn bị",
    query: "Khi đi tour trong nước và quốc tế thì người lớn và trẻ em cần mang giấy tờ gì?",
  },
];

function formatTime(date: Date = new Date()): string {
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${hours}:${minutes}`;
}

export default function AiAssistantPage() {
  const { notify } = useToast();

  // Multi-turn Conversation State
  const [conversationId, setConversationId] = useState<string>("");
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);
  const [syncing, setSyncing] = useState(false);

  // Danh sách tin nhắn trong khung chat
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "msg-welcome",
      sender: "ai",
      text: "Xin chào quý khách! 👋 Em là **Trợ lý AI Du lịch 24/7** của Travel Platform.\n\nEm có thể hỗ trợ quý khách:\n• **Tư vấn lịch trình & gợi ý tour du lịch** theo điểm đến và ngân sách cá nhân\n• **Cung cấp đường link xem chi tiết & đặt tour ngay**\n• **Giải đáp chính sách hoàn hủy vé, đặt cọc 30-50% & các cổng thanh toán**\n• **Quy định giấy tờ tùy thân, thủ tục CCCD/Visa và số Hotline hỗ trợ 24/7**.\n\nQuý khách đang quan tâm đến chuyến đi nào hoặc cần hỗ trợ thông tin gì cứ nhắn em nhé!",
      timestamp: "",
    },
  ]);

  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Khởi tạo timestamp cho tin nhắn chào mừng ban đầu trên client để tránh lệch SSR Hydration
  useEffect(() => {
    setMessages((prev) =>
      prev.map((m) =>
        m.id === "msg-welcome" && !m.timestamp ? { ...m, timestamp: formatTime() } : m
      )
    );
  }, []);

  // Tự động cuộn xuống tin nhắn mới nhất
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, chatLoading]);

  // Xử lý gửi tin nhắn (Multi-turn Context RAG)
  async function handleSendMessage(textToSend?: string) {
    const text = (textToSend ?? chatInput).trim();
    if (!text || chatLoading) return;

    const userMsg: ChatMessage = {
      id: `chat-user-${Date.now()}`,
      sender: "user",
      text: text,
      timestamp: formatTime(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setChatInput("");
    setChatLoading(true);

    try {
      // Gửi câu hỏi kèm conversationId để AI nhớ ngữ cảnh đa lượt
      const response = await aiService.chat(text, conversationId || undefined);

      if (response && response.conversationId) {
        setConversationId(response.conversationId);
      }

      const replyText = typeof response === "string" ? response : response.reply;

      const aiMsg: ChatMessage = {
        id: `chat-ai-${Date.now()}`,
        sender: "ai",
        text: replyText,
        timestamp: formatTime(),
      };

      setMessages((prev) => [...prev, aiMsg]);
    } catch (err: any) {
      notify("error", err?.message || "Lỗi kết nối với Trợ lý AI, vui lòng thử lại.");
      const errorMsg: ChatMessage = {
        id: `chat-err-${Date.now()}`,
        sender: "ai",
        text: "Dạ xin lỗi quý khách, hiện tại kết nối đến hệ thống AI đang bị gián đoạn đôi chút. Quý khách vui lòng thử lại sau vài giây hoặc liên hệ Hotline **1900 6868** để được hỗ trợ tức thì nhé!",
        timestamp: formatTime(),
      };
      setMessages((prev) => [...prev, errorMsg]);
    } finally {
      setChatLoading(false);
    }
  }

  // Bắt đầu phiên trò chuyện mới (Reset Session)
  async function handleResetConversation() {
    if (conversationId) {
      try {
        await aiService.clearConversation(conversationId);
      } catch (ignored) {}
    }

    setConversationId("");
    setMessages([
      {
        id: `msg-welcome-${Date.now()}`,
        sender: "ai",
        text: "Đã bắt đầu phiên trò chuyện mới! 🚀 Quý khách cần em tư vấn tour du lịch hay giải đáp chính sách nào ạ?",
        timestamp: formatTime(),
      },
    ]);
    notify("success", "Đã làm mới cuộc hội thoại!");
  }

  // Đồng bộ Tour thật từ tour-service sang AI Vector DB
  async function handleSyncTours() {
    setSyncing(true);
    try {
      const res = await aiService.syncFromTourService();
      notify("success", res.message || "Đã đồng bộ thành công toàn bộ Tour thật từ hệ thống sang AI Vector DB!");
    } catch (err: any) {
      notify("error", err?.message || "Lỗi kết nối khi đồng bộ Tour từ hệ thống.");
    } finally {
      setSyncing(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6 py-6 sm:py-8 space-y-6">
      {/* Top Banner Header */}
      <div className="rounded-3xl bg-[#0D2B2B] text-white p-6 sm:p-8 relative overflow-hidden shadow-xl border border-[#123A3A]">
        <div className="absolute right-0 top-0 w-80 h-80 bg-[#E2603F]/15 rounded-full blur-3xl pointer-events-none" />
        <div className="relative flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4A24C]/20 border border-[#D4A24C]/30 text-[#D4A24C] text-xs font-semibold">
              <Sparkles size={14} className="text-[#D4A24C] animate-pulse" />
              <span>Trợ Lý RAG AI Đa Năng 24/7</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Tư Vấn Lịch Trình & Chăm Sóc Khách Hàng Thông Minh
            </h1>
            <p className="text-stone-300 text-xs sm:text-sm">
              Hệ thống kết nối trực tiếp với CSDL Tour & Chính sách thực tế. Tự động gợi ý tour phù hợp ngân sách, đính kèm link đặt tour và ghi nhớ ngữ cảnh hội thoại.
            </p>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-2.5 shrink-0">
            <button
              onClick={handleResetConversation}
              className="px-3.5 py-2 rounded-xl text-xs font-bold flex items-center gap-1.5 bg-[#123A3A] hover:bg-[#1b4b4b] text-stone-200 hover:text-white transition-all border border-stone-700/50 shadow-sm"
              title="Xóa ngữ cảnh và bắt đầu phiên chat mới"
            >
              <RotateCcw size={14} />
              <span>Làm mới chat</span>
            </button>

            <button
              onClick={handleSyncTours}
              disabled={syncing}
              className="px-3.5 py-2 rounded-xl text-xs font-bold flex items-center gap-1.5 bg-[#D4A24C]/20 text-[#D4A24C] border border-[#D4A24C]/40 hover:bg-[#D4A24C]/30 transition-all disabled:opacity-50 shadow-sm"
              title="Đồng bộ dữ liệu tour thật từ Tour Service vào AI Vector DB"
            >
              <RefreshCw size={14} className={syncing ? "animate-spin" : ""} />
              <span>{syncing ? "Đang nạp..." : "Đồng bộ Tour"}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Unified Chat Container */}
      <div className="bg-white rounded-3xl border border-[#e7e0d3] shadow-md overflow-hidden flex flex-col h-[650px] sm:h-[700px]">
        {/* Chat Messages Area */}
        <div className="flex-1 p-4 sm:p-6 overflow-y-auto space-y-5 bg-[#FBF8F2]/60">
          {messages.map((m) => (
            <div
              key={m.id}
              className={`flex items-start gap-3 ${
                m.sender === "user" ? "flex-row-reverse" : "flex-row"
              }`}
            >
              {/* Avatar */}
              <div
                className={`w-9 h-9 rounded-2xl flex items-center justify-center shrink-0 shadow-sm ${
                  m.sender === "user"
                    ? "bg-[#E2603F] text-white"
                    : "bg-[#0D2B2B] text-[#D4A24C] border border-[#1b4b4b]"
                }`}
              >
                {m.sender === "user" ? (
                  <User size={18} />
                ) : (
                  <Bot size={18} className="animate-pulse" />
                )}
              </div>

              {/* Message Bubble */}
              <div
                className={`max-w-[85%] sm:max-w-2xl p-4 sm:p-5 rounded-3xl text-sm leading-relaxed ${
                  m.sender === "user"
                    ? "bg-[#E2603F] text-white rounded-tr-none shadow-md font-medium"
                    : "bg-white text-stone-800 border border-[#e7e0d3] rounded-tl-none shadow-sm"
                }`}
              >
                {m.sender === "user" ? (
                  <p className="whitespace-pre-line">{m.text}</p>
                ) : (
                  <MarkdownMessage content={m.text} />
                )}

                {m.timestamp && (
                  <span
                    suppressHydrationWarning
                    className={`text-[10px] block mt-2 ${
                      m.sender === "user"
                        ? "text-amber-100/80 text-right"
                        : "text-stone-400"
                    }`}
                  >
                    {m.timestamp}
                  </span>
                )}
              </div>
            </div>
          ))}

          {/* Typing Loading Indicator */}
          {chatLoading && (
            <div className="flex items-start gap-3">
              <div className="w-9 h-9 rounded-2xl flex items-center justify-center shrink-0 bg-[#0D2B2B] text-[#D4A24C] border border-[#1b4b4b] shadow-sm">
                <Bot size={18} className="animate-spin text-[#E2603F]" />
              </div>
              <div className="bg-white p-4 rounded-3xl rounded-tl-none border border-[#e7e0d3] shadow-sm flex items-center gap-2">
                <span className="w-2 h-2 rounded-full bg-[#E2603F] animate-bounce" />
                <span
                  className="w-2 h-2 rounded-full bg-[#E2603F] animate-bounce"
                  style={{ animationDelay: "0.2s" }}
                />
                <span
                  className="w-2 h-2 rounded-full bg-[#E2603F] animate-bounce"
                  style={{ animationDelay: "0.4s" }}
                />
                <span className="text-xs text-stone-500 font-medium ml-2">
                  AI đang tra cứu dữ liệu CSDL & soạn câu trả lời...
                </span>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Quick Suggestion Chips */}
        <div className="px-4 py-2.5 bg-white border-t border-[#e7e0d3]/70 overflow-x-auto flex items-center gap-2 scrollbar-none">
          <div className="text-[11px] font-bold text-stone-500 flex items-center gap-1 shrink-0 pl-1">
            <Compass size={13} className="text-[#E2603F]" />
            <span>Gợi ý hỏi nhanh:</span>
          </div>
          {QUICK_PROMPTS.map((chip, idx) => {
            const Icon = chip.icon;
            return (
              <button
                key={idx}
                type="button"
                onClick={() => handleSendMessage(chip.query)}
                disabled={chatLoading}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium bg-[#FBF8F2] hover:bg-[#F2ECE1] text-stone-700 border border-[#e7e0d3] transition-all hover:scale-[1.02] shrink-0 disabled:opacity-50"
              >
                <Icon size={12} className="text-[#E2603F]" />
                <span>{chip.label}</span>
              </button>
            );
          })}
        </div>

        {/* Input Bar Form */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSendMessage();
          }}
          className="p-3 sm:p-4 bg-white border-t border-[#e7e0d3] flex items-center gap-3"
        >
          <input
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
            placeholder="Hỏi AI bất kỳ điều gì (Ví dụ: Gợi ý tour Sa Pa 3N2Đ, quy định hoàn tiền khi hủy vé)..."
            className="flex-1 bg-[#FBF8F2] border border-[#e7e0d3] rounded-2xl px-4 py-3 sm:py-3.5 text-sm focus:outline-none focus:border-[#E2603F] focus:ring-2 focus:ring-[#E2603F]/20 transition-all text-stone-800 placeholder:text-stone-400"
          />

          <button
            type="submit"
            disabled={chatLoading || !chatInput.trim()}
            className="bg-[#E2603F] hover:bg-[#d55333] active:scale-95 text-white font-bold p-3 sm:px-6 sm:py-3.5 rounded-2xl shadow-md shadow-[#E2603F]/20 transition-all flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed shrink-0"
          >
            <Send size={16} />
            <span className="hidden sm:inline text-xs uppercase tracking-wider">
              Gửi
            </span>
          </button>
        </form>
      </div>
    </div>
  );
}
