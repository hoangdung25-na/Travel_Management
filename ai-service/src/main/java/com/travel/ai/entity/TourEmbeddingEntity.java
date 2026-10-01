package com.travel.ai.entity;

import com.travel.ai.constant.ChunkType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity quản lý dữ liệu lưu trữ Vector Embedding cho Tour Du lịch trong db_travel_ai
 */
@Entity
@Table(name = "tour_embeddings", indexes = {
        @Index(name = "idx_tour_embeddings_tour_id", columnList = "tour_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourEmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chunk_type", nullable = false, length = 50)
    private ChunkType chunkType;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
