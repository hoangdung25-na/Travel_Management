package com.travel.ai.service.impl;

import com.travel.ai.constant.AiLexiconConstants;
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
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourRetrievalServiceImpl implements TourRetrievalService {

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate jdbcTemplate;



    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ\\s]");

    @Override
    public List<RetrievedChunkDto> retrieveRelevantChunks(AiQueryRequest request) {
        log.info("Bắt đầu PIPELINE 2: Retrieval cho câu hỏi: '{}', destination: '{}', maxBudget: {}",
                request.getQueryText(), request.getDestination(), request.getMaxBudget());
        int limit = (request.getTopK() != null && request.getTopK() > 0) ? request.getTopK() : 5;

        // Trích xuất địa danh / điểm đến ưu tiên từ request và từ câu query
        String targetDestination = extractTargetDestination(request);
        log.info("Trích xuất điểm đến mục tiêu: '{}'", targetDestination);

        try {
            // 1. Mã hóa Yêu cầu Người dùng (Query Embedding) -> Vector 768 chiều
            float[] queryVector = embeddingModel.embed(request.getQueryText());
            String vectorStr = Arrays.toString(queryVector);

            Double maxBudgetVal = request.getMaxBudget() != null ? request.getMaxBudget().doubleValue() : null;

            // Hybrid Search kết hợp pgvector Cosine Distance + Boost điểm đến / tỉnh thành
            String destFilter = "%" + (targetDestination != null ? targetDestination.trim().toLowerCase() : "") + "%";
            boolean hasDest = targetDestination != null && !targetDestination.isBlank();

            String sql;
            Object[] params;

            if (maxBudgetVal != null) {
                sql = """
                    SELECT id, tour_id, chunk_type, content, metadata,
                           (1 - (embedding <=> ?::vector)) AS similarity_score
                    FROM tour_embeddings
                    WHERE embedding IS NOT NULL
                      AND (metadata->>'price')::numeric <= ?
                    ORDER BY
                      CASE
                        WHEN ? = TRUE AND (LOWER(content) LIKE ? OR LOWER(COALESCE(metadata->>'destination', '')) LIKE ?) THEN 0
                        ELSE 1
                      END,
                      embedding <=> ?::vector ASC
                    LIMIT ?
                    """;
                params = new Object[]{ vectorStr, maxBudgetVal, hasDest, destFilter, destFilter, vectorStr, limit };
            } else {
                sql = """
                    SELECT id, tour_id, chunk_type, content, metadata,
                           (1 - (embedding <=> ?::vector)) AS similarity_score
                    FROM tour_embeddings
                    WHERE embedding IS NOT NULL
                    ORDER BY
                      CASE
                        WHEN ? = TRUE AND (LOWER(content) LIKE ? OR LOWER(COALESCE(metadata->>'destination', '')) LIKE ?) THEN 0
                        ELSE 1
                      END,
                      embedding <=> ?::vector ASC
                    LIMIT ?
                    """;
                params = new Object[]{ vectorStr, hasDest, destFilter, destFilter, vectorStr, limit };
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

            log.info("Tìm thấy {} Chunks ngữ cảnh phù hợp từ pgvector (Hybrid Boost)", results.size());
            if (!results.isEmpty()) {
                return results;
            }
        } catch (Exception e) {
            log.warn("Không thể dùng pgvector (hoặc chưa cấu hình Embedding API): {}. Chuyển sang tìm kiếm SQL thông minh.", e.getMessage());
        }

        // 2. TÌM KIẾM FALLBACK SQL THÔNG MINH THEO ĐIỂM ĐẾN / TỈNH THÀNH
        return executeSmartSqlFallback(request, targetDestination, limit);
    }

    /**
     * Tìm kiếm Fallback SQL theo nhiều tầng (Multi-tier Smart Matching):
     * Tầng 1: Khớp điểm đến / tỉnh thành mục tiêu (tránh bị nhiễu bởi chữ 'tour')
     * Tầng 2: Khớp các từ khóa địa danh con (ví dụ: 'Cửa Lò', 'Bà Nà', 'Hội An')
     * Tầng 3: Khớp metadata tổng quan theo ngân sách
     */
    private List<RetrievedChunkDto> executeSmartSqlFallback(AiQueryRequest request, String targetDestination, int limit) {
        Double maxBudgetVal = request.getMaxBudget() != null ? request.getMaxBudget().doubleValue() : null;
        Set<UUID> collectedTourIds = new HashSet<>();
        List<RetrievedChunkDto> results = new ArrayList<>();

        // TẦNG 1: Tìm theo địa danh / tỉnh thành mục tiêu
        if (targetDestination != null && !targetDestination.isBlank()) {
            String destPattern = "%" + targetDestination.trim().toLowerCase() + "%";
            String tier1Sql = """
                SELECT id, tour_id, chunk_type, content, metadata, 0.95 AS similarity_score
                FROM tour_embeddings
                WHERE (LOWER(content) LIKE ? OR LOWER(COALESCE(metadata->>'destination', '')) LIKE ?)
                  AND (?::numeric IS NULL OR (metadata->>'price')::numeric <= ?::numeric)
                ORDER BY CASE WHEN chunk_type = 'METADATA' THEN 0 ELSE 1 END, created_at DESC
                LIMIT ?
                """;

            List<RetrievedChunkDto> tier1Results = jdbcTemplate.query(
                    tier1Sql,
                    (rs, rowNum) -> mapRowToDto(rs),
                    destPattern, destPattern, maxBudgetVal, maxBudgetVal, limit
            );

            for (RetrievedChunkDto item : tier1Results) {
                if (collectedTourIds.add(item.getTourId())) {
                    results.add(item);
                }
            }
        }

        // TẦNG 2: Tách các từ khóa địa danh con (loại bỏ stop-words như 'tour', 'du lịch')
        if (results.size() < limit) {
            List<String> keywords = extractLocationKeywords(request.getQueryText());
            for (String kw : keywords) {
                if (results.size() >= limit) break;
                String kwPattern = "%" + kw.toLowerCase() + "%";
                String tier2Sql = """
                    SELECT id, tour_id, chunk_type, content, metadata, 0.90 AS similarity_score
                    FROM tour_embeddings
                    WHERE (LOWER(content) LIKE ? OR LOWER(COALESCE(metadata->>'destination', '')) LIKE ?)
                      AND (?::numeric IS NULL OR (metadata->>'price')::numeric <= ?::numeric)
                    ORDER BY CASE WHEN chunk_type = 'METADATA' THEN 0 ELSE 1 END
                    LIMIT ?
                    """;

                List<RetrievedChunkDto> kwMatches = jdbcTemplate.query(
                        tier2Sql,
                        (rs, rowNum) -> mapRowToDto(rs),
                        kwPattern, kwPattern, maxBudgetVal, maxBudgetVal, limit - results.size()
                );

                for (RetrievedChunkDto item : kwMatches) {
                    if (collectedTourIds.add(item.getTourId())) {
                        results.add(item);
                    }
                }
            }
        }

        // TẦNG 3: Nếu vẫn chưa có kết quả (ví dụ query chung chung 'tôi muốn đi du lịch hè'), lấy các Tour nổi bật phù hợp ngân sách
        if (results.isEmpty()) {
            String tier3Sql = """
                SELECT id, tour_id, chunk_type, content, metadata, 0.80 AS similarity_score
                FROM tour_embeddings
                WHERE (?::numeric IS NULL OR (metadata->>'price')::numeric <= ?::numeric)
                ORDER BY CASE WHEN chunk_type = 'METADATA' THEN 0 ELSE 1 END, created_at DESC
                LIMIT ?
                """;

            results = jdbcTemplate.query(
                    tier3Sql,
                    (rs, rowNum) -> mapRowToDto(rs),
                    maxBudgetVal, maxBudgetVal, limit
            );
        }

        return results;
    }

    /**
     * Bóc tách địa danh / tỉnh thành từ request
     */
    private String extractTargetDestination(AiQueryRequest request) {
        if (request.getDestination() != null && !request.getDestination().isBlank()) {
            return request.getDestination().trim();
        }

        String text = request.getQueryText();
        if (text == null || text.isBlank()) {
            return null;
        }

        // Bỏ các từ tiền tố 'tour', 'du lịch', 'đi', 'tìm tour'... để lấy cụm từ khóa địa danh chính
        String cleaned = text.trim();
        String lower = cleaned.toLowerCase();

        // Xóa các tiền tố tìm kiếm phổ biến
        String[] prefixes = {"tìm tour đi", "tìm tour", "tour du lịch", "tour đi", "tour", "du lịch đi", "du lịch"};
        for (String p : prefixes) {
            if (lower.startsWith(p)) {
                cleaned = cleaned.substring(p.length()).trim();
                break;
            }
        }

        // Xóa bớt thông tin số ngày hoặc giá ở cuối câu (ví dụ '3 ngày 2 đêm', '3n2đ', 'giá rẻ')
        cleaned = cleaned.replaceAll("(?i)\\b(\\d+\\s*(ngày|đêm|n\\d+đ)|giá\\s*rẻ|trọn\\s*gói|tiết\\s*kiệm)\\b.*", "").trim();

        cleaned = CLEAN_PATTERN.matcher(cleaned).replaceAll("").trim();
        return cleaned.length() >= 2 ? cleaned : null;
    }

    /**
     * Trích xuất các từ khóa có nghĩa, loại bỏ hoàn toàn các từ rác / từ dừng như 'tour', 'du lịch'
     */
    private List<String> extractLocationKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String cleaned = CLEAN_PATTERN.matcher(text).replaceAll(" ");
        String[] words = cleaned.trim().split("\\s+");

        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            String w = word.trim().toLowerCase();
            if (w.length() >= 2 && !AiLexiconConstants.isStopWord(w)) {
                keywords.add(word.trim());
            }
        }
        return keywords;
    }

    private RetrievedChunkDto mapRowToDto(java.sql.ResultSet rs) throws java.sql.SQLException {
        String tourIdStr = rs.getString("tour_id");
        UUID tourId = (tourIdStr != null && !tourIdStr.isBlank()) ? UUID.fromString(tourIdStr) : null;

        return RetrievedChunkDto.builder()
                .id(UUID.fromString(rs.getString("id")))
                .tourId(tourId)
                .chunkType(ChunkType.valueOf(rs.getString("chunk_type")))
                .content(rs.getString("content"))
                .metadata(rs.getString("metadata"))
                .similarityScore(rs.getDouble("similarity_score"))
                .build();
    }

    @Override
    public List<RetrievedChunkDto> retrieveKnowledgeChunks(String queryText, int limit) {
        if (queryText == null || queryText.isBlank()) {
            return Collections.emptyList();
        }
        int effectiveLimit = limit > 0 ? limit : 4;

        // 1. Thử tìm kiếm Vector bằng pgvector Cosine Distance
        try {
            float[] queryVector = embeddingModel.embed(queryText);
            String vectorStr = Arrays.toString(queryVector);

            String sql = """
                SELECT id, tour_id, chunk_type, content, metadata,
                       (1 - (embedding <=> ?::vector)) AS similarity_score
                FROM tour_embeddings
                WHERE embedding IS NOT NULL
                ORDER BY embedding <=> ?::vector ASC
                LIMIT ?
                """;

            List<RetrievedChunkDto> vectorResults = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> mapRowToDto(rs),
                    vectorStr, vectorStr, effectiveLimit
            );

            if (!vectorResults.isEmpty()) {
                double topScore = vectorResults.get(0).getSimilarityScore() != null ? vectorResults.get(0).getSimilarityScore() : 0.0;
                // Nếu độ tương đồng cao nhất dưới 0.60, câu hỏi hoàn toàn xa lạ với cơ sở tri thức du lịch
                if (topScore < 0.60) {
                    log.info("pgvector best similarity ({}) < 0.60 cho câu: '{}'. Xác định câu hỏi ngoài phạm vi tri thức.", topScore, queryText);
                    return Collections.emptyList();
                }
                log.info("Tìm thấy {} chunks tri thức từ pgvector cho câu chat: '{}' (Similarity: {})", vectorResults.size(), queryText, topScore);
                return vectorResults;
            }
        } catch (Exception e) {
            log.warn("Không thể dùng pgvector cho câu chat: {}. Chuyển sang tìm kiếm Full-Text SQL.", e.getMessage());
        }

        // 2. Tìm kiếm SQL Fallback thuần túy từ CSDL
        return executeKnowledgeSqlFallback(queryText, effectiveLimit);
    }

    private List<RetrievedChunkDto> executeKnowledgeSqlFallback(String queryText, int limit) {
        if (queryText == null || queryText.isBlank()) {
            return Collections.emptyList();
        }

        Set<UUID> collectedIds = new HashSet<>();
        List<RetrievedChunkDto> results = new ArrayList<>();

        // 1. Tìm kiếm cụm từ người dùng hỏi trực tiếp trong content hoặc metadata của CSDL
        String fullPattern = "%" + queryText.trim().toLowerCase() + "%";
        String exactSql = """
            SELECT id, tour_id, chunk_type, content, metadata, 0.95 AS similarity_score
            FROM tour_embeddings
            WHERE LOWER(content) LIKE ? OR LOWER(metadata::text) LIKE ?
            ORDER BY created_at DESC
            LIMIT ?
            """;

        List<RetrievedChunkDto> exactMatches = jdbcTemplate.query(
                exactSql,
                (rs, rowNum) -> mapRowToDto(rs),
                fullPattern, fullPattern, limit
        );
        for (RetrievedChunkDto m : exactMatches) {
            if (collectedIds.add(m.getId())) {
                results.add(m);
            }
        }

        // 2. Tách các từ khóa có nghĩa để tìm kiếm phủ trong CSDL
        List<String> keywords = extractLocationKeywords(queryText);
        for (String kw : keywords) {
            if (results.size() >= limit) break;
            String kwPattern = "%" + kw.toLowerCase() + "%";
            String kwSql = """
                SELECT id, tour_id, chunk_type, content, metadata, 0.85 AS similarity_score
                FROM tour_embeddings
                WHERE LOWER(content) LIKE ? OR LOWER(metadata::text) LIKE ?
                ORDER BY created_at DESC
                LIMIT ?
                """;

            List<RetrievedChunkDto> matches = jdbcTemplate.query(
                    kwSql,
                    (rs, rowNum) -> mapRowToDto(rs),
                    kwPattern, kwPattern, limit - results.size()
            );

            for (RetrievedChunkDto m : matches) {
                if (collectedIds.add(m.getId())) {
                    results.add(m);
                }
            }
        }

        // 3. Dự phòng nếu câu hỏi hoàn toàn không khớp từ nào, lấy các Tour nổi bật trong CSDL
        if (results.isEmpty()) {
            String defaultSql = """
                SELECT id, tour_id, chunk_type, content, metadata, 0.70 AS similarity_score
                FROM tour_embeddings
                WHERE chunk_type = 'METADATA'
                ORDER BY created_at DESC
                LIMIT ?
                """;
            results.addAll(jdbcTemplate.query(
                    defaultSql,
                    (rs, rowNum) -> mapRowToDto(rs),
                    limit
            ));
        }

        return results;
    }
}

