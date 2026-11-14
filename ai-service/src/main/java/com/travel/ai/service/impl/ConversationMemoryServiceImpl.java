package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.ChatMessageDto;
import com.travel.ai.service.ConversationMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMemoryServiceImpl implements ConversationMemoryService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CONVERSATION_KEY_PREFIX = "ai:chat:history:";
    private static final Duration CONVERSATION_TTL = Duration.ofMinutes(30);
    private static final long MAX_STORED_MESSAGES = 20;

    @Override
    public List<ChatMessageDto> getRecentMessages(String conversationId, int limit) {
        if (conversationId == null || conversationId.isBlank() || limit <= 0) {
            return Collections.emptyList();
        }

        String key = buildRedisKey(conversationId);
        try {
            // Lấy `limit` phần tử cuối cùng trong Redis List
            Long size = redisTemplate.opsForList().size(key);
            if (size == null || size == 0) {
                return Collections.emptyList();
            }

            long start = Math.max(0, size - limit);
            List<String> rawMessages = redisTemplate.opsForList().range(key, start, -1);

            if (rawMessages == null || rawMessages.isEmpty()) {
                return Collections.emptyList();
            }

            List<ChatMessageDto> messages = new ArrayList<>(rawMessages.size());
            for (String rawJson : rawMessages) {
                try {
                    ChatMessageDto msg = objectMapper.readValue(rawJson, ChatMessageDto.class);
                    messages.add(msg);
                } catch (Exception parseEx) {
                    log.warn("Không thể parse tin nhắn JSON từ Redis: {}", parseEx.getMessage());
                }
            }

            log.info("Lấy thành công {} tin nhắn ngữ cảnh cho Conversation [{}]", messages.size(), conversationId);
            return messages;
        } catch (Exception e) {
            log.warn("Lỗi khi đọc lịch sử hội thoại từ Redis cho Conversation [{}]: {}", conversationId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void saveMessage(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank() || content == null || content.isBlank()) {
            return;
        }

        String key = buildRedisKey(conversationId);
        ChatMessageDto message = ChatMessageDto.builder()
                .role(role != null ? role.trim() : "user")
                .content(content.trim())
                .timestamp(System.currentTimeMillis())
                .build();

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);

            // 1. Đẩy tin nhắn vào cuối danh sách
            redisTemplate.opsForList().rightPush(key, jsonMessage);

            // 2. Giới hạn tối đa MAX_STORED_MESSAGES tin nhắn gần nhất để tránh phình bộ nhớ
            redisTemplate.opsForList().trim(key, -MAX_STORED_MESSAGES, -1);

            // 3. Gia hạn thời gian sống TTL 30 phút sau mỗi lượt trao đổi
            redisTemplate.expire(key, CONVERSATION_TTL);

            log.info("Đã lưu tin nhắn vai trò [{}] cho Conversation [{}] vào Redis (TTL 30 phút)", role, conversationId);
        } catch (Exception e) {
            log.warn("Lỗi khi lưu tin nhắn vào Redis cho Conversation [{}]: {}", conversationId, e.getMessage());
        }
    }

    @Override
    public void clearConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        String key = buildRedisKey(conversationId);
        try {
            redisTemplate.delete(key);
            log.info("Đã xóa phiên hội thoại [{}] trên Redis", conversationId);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa phiên hội thoại [{}] trên Redis: {}", conversationId, e.getMessage());
        }
    }

    private String buildRedisKey(String conversationId) {
        return CONVERSATION_KEY_PREFIX + conversationId.trim();
    }
}
