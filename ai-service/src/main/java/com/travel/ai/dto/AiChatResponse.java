package com.travel.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload trả về từ API trò chuyện với AI Chatbot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    /**
     * Mã định danh phiên hội thoại (client cần lưu lại để gửi tiếp cho câu hỏi sau)
     */
    private String conversationId;

    /**
     * Nội dung câu trả lời từ AI
     */
    private String reply;
}
