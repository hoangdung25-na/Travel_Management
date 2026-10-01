package com.travel.ai.service.impl;

import com.travel.ai.constant.ChunkType;
import com.travel.ai.dto.ItineraryItemDto;
import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.entity.TourEmbeddingEntity;
import com.travel.ai.helper.TextChunkerHelper;
import com.travel.ai.repository.TourEmbeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourIngestionServiceImplTest {

    @Mock
    private TourEmbeddingRepository tourEmbeddingRepository;

    @Mock
    private TextChunkerHelper textChunkerHelper;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TourIngestionServiceImpl tourIngestionService;

    private UUID tourId;
    private TourUpdatedEvent tourEvent;

    @BeforeEach
    void setUp() {
        tourId = UUID.randomUUID();
        tourEvent = TourUpdatedEvent.builder()
                .tourId(tourId)
                .code("TOUR-NT-01")
                .title("Tour Nha Trang 3N2Đ")
                .description("Du lịch đảo Nha Trang")
                .destination("Nha Trang")
                .minPrice(new BigDecimal("4800000"))
                .durationDays(3)
                .suitableFor("Gia đình")
                .itineraries(List.of(
                        ItineraryItemDto.builder().dayNumber(1).title("Ngày 1").description("Tắm biển").build()
                ))
                .build();
    }

    @Test
    @DisplayName("Test 1: Data Ingestion & Drift Guardrail - Xóa chunks cũ và lưu vector chunks mới thành công")
    void processTourUpdate_DataIngestionAndDriftGuardrail_Success() {
        TourEmbeddingEntity chunk = TourEmbeddingEntity.builder()
                .id(UUID.randomUUID())
                .tourId(tourId)
                .chunkType(ChunkType.METADATA)
                .content("Tour Nha Trang 3N2Đ...")
                .build();

        when(textChunkerHelper.createChunks(any())).thenReturn(List.of(chunk));
        when(embeddingModel.embed(anyString())).thenReturn(new float[768]);
        when(tourEmbeddingRepository.save(any())).thenReturn(chunk);

        tourIngestionService.processTourUpdate(tourEvent);

        verify(tourEmbeddingRepository, times(1)).deleteByTourId(tourId);
        verify(textChunkerHelper, times(1)).createChunks(tourEvent);
        verify(embeddingModel, times(1)).embed(anyString());
        verify(tourEmbeddingRepository, times(1)).save(chunk);
        verify(jdbcTemplate, times(1)).update(anyString(), anyString(), eq(chunk.getId()));
    }
}
