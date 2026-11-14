package com.travel.ai.service;

import com.travel.ai.dto.ChatMessageDto;

import java.util.List;

/**
 * Service quản lý lịch sử hội thoại nhiều lượt (Multi-turn Context Memory) lưu trữ trên Redis
 */
public interface ConversationMemoryService {

    /**
     * Lấy danh sách N tin nhắn gần nhất của phiên hội thoại để làm ngữ cảnh cho AI
     *
     * @param conversationId Mã phiên hội thoại
     * @param limit Số lượng tin nhắn tối đa cần lấy (thường là 4 - 6 tin)
     * @return Danh sách tin nhắn theo thứ tự thời gian từ cũ đến mới
     */
    List<ChatMessageDto> getRecentMessages(String conversationId, int limit);

    /**
     * Lưu một tin nhắn mới (của người dùng hoặc của AI) vào Redis
     *
     * @param conversationId Mã phiên hội thoại
     * @param role Vai trò người gửi ("user" hoặc "assistant")
     * @param content Nội dung tin nhắn
     */
    void saveMessage(String conversationId, String role, String content);

    /**
     * Xóa toàn bộ lịch sử hội thoại khi người dùng muốn bắt đầu phiên mới
     *
     * @param conversationId Mã phiên hội thoại
     */
    void clearConversation(String conversationId);
}
