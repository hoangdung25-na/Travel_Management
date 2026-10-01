"use client";

import { useState } from "react";
import Link from "next/link";
import {
  Sparkles,
  Send,
  MapPin,
  Wallet,
  Bot,
  User,
  MessageSquare,
  Compass,
  ArrowRight,
  RefreshCw,
} from "lucide-react";
import { aiService } from "@/lib/services/ai-service";
import { TripRecommendationVm, ChatMessage } from "@/lib/types";
import { formatCurrency } from "@/lib/format";
import { useToast } from "@/lib/toast-context";

const SUGGESTIONS = [
  "3 ngày 2 đêm ở Đà Nẵng, ngân sách 5 triệu cho 2 người",
  "Tour thiên đường Phú Quốc lặn ngắm san hô 4 ngày 3 đêm",
  "Phượt đèo Mã Pí Lèng Hà Giang 3 ngày 2 đêm cùng bạn bè",
];

export default function AiAssistantPage() {
  const { notify } = useToast();
  const [activeTab, setActiveTab] = useState<"PLANNER" | "CHAT">("PLANNER");

  // RAG Planner State
  const [query, setQuery] = useState("");
  const [maxBudget, setMaxBudget] = useState("");
  const [destination, setDestination] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<TripRecommendationVm | null>(null);

  // Chatbot State
  const [chatInput, setChatInput] = useState("");
  const [chatLoading, setChatLoading] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "msg-1",
      sender: "ai",
      text: "Xin chào! Tôi là AI Travel Assistant. Bạn cần tư vấn về chính sách hủy tour, thủ tục thanh toán, chọn trang phục hay thời tiết các điểm đến?",
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    },
  ]);

  async function handleAskRAG(e?: React.FormEvent, presetQuery?: string) {
    e?.preventDefault();
    const text = presetQuery ?? query;
    if (!text.trim()) return;
    setQuery(text);
    setLoading(true);
    setResult(null);

    try {
      const res = await aiService.getRecommendations({
        promptText: text,
        maxBudget: maxBudget ? Number(maxBudget) : undefined,
        destination: destination || undefined,
        topK: 5,
      });
      setResult(res);
    } catch (err: any) {
      notify("error", err.message || "Trợ lý AI chưa phản hồi được, vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  }

  async function handleSendChat(e: React.FormEvent) {
    e.preventDefault();
    if (!chatInput.trim() || chatLoading) return;

    const userMsg: ChatMessage = {
      id: `chat-${Date.now()}`,
      sender: "user",
      text: chatInput,
      timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    };

    setMessages((prev) => [...prev, userMsg]);
    const currentInput = chatInput;
    setChatInput("");
    setChatLoading(true);

    try {
      const reply = await aiService.chat(currentInput);
      const aiMsg: ChatMessage = {
        id: `chat-ai-${Date.now()}`,
        sender: "ai",
        text: reply,
        timestamp: new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err: any) {
      notify("error", "Lỗi kết nối AI Chatbot");
    } finally {
      setChatLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 sm:px-6 py-10 space-y-8">
      {/* Top Banner Header */}
      <div className="rounded-3xl bg-[#0D2B2B] text-white p-8 sm:p-10 relative overflow-hidden shadow-xl border border-[#123A3A]">
        <div className="absolute right-0 top-0 w-80 h-80 bg-[#E2603F]/10 rounded-full blur-3xl" />
        <div className="relative max-w-2xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#D4A24C]/20 border border-[#D4A24C]/30 text-[#D4A24C] text-xs font-semibold">
            <Sparkles size={14} className="text-[#D4A24C] animate-pulse" />
            <span>Trợ Lý AI Lập Kế Hoạch Du Lịch</span>
          </div>
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
            Trợ Lý AI Du Lịch Thông Minh 24/7
          </h1>
          <p className="text-stone-300 text-sm">
            Tự động gợi ý tour phù hợp với ngân sách cá nhân bằng công nghệ tìm kiếm thông minh và giải đáp thắc mắc 24/7.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="pt-6 flex gap-3">
          <button
            onClick={() => setActiveTab("PLANNER")}
            className={`px-5 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 transition-all ${
              activeTab === "PLANNER"
                ? "bg-[#E2603F] text-white shadow-lg shadow-[#E2603F]/30"
                : "bg-[#123A3A] text-stone-300 hover:text-white"
            }`}
          >
            <Compass size={16} /> Gợi ý lịch trình thông minh
          </button>
          <button
            onClick={() => setActiveTab("CHAT")}
            className={`px-5 py-2.5 rounded-xl font-bold text-xs flex items-center gap-2 transition-all ${
              activeTab === "CHAT"
                ? "bg-[#E2603F] text-white shadow-lg shadow-[#E2603F]/30"
                : "bg-[#123A3A] text-stone-300 hover:text-white"
            }`}
          >
            <MessageSquare size={16} /> Chatbot Tư Vấn 24/7
          </button>
        </div>
      </div>

      {/* Tab 1: Smart Itinerary Planner */}
      {activeTab === "PLANNER" && (
        <div className="space-y-8">
          <form onSubmit={handleAskRAG} className="bg-white p-6 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-4">
            <label className="text-xs font-bold text-stone-700 block">Nhu cầu du lịch của bạn:</label>
            <textarea
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              rows={3}
              placeholder="Ví dụ: Tôi muốn đi du lịch 3 ngày 2 đêm ở Đà Nẵng cho gia đình có trẻ nhỏ, ngân sách 10 triệu..."
              className="w-full text-sm p-4 bg-[#FBF8F2] border border-[#e7e0d3] rounded-2xl focus:outline-none focus:border-[#E2603F] resize-none"
            />
            <div className="flex flex-col sm:flex-row gap-3">
              <div className="flex items-center gap-2 flex-1 rounded-xl border border-[#e7e0d3] bg-[#FBF8F2] px-3 py-2 text-xs">
                <MapPin size={16} className="text-[#E2603F] shrink-0" />
                <input
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  placeholder="Điểm đến ưu tiên (ví dụ: Đà Nẵng)"
                  className="w-full bg-transparent focus:outline-none"
                />
              </div>
              <div className="flex items-center gap-2 flex-1 rounded-xl border border-[#e7e0d3] bg-[#FBF8F2] px-3 py-2 text-xs">
                <Wallet size={16} className="text-[#D4A24C] shrink-0" />
                <input
                  value={maxBudget}
                  onChange={(e) => setMaxBudget(e.target.value)}
                  type="number"
                  placeholder="Ngân sách tối đa (VNĐ)"
                  className="w-full bg-transparent focus:outline-none"
                />
              </div>
              <button
                type="submit"
                disabled={loading || !query.trim()}
                className="bg-[#E2603F] hover:bg-[#d55333] text-white font-bold text-xs px-6 py-3 rounded-xl shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {loading ? <RefreshCw size={14} className="animate-spin" /> : <Send size={14} />}
                <span>Gửi Cho AI</span>
              </button>
            </div>
          </form>

          {/* Prompt Presets */}
          {!result && !loading && (
            <div className="space-y-2">
              <span className="text-xs font-semibold text-stone-500 block">Gợi ý mẫu:</span>
              <div className="flex flex-wrap gap-2">
                {SUGGESTIONS.map((s) => (
                  <button
                    key={s}
                    onClick={() => handleAskRAG(undefined, s)}
                    className="text-xs rounded-xl border border-[#e7e0d3] bg-white px-3.5 py-2 text-stone-700 hover:border-[#E2603F] hover:text-[#E2603F] transition-all shadow-sm"
                  >
                    {s}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Loading Skeleton */}
          {loading && (
            <div className="p-8 bg-white rounded-3xl border border-[#e7e0d3] space-y-4 animate-pulse">
              <div className="h-5 w-1/3 bg-stone-200 rounded" />
              <div className="h-20 bg-stone-200 rounded-2xl" />
              <div className="h-32 bg-stone-200 rounded-2xl" />
            </div>
          )}

          {/* AI Recommendation Output */}
          {result && (
            <div className="bg-white p-6 sm:p-8 rounded-3xl border border-[#e7e0d3] shadow-sm space-y-8">
              {/* Introduction */}
              <div className="p-4 bg-[#FBF8F2] border border-[#e7e0d3] rounded-2xl text-stone-800 text-sm leading-relaxed">
                <p className="font-semibold text-stone-900 mb-1 flex items-center gap-1.5">
                  <Sparkles size={16} className="text-[#D4A24C]" /> Lời Tư Vấn Từ AI Assistant:
                </p>
                {result.introduction}
              </div>

              {/* Recommended Tours */}
              {(result.recommendedTours ?? (result as any).recommended_tours ?? []).length > 0 && (
                <div className="space-y-4">
                  <h3 className="font-bold text-stone-900 text-lg flex items-center gap-2">
                    <Compass size={20} className="text-[#E2603F]" /> Tour Được Đề Xuất
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {(result.recommendedTours ?? (result as any).recommended_tours ?? []).map((t: any) => (
                      <Link
                        key={t.tourId || t.tour_id}
                        href={t.bookingUrl || t.booking_url || `/tours/${t.tourId || t.tour_id}`}
                        className="group block bg-[#FBF8F2] p-5 rounded-2xl border border-[#e7e0d3] hover:border-[#E2603F]/50 hover:bg-white transition-all shadow-sm"
                      >
                        <div className="flex items-center justify-between mb-2">
                          <span className="font-mono text-[10px] font-bold bg-[#0D2B2B] text-[#D4A24C] px-2 py-0.5 rounded border border-[#D4A24C]/30">
                            {t.code || "TOUR"}
                          </span>
                          <span className="font-bold text-[#E2603F] text-sm">
                            {formatCurrency(t.pricePerAdult || t.price || 4500000)}
                          </span>
                        </div>
                        <h4 className="font-bold text-stone-900 text-sm group-hover:text-[#E2603F] transition-colors leading-snug mb-1">
                          {t.title}
                        </h4>
                        <p className="text-xs text-stone-500 leading-relaxed">{t.reason}</p>
                        <div className="mt-3 flex items-center gap-1 text-xs font-bold text-[#E2603F]">
                          <span>Xem chi tiết tour</span>
                          <ArrowRight size={14} />
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              )}

              {/* Detailed Itinerary */}
              {(result.detailedItinerary ?? (result as any).detailed_itinerary ?? []).length > 0 && (
                <div className="space-y-4 pt-4 border-t border-[#f5f0e6]">
                  <h3 className="font-bold text-stone-900 text-lg">Gợi Ý Lịch Trình Chi Tiết</h3>
                  <div className="space-y-3">
                    {(result.detailedItinerary ?? (result as any).detailed_itinerary ?? []).map((d: any) => (
                      <div key={d.day} className="flex items-start gap-3 p-4 bg-[#FBF8F2] rounded-2xl border border-[#e7e0d3]">
                        <span className="w-7 h-7 rounded-xl bg-[#0D2B2B] text-[#D4A24C] text-xs font-bold flex items-center justify-center shrink-0 border border-[#D4A24C]/30">
                          N{d.day}
                        </span>
                        <div className="space-y-1">
                          <p className="font-bold text-stone-900 text-sm">{d.title || `Ngày ${d.day}`}</p>
                          <p className="text-xs text-stone-600 leading-relaxed">{d.description || d.activity}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Tab 2: 24/7 Chatbot */}
      {activeTab === "CHAT" && (
        <div className="bg-white rounded-3xl border border-[#e7e0d3] shadow-sm overflow-hidden flex flex-col h-[550px]">
          {/* Chat Messages Log */}
          <div className="flex-1 p-6 overflow-y-auto space-y-4 bg-[#FBF8F2]">
            {messages.map((m) => (
              <div
                key={m.id}
                className={`flex items-start gap-3 ${m.sender === "user" ? "flex-row-reverse" : "flex-row"}`}
              >
                <div
                  className={`w-8 h-8 rounded-xl flex items-center justify-center text-white shrink-0 ${
                    m.sender === "user" ? "bg-[#E2603F]" : "bg-[#0D2B2B]"
                  }`}
                >
                  {m.sender === "user" ? <User size={16} /> : <Bot size={16} className="text-[#D4A24C]" />}
                </div>
                <div
                  className={`max-w-md p-4 rounded-2xl text-xs sm:text-sm leading-relaxed ${
                    m.sender === "user"
                      ? "bg-[#E2603F] text-white rounded-tr-none shadow-md"
                      : "bg-white text-stone-800 border border-[#e7e0d3] rounded-tl-none shadow-sm"
                  }`}
                >
                  <p className="whitespace-pre-line">{m.text}</p>
                  <span className={`text-[10px] block mt-1 ${m.sender === "user" ? "text-amber-100 text-right" : "text-stone-400"}`}>
                    {m.timestamp}
                  </span>
                </div>
              </div>
            ))}
            {chatLoading && (
              <div className="flex items-center gap-2 text-xs text-stone-400 italic">
                <Bot size={16} className="animate-spin text-[#E2603F]" />
                <span>AI Bot đang soạn câu trả lời...</span>
              </div>
            )}
          </div>

          {/* Chat Input Form */}
          <form onSubmit={handleSendChat} className="p-4 bg-white border-t border-[#e7e0d3] flex gap-3">
            <input
              value={chatInput}
              onChange={(e) => setChatInput(e.target.value)}
              placeholder="Nhập câu hỏi (ví dụ: Quy định hủy vé, hoàn tiền ra sao?)..."
              className="flex-1 bg-[#FBF8F2] border border-[#e7e0d3] rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-[#E2603F]"
            />
            <button
              type="submit"
              disabled={chatLoading || !chatInput.trim()}
              className="bg-[#E2603F] hover:bg-[#d55333] text-white font-bold px-5 py-3 rounded-xl shadow-md transition-all flex items-center gap-2 disabled:opacity-50"
            >
              <Send size={16} />
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
