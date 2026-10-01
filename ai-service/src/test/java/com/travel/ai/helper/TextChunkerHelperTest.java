package com.travel.ai.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.constant.ChunkType;
import com.travel.ai.dto.ItineraryItemDto;
import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.entity.TourEmbeddingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerHelperTest {

    private TextChunkerHelper textChunkerHelper;

    @BeforeEach
    void setUp() {
        textChunkerHelper = new TextChunkerHelper(new ObjectMapper());
    }

    @Test
    @DisplayName("Tạo Semantic Chunks thành công từ TourUpdatedEvent")
    void createChunks_Success() {
        UUID tourId = UUID.randomUUID();
        TourUpdatedEvent tour = TourUpdatedEvent.builder()
                .tourId(tourId)
                .code("TOUR-DN-01")
                .title("Tour Đà Nẵng - Hội An 3N2Đ")
                .description("Tour du lịch biển Đà Nẵng tuyệt đẹp")
                .destination("Đà Nẵng")
                .minPrice(new BigDecimal("4500000"))
                .durationDays(3)
                .suitableFor("Gia đình")
                .itineraries(List.of(
                        ItineraryItemDto.builder().dayNumber(1).title("Ngày 1").description("Vui chơi Bà Nà Hills").activity("Cáp treo").build(),
                        ItineraryItemDto.builder().dayNumber(2).title("Ngày 2").description("Tham quan Phố cổ Hội An").activity("Đi bộ").build()
                ))
                .build();

        List<TourEmbeddingEntity> chunks = textChunkerHelper.createChunks(tour);

        assertNotNull(chunks);
        assertEquals(3, chunks.size()); // 1 Metadata Chunk + 2 Itinerary Day Chunks

        TourEmbeddingEntity metadataChunk = chunks.get(0);
        assertEquals(ChunkType.METADATA, metadataChunk.getChunkType());
        assertTrue(metadataChunk.getContent().contains("Tour Đà Nẵng - Hội An 3N2Đ"));
        assertTrue(metadataChunk.getContent().contains("4500000"));

        TourEmbeddingEntity day1Chunk = chunks.get(1);
        assertEquals(ChunkType.ITINERARY_DAY, day1Chunk.getChunkType());
        assertTrue(day1Chunk.getContent().contains("Bà Nà Hills"));
    }
}
