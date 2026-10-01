package com.travel.ai.service.impl;

import com.travel.ai.constant.ChunkType;
import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;
import com.travel.ai.service.TourRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourRetrievalServiceImpl implements TourRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<RetrievedChunkDto> retrieveRelevantChunks(AiQueryRequest request) {
        log.info("Bắt đầu PIPELINE 2: Retrieval cho câu hỏi: '{}'", request.getQueryText());
        int limit = (request.getTopK() != null && request.getTopK() > 0) ? request.getTopK() : 5;

        try {
            // 1. Mã hóa Yêu cầu Người dùng (Query Embedding) -> Vector 768 chiều
            float[] queryVector = embeddingModel.embed(request.getQueryText());
            String vectorStr = Arrays.toString(queryVector);

            Double maxBudgetVal = request.getMaxBudget() != null ? request.getMaxBudget().doubleValue() : null;

            String sql;
            Object[] params;

            if (maxBudgetVal != null) {
                sql = """
                    SELECT id, tour_id, chunk_type, content, metadata,
                           1 - (embedding <=> ?::vector) AS similarity_score
                    FROM tour_embeddings
                    WHERE embedding IS NOT NULL
                      AND (metadata->>'price')::numeric <= ?
                    ORDER BY embedding <=> ?::vector ASC
                    LIMIT ?
                    """;
                params = new Object[]{ vectorStr, maxBudgetVal, vectorStr, limit };
            } else {
                sql = """
                    SELECT id, tour_id, chunk_type, content, metadata,
                           1 - (embedding <=> ?::vector) AS similarity_score
                    FROM tour_embeddings
                    WHERE embedding IS NOT NULL
                    ORDER BY embedding <=> ?::vector ASC
                    LIMIT ?
                    """;
                params = new Object[]{ vectorStr, vectorStr, limit };
            }

            List<RetrievedChunkDto> results = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> RetrievedChunkDto.builder()
                            .id(UUID.fromString(rs.getString("id")))
                            .tourId(UUID.fromString(rs.getString("tour_id")))
                            .chunkType(ChunkType.valueOf(rs.getString("chunk_type")))
                            .content(rs.getString("content"))
                            .metadata(rs.getString("metadata"))
                            .similarityScore(rs.getDouble("similarity_score"))
                            .build(),
                    params
            );

            log.info("Tìm thấy {} Chunks ngữ cảnh phù hợp từ pgvector", results.size());
            return results;

        } catch (Exception e) {
            log.warn("Chuyển sang tìm kiếm ngữ cảnh văn bản SQL: {}", e.getMessage());

            String queryText = request.getQueryText() != null ? request.getQueryText() : "";
            String searchKeyword = "%" + queryText.trim() + "%";

            String fallbackSql = """
                SELECT id, tour_id, chunk_type, content, metadata, 0.85 AS similarity_score
                FROM tour_embeddings
                WHERE LOWER(content) LIKE LOWER(?)
                LIMIT ?
                """;

            List<RetrievedChunkDto> fallbackResults = jdbcTemplate.query(
                    fallbackSql,
                    (rs, rowNum) -> RetrievedChunkDto.builder()
                            .id(UUID.fromString(rs.getString("id")))
                            .tourId(UUID.fromString(rs.getString("tour_id")))
                            .chunkType(ChunkType.valueOf(rs.getString("chunk_type")))
                            .content(rs.getString("content"))
                            .metadata(rs.getString("metadata"))
                            .similarityScore(rs.getDouble("similarity_score"))
                            .build(),
                    searchKeyword,
                    limit
            );

            // Nếu câu tìm kiếm dài không khớp nguyên văn, tìm kiếm theo từ khóa (Ví dụ: Nghệ An, Đà Nẵng, Sa Pa...)
            if (fallbackResults.isEmpty()) {
                String[] words = queryText.split("\\s+");
                List<String> keywords = Arrays.stream(words)
                        .map(w -> w.replaceAll("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ]", ""))
                        .filter(w -> w.length() >= 2)
                        .toList();

                for (String kw : keywords) {
                    if (fallbackResults.size() >= limit) break;
                    List<RetrievedChunkDto> kwMatches = jdbcTemplate.query(
                            "SELECT id, tour_id, chunk_type, content, metadata, 0.82 AS similarity_score FROM tour_embeddings WHERE LOWER(content) LIKE LOWER(?) LIMIT ?",
                            (rs, rowNum) -> RetrievedChunkDto.builder()
                                    .id(UUID.fromString(rs.getString("id")))
                                    .tourId(UUID.fromString(rs.getString("tour_id")))
                                    .chunkType(ChunkType.valueOf(rs.getString("chunk_type")))
                                    .content(rs.getString("content"))
                                    .metadata(rs.getString("metadata"))
                                    .similarityScore(rs.getDouble("similarity_score"))
                                    .build(),
                            "%" + kw + "%",
                            limit
                    );
                    fallbackResults.addAll(kwMatches);
                }
            }

            // Nếu vẫn rỗng, trả về các Chunks mặc định để LLM Gemini có bối cảnh tư vấn
            if (fallbackResults.isEmpty()) {
                String allChunksSql = "SELECT id, tour_id, chunk_type, content, metadata, 0.80 AS similarity_score FROM tour_embeddings LIMIT ?";
                fallbackResults = jdbcTemplate.query(
                        allChunksSql,
                        (rs, rowNum) -> RetrievedChunkDto.builder()
                                .id(UUID.fromString(rs.getString("id")))
                                .tourId(UUID.fromString(rs.getString("tour_id")))
                                .chunkType(ChunkType.valueOf(rs.getString("chunk_type")))
                                .content(rs.getString("content"))
                                .metadata(rs.getString("metadata"))
                                .similarityScore(rs.getDouble("similarity_score"))
                                .build(),
                        limit
                );
            }
            return fallbackResults;
        }
    }
}
