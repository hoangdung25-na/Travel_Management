package com.travel.ai.dto;

import com.travel.ai.constant.ChunkType;
import lombok.*;

import java.util.UUID;

/**
 * DTO chứa kết quả trích xuất ngữ cảnh từ Vector DB pgvector kèm điểm tương đồng (Similarity Score)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrievedChunkDto {

    private UUID id;
    private UUID tourId;
    private ChunkType chunkType;
    private String content;
    private String metadata;
    private Double similarityScore;
}
