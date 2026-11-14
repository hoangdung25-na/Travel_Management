package com.travel.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload cho API trò chuyện 24/7 với Trợ lý AI (Multi-turn Chat)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    /**
     * Mã định danh phiên hội thoại (UUID).
     * Nếu client không truyền, hệ thống sẽ tự động khởi tạo mới.
     */
    private String conversationId;

    /**
     * Nội dung câu hỏi của người dùng
     */
    private String promptText;

    /**
     * Tên trường thay thế (hỗ trợ tương thích ngược với Frontend)
     */
    private String message;

    /**
     * Lấy nội dung câu hỏi hợp lệ từ promptText hoặc message
     */
    public String getEffectiveMessage() {
        if (promptText != null && !promptText.isBlank()) {
            return promptText.trim();
        }
        if (message != null && !message.isBlank()) {
            return message.trim();
        }
        return "";
    }
}
