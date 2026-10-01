package com.travel.ai.constant;

/**
 * Phân loại đoạn văn bản Vector (Text Chunk)
 */
public enum ChunkType {
    /**
     * Chunk chứa thông tin tổng quan Tour (Tên, điểm đến, giá, đối tượng phù hợp, số ngày đi)
     */
    METADATA,

    /**
     * Chunk chứa thông tin chi tiết lịch trình theo từng ngày
     */
    ITINERARY_DAY
}
