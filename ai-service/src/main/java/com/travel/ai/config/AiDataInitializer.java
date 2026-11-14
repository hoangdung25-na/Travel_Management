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
    @org.springframework.context.annotation.Lazy
    private final com.travel.ai.service.TourSyncService tourSyncService;
    private final com.travel.ai.service.CompanyPolicyService companyPolicyService;

    @Override
    public void run(String... args) {
        try {
            try {
                jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
                jdbcTemplate.execute("ALTER TABLE tour_embeddings ADD COLUMN IF NOT EXISTS embedding vector(768)");
                jdbcTemplate.execute("ALTER TABLE tour_embeddings DROP CONSTRAINT IF EXISTS tour_embeddings_chunk_type_check");
            } catch (Exception ex) {
                log.warn("Cảnh báo khi kiểm tra/tạo cột embedding hoặc constraint: {}", ex.getMessage());
            }

            // 1. Tự động kiểm tra và nạp Tri thức Chính sách Công ty
            try {
                Integer policyCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM tour_embeddings WHERE chunk_type = 'POLICY'", Integer.class);
                if (policyCount == null || policyCount == 0) {
                    log.info("Chưa có chính sách công ty trong Vector DB. Đang tự động nạp chính sách...");
                    companyPolicyService.initCompanyPolicies();
                } else {
                    log.info("Đã có {} bản ghi chính sách công ty trong Vector DB.", policyCount);
                }
            } catch (Exception ex) {
                log.warn("Cảnh báo khi nạp chính sách công ty: {}", ex.getMessage());
            }

            // 2. Tự động đồng bộ Tour thật nếu bảng chưa có tour nào
            Integer tourCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tour_embeddings WHERE chunk_type != 'POLICY'", Integer.class);
            if (tourCount == null || tourCount == 0) {
                log.info("Chưa có Tour thật trong Vector DB. Đang tự động đồng bộ từ Tour Service...");
                try {
                    int synced = tourSyncService.syncAllToursFromTourService();
                    log.info("Đã tự động đồng bộ thành công {} tour thật từ Tour Service khi khởi động!", synced);
                } catch (Exception ex) {
                    log.warn("Chưa thể kết nối đến Tour Service để đồng bộ tự động lúc này: {}. Bạn có thể bấm nút đồng bộ trên giao diện sau.", ex.getMessage());
                }
            } else {
                log.info("Bảng tour_embeddings đã có {} Chunks Tour du lịch.", tourCount);
            }
        } catch (Exception e) {
            log.warn("Cảnh báo khi chạy AiDataInitializer: {}", e.getMessage(), e);
        }
    }
}
