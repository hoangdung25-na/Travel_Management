package com.travel.ai.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.constant.ChunkType;
import com.travel.ai.dto.ItineraryItemDto;
import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.entity.TourEmbeddingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Helper thực hiện chiến lược Semantic Text Chunking cho RAG Du lịch:
 * 1. Metadata Chunk: Thông tin tổng quan Tour (Mã, điểm đến, giá, đối tượng phù hợp).
 * 2. Itinerary Day Chunks: Lịch trình chi tiết từng ngày của Tour.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TextChunkerHelper {

    private final ObjectMapper objectMapper;

    /**
     * Tách dữ liệu Tour thành các đoạn văn bản (Chunks) chuẩn ngữ nghĩa
     */
    public List<TourEmbeddingEntity> createChunks(TourUpdatedEvent tour) {
        List<TourEmbeddingEntity> chunks = new ArrayList<>();

        // 1. Tạo Metadata Chunk (Thông tin tổng quan)
        String metadataContent = String.format(
                "Tour: %s (Mã Tour: %s). Điểm đến: %s. Thời gian: %d ngày. Mức giá từ: %s VNĐ. Đối tượng phù hợp: %s. Mô tả: %s",
                tour.getTitle(),
                tour.getCode() != null ? tour.getCode() : "N/A",
                tour.getDestination() != null ? tour.getDestination() : "N/A",
                tour.getDurationDays() != null ? tour.getDurationDays() : 1,
                tour.getMinPrice() != null ? tour.getMinPrice().toPlainString() : "Liên hệ",
                tour.getSuitableFor() != null ? tour.getSuitableFor() : "Mọi đối tượng",
                tour.getDescription() != null ? tour.getDescription() : ""
        );

        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("tour_id", tour.getTourId().toString());
        metadataMap.put("tour_code", tour.getCode());
        metadataMap.put("title", tour.getTitle());
        metadataMap.put("destination", tour.getDestination());
        metadataMap.put("price", tour.getMinPrice() != null ? tour.getMinPrice().doubleValue() : 0);
        metadataMap.put("chunk_type", ChunkType.METADATA.name());

        chunks.add(TourEmbeddingEntity.builder()
                .tourId(tour.getTourId())
                .chunkType(ChunkType.METADATA)
                .content(metadataContent)
                .metadata(toJsonString(metadataMap))
                .build());

        // 2. Tạo Itinerary Day Chunks (Chi tiết lịch trình từng ngày)
        if (tour.getItineraries() != null && !tour.getItineraries().isEmpty()) {
            for (ItineraryItemDto itinerary : tour.getItineraries()) {
                String dayContent = String.format(
                        "Tour %s - Ngày %d: %s. Lịch trình chi tiết: %s. Hoạt động: %s",
                        tour.getTitle(),
                        itinerary.getDayNumber() != null ? itinerary.getDayNumber() : 1,
                        itinerary.getTitle() != null ? itinerary.getTitle() : "",
                        itinerary.getDescription() != null ? itinerary.getDescription() : "",
                        itinerary.getActivity() != null ? itinerary.getActivity() : ""
                );

                Map<String, Object> dayMetadataMap = new HashMap<>();
                dayMetadataMap.put("tour_id", tour.getTourId().toString());
                dayMetadataMap.put("tour_code", tour.getCode());
                dayMetadataMap.put("title", tour.getTitle());
                dayMetadataMap.put("day_number", itinerary.getDayNumber());
                dayMetadataMap.put("chunk_type", ChunkType.ITINERARY_DAY.name());

                chunks.add(TourEmbeddingEntity.builder()
                        .tourId(tour.getTourId())
                        .chunkType(ChunkType.ITINERARY_DAY)
                        .content(dayContent)
                        .metadata(toJsonString(dayMetadataMap))
                        .build());
            }
        }

        return chunks;
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Lỗi chuyển đổi metadata thành JSON string: {}", e.getMessage());
            return "{}";
        }
    }
}
