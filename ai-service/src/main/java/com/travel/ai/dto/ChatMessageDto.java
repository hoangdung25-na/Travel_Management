package com.travel.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO đại diện cho một tin nhắn trong phiên hội thoại với AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Vai trò của người gửi: "user" hoặc "assistant"
     */
    private String role;

    /**
     * Nội dung văn bản của tin nhắn
     */
    private String content;

    /**
     * Thời điểm gửi tin nhắn (Unix timestamp tính bằng millisecond)
     */
    private long timestamp;
}
