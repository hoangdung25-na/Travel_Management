package com.travel.ai.repository;

import com.travel.ai.entity.TourEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository thao tác với bảng tour_embeddings
 */
@Repository
public interface TourEmbeddingRepository extends JpaRepository<TourEmbeddingEntity, UUID> {

    /**
     * Tìm danh sách Chunks theo tourId
     */
    List<TourEmbeddingEntity> findByTourId(UUID tourId);

    /**
     * Xóa sạch các Chunks cũ của một Tour khi có sự thay đổi dữ liệu (Drift Guardrail)
     */
    @Modifying
    @Query("DELETE FROM TourEmbeddingEntity e WHERE e.tourId = :tourId")
    void deleteByTourId(@Param("tourId") UUID tourId);
}
