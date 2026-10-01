package com.travel.ai.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiDataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    @Override
    public void run(String... args) {
        try {
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                jdbcTemplate.execute("ALTER TABLE tour_embeddings ADD COLUMN IF NOT EXISTS embedding vector(768)");
            } catch (Exception ex) {
                log.warn("Cảnh báo khi kiểm tra/tạo cột embedding: {}", ex.getMessage());
            }

            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tour_embeddings WHERE embedding IS NOT NULL", Integer.class);
            if (count != null && count >= 5) {
                log.info("Bảng tour_embeddings đã có {} Chunks dữ liệu RAG kèm Vector Embedding. Bỏ qua khởi tạo mẫu.", count);
                return;
            }

            log.info("Đang tự động nạp dữ liệu RAG mẫu kèm Vector Embedding cho các Tour...");
            jdbcTemplate.update("TRUNCATE TABLE tour_embeddings");

            insertSampleTour(
                    "11111111-1111-1111-1111-111111111111",
                    "Tour Khám Phá Đà Nẵng - Ba Na Hills - Phố Cổ Hội An 3N2Đ. Hành trình trải nghiệm tuyệt vời miền Trung: Chinh phục đỉnh Bà Nà Hills, check-in Cầu Vàng huyền thoại, ngắm hoàng hôn Phố cổ Hội An rực rỡ đèn lồng và thưởng thức ẩm thực đặc sản cao cấp. Giá vé 4.500.000đ cho 2 người.",
                    "{\"price\": 4500000, \"destination\": \"Đà Nẵng\"}"
            );

            insertSampleTour(
                    "22222222-2222-2222-2222-222222222222",
                    "Tour Thiên Đường Nắng Vàng Phú Quốc 4N3Đ - VinWonders & Safari. Khám phá đảo ngọc Phú Quốc với tour trọn gói: Trải nghiệm cáp treo Hòn Thơm vượt biển dài nhất thế giới, lặn ngắm san hô 4 đảo, vui chơi không giới hạn VinWonders. Giá vé 6.200.000đ.",
                    "{\"price\": 6200000, \"destination\": \"Phú Quốc\"}"
            );

            insertSampleTour(
                    "33333333-3333-3333-3333-333333333333",
                    "Tour Phượt Hà Giang Loop - Mã Pí Lèng - Chinh Phục Cột Cờ Lũng Cú 3N2Đ cùng bạn bè. Trải nghiệm cung đường đèo Mã Pí Lèng huyền thoại, đi thuyền trên dòng sông Nho Quế xanh ngọc bích, giao lưu văn hóa H'Mông tại Bản Lô Lô Chải. Giá vé 3.850.000đ.",
                    "{\"price\": 3850000, \"destination\": \"Hà Giang\"}"
            );

            insertSampleTour(
                    "44444444-4444-4444-4444-444444444444",
                    "Tour Sa Pa Săn Mây Fansipan - Bản Cát Cát - Moana Sapa 3N2Đ. Săn mây trên Nóc nhà Đông Dương Fansipan ở độ cao 3.143m, dạo bước qua nấc thang bản Cát Cát của người H'Mông và check-in sống ảo tại Moana Sapa. Giá vé 3.290.000đ.",
                    "{\"price\": 3290000, \"destination\": \"Sa Pa\"}"
            );

            insertSampleTour(
                    "55555555-5555-5555-5555-555555555555",
                    "Tour Nghệ An - Biển Cửa Lò - Thăm Quê Bác Kim Liên 2N3Đ (2 ngày 3 đêm). Khám phá vùng đất lịch sử Nghệ An, dâng hương Quê Bác tại làng Sen Kim Liên, nghỉ dưỡng và thưởng thức hải sản tươi sống tại bãi biển Cửa Lò tuyệt đẹp. Giá vé 3.500.000đ.",
                    "{\"price\": 3500000, \"destination\": \"Nghệ An\"}"
            );

            log.info("Đã tự động nạp thành công 5 Chunks Tour mẫu kèm Vector Embedding vào bảng tour_embeddings!");
        } catch (Exception e) {
            log.warn("Cảnh báo khi nạp dữ liệu RAG mẫu: {}", e.getMessage(), e);
        }
    }

    private void insertSampleTour(String tourId, String content, String metadata) {
        String id = UUID.randomUUID().toString();
        float[] vector = null;
        try {
            vector = embeddingModel.embed(content);
        } catch (Exception e) {
            log.warn("Cảnh báo khi gọi Embedding API cho Tour sample [{}]: {}. Vẫn lưu nội dung văn bản vào CSDL.", tourId, e.getMessage());
        }

        String insertSql = "INSERT INTO tour_embeddings (id, tour_id, chunk_type, content, metadata, created_at) VALUES (?::uuid, ?::uuid, 'METADATA', ?, ?::jsonb, NOW())";
        jdbcTemplate.update(insertSql, id, tourId, content, metadata);

        if (vector != null && vector.length > 0) {
            try {
                String updateVectorSql = "UPDATE tour_embeddings SET embedding = ?::vector WHERE id = ?::uuid";
                jdbcTemplate.update(updateVectorSql, Arrays.toString(vector), id);
            } catch (Exception ex) {
                log.warn("Cảnh báo khi cập nhật vector vào pgvector: {}", ex.getMessage());
            }
        }
    }
}
