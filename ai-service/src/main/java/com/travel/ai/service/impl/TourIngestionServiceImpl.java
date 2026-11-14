package com.travel.ai.service.impl;

import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.entity.TourEmbeddingEntity;
import com.travel.ai.helper.TextChunkerHelper;
import com.travel.ai.repository.TourEmbeddingRepository;
import com.travel.ai.service.TourIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourIngestionServiceImpl implements TourIngestionService {

    private final TourEmbeddingRepository tourEmbeddingRepository;
    private final TextChunkerHelper textChunkerHelper;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void processTourUpdate(TourUpdatedEvent event) {
        log.info("Bắt đầu xử lý Indexing RAG cho Tour ID: {}, Title: {}", event.getTourId(), event.getTitle());

        // 1. Chống trôi dữ liệu (Drift Guardrail): Xóa tất cả Chunks cũ của Tour này trong CSDL
        tourEmbeddingRepository.deleteByTourId(event.getTourId());
        log.info("Đã xóa sạch các Vector Chunks cũ của Tour ID: {}", event.getTourId());

        // 2. Phân đoạn văn bản (Text Chunking Strategy)
        List<TourEmbeddingEntity> chunks = textChunkerHelper.createChunks(event);
        log.info("Đã phân đoạn thành {} Chunks chuẩn cho Tour ID: {}", chunks.size(), event.getTourId());

        // 3. Tạo Vector Embedding (Google Gemini text-embedding-004) và lưu vào pgvector

        for (TourEmbeddingEntity chunk : chunks) {
            float[] vector = null;
            try {
                vector = embeddingModel.embed(chunk.getContent());
            } catch (Exception e) {
                log.warn("Cảnh báo khi gọi Embedding Vector API cho Chunk: {}. Vẫn lưu dữ liệu văn bản Chunk vào CSDL để tìm kiếm SQL Fallback.", e.getMessage());
            }

            // Lưu bản ghi vào bảng tour_embeddings
            TourEmbeddingEntity savedEntity = tourEmbeddingRepository.save(chunk);
            tourEmbeddingRepository.flush();

            // Nếu tạo được Vector, cập nhật mảng Float Vector vào cột 'embedding' kiểu 'vector(768)' trong PostgreSQL
            if (vector != null) {
                try {
                    String vectorSql = "UPDATE tour_embeddings SET embedding = ?::vector WHERE id = ?";
                    jdbcTemplate.update(vectorSql, Arrays.toString(vector), savedEntity.getId());
                } catch (Exception ex) {
                    log.warn("Cảnh báo khi ghi Vector vào pgvector: {}", ex.getMessage());
                }
            }
        }

        log.info("Hoàn tất PIPELINE 1: Indexing RAG thành công cho Tour ID: {}", event.getTourId());
    }
}
