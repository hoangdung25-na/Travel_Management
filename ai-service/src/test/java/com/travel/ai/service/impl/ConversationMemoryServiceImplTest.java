package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.ChatMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationMemoryServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    private ObjectMapper objectMapper;
    private ConversationMemoryServiceImpl conversationMemoryService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        conversationMemoryService = new ConversationMemoryServiceImpl(redisTemplate, objectMapper);
    }

    @Test
    @DisplayName("Test 3.1: Lưu tin nhắn hội thoại vào Redis với TTL 30 phút và giới hạn 20 tin nhắn")
    void saveMessage_ValidInput_PushesToRedisAndSetsTtl() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        String conversationId = "conv-123";
        conversationMemoryService.saveMessage(conversationId, "user", "Tôi muốn tìm tour Sa Pa");

        String expectedKey = "ai:chat:history:conv-123";
        verify(listOperations, times(1)).rightPush(eq(expectedKey), anyString());
        verify(listOperations, times(1)).trim(eq(expectedKey), eq(-20L), eq(-1L));
        verify(redisTemplate, times(1)).expire(eq(expectedKey), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Test 3.2: Lấy tin nhắn ngữ cảnh gần nhất theo giới hạn limit")
    void getRecentMessages_ValidMessagesInRedis_ReturnsLimitedList() throws Exception {
        when(redisTemplate.opsForList()).thenReturn(listOperations);

        String conversationId = "conv-123";
        String expectedKey = "ai:chat:history:conv-123";

        ChatMessageDto msg1 = ChatMessageDto.builder().role("user").content("Đi Sa Pa 3N2Đ").timestamp(1000L).build();
        ChatMessageDto msg2 = ChatMessageDto.builder().role("assistant").content("Dạ em gợi ý Tour Sa Pa").timestamp(2000L).build();

        String json1 = objectMapper.writeValueAsString(msg1);
        String json2 = objectMapper.writeValueAsString(msg2);

        when(listOperations.size(expectedKey)).thenReturn(2L);
        when(listOperations.range(expectedKey, 0L, -1L)).thenReturn(List.of(json1, json2));

        List<ChatMessageDto> result = conversationMemoryService.getRecentMessages(conversationId, 5);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user", result.get(0).getRole());
        assertEquals("Đi Sa Pa 3N2Đ", result.get(0).getContent());
        assertEquals("assistant", result.get(1).getRole());
        assertEquals("Dạ em gợi ý Tour Sa Pa", result.get(1).getContent());
    }

    @Test
    @DisplayName("Test 3.3: Xóa phiên hội thoại khi bắt đầu phiên mới")
    void clearConversation_ValidId_DeletesKey() {
        String conversationId = "conv-reset";
        conversationMemoryService.clearConversation(conversationId);

        verify(redisTemplate, times(1)).delete("ai:chat:history:conv-reset");
    }
}
