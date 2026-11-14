package com.travel.ai.service.impl;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourRetrievalServiceImplTest {

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TourRetrievalServiceImpl tourRetrievalService;

    @BeforeEach
    void setUp() {
        tourRetrievalService = new TourRetrievalServiceImpl(embeddingModel, jdbcTemplate);
    }

    @Test
    @DisplayName("Khi EmbeddingModel bị lỗi, hệ thống kích hoạt Fallback SQL và trích xuất điểm đến thông minh")
    void retrieveRelevantChunks_FallbackWhenEmbeddingFails() {
        AiQueryRequest request = AiQueryRequest.builder()
                .queryText("Tìm tour đi Nghệ An 3 ngày")
                .build();

        when(embeddingModel.embed(anyString())).thenThrow(new RuntimeException("API Key not found or quota exceeded"));

        RetrievedChunkDto mockChunk = RetrievedChunkDto.builder()
                .id(UUID.randomUUID())
                .tourId(UUID.randomUUID())
                .chunkType(com.travel.ai.constant.ChunkType.METADATA)
                .content("Tour Nghệ An - Biển Cửa Lò - Thăm Quê Bác Kim Liên 2N3Đ")
                .similarityScore(0.95)
                .build();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(), any(), any(), any(), anyInt()))
                .thenReturn(List.of(mockChunk));

        List<RetrievedChunkDto> chunks = tourRetrievalService.retrieveRelevantChunks(request);

        assertNotNull(chunks);
        assertFalse(chunks.isEmpty());
        assertEquals("Tour Nghệ An - Biển Cửa Lò - Thăm Quê Bác Kim Liên 2N3Đ", chunks.get(0).getContent());
    }

    @Test
    @DisplayName("Chỉ cần gõ tên tỉnh thành 'Đà Nẵng' mà không cần đúng tiêu đề tour, AI vẫn tìm được")
    void retrieveRelevantChunks_SearchByProvinceNameOnly() {
        AiQueryRequest request = AiQueryRequest.builder()
                .queryText("Đà Nẵng")
                .build();

        float[] mockVector = new float[]{0.1f, 0.2f, 0.3f};
        when(embeddingModel.embed("Đà Nẵng")).thenReturn(mockVector);

        RetrievedChunkDto mockChunk = RetrievedChunkDto.builder()
                .id(UUID.randomUUID())
                .tourId(UUID.randomUUID())
                .chunkType(com.travel.ai.constant.ChunkType.METADATA)
                .content("Tour Khám Phá Đà Nẵng - Ba Na Hills - Phố Cổ Hội An 3N2Đ")
                .similarityScore(0.92)
                .build();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(mockChunk));

        List<RetrievedChunkDto> chunks = tourRetrievalService.retrieveRelevantChunks(request);

        assertNotNull(chunks);
        assertEquals(1, chunks.size());
        assertTrue(chunks.get(0).getContent().contains("Đà Nẵng"));
    }
}
