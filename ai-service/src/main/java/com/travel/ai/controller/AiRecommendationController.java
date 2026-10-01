package com.travel.ai.controller;

import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.service.AiRecommendationService;
import com.travel.ai.viewmodel.TripRecommendationVm;
import com.travel.common.core.dto.ApiResponse;
import com.travel.common.security.context.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller tiếp nhận các yêu cầu tư vấn tour du lịch thông minh từ khách
 * hàng (RAG AI Service)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;
    private final com.travel.ai.config.AiDataInitializer aiDataInitializer;

    /**
     * POST /api/v1/ai/recommendations
     * Tiếp nhận câu hỏi tư vấn, thực hiện tìm kiếm ngữ nghĩa RAG và trả về bài tư
     * vấn JSON cấu trúc TripRecommendationVm
     */
    @PostMapping("/recommendations")
    public ApiResponse<TripRecommendationVm> getRecommendations(@Valid @RequestBody AiQueryRequest request) {
        String userIdStr = getCurrentUserIdString();
        log.info("REST Request tư vấn AI RAG từ User [{}]: '{}'", userIdStr, request.getQueryText());

        TripRecommendationVm recommendation = aiRecommendationService.getRecommendation(request);

        return ApiResponse.ok(recommendation, "Lấy bài tư vấn tour du lịch từ RAG AI thành công");
    }

    /**
     * POST /api/v1/ai/seed-data
     * API kích hoạt nạp lại dữ liệu RAG mẫu vào CSDL (tour_embeddings)
     */
    @PostMapping("/seed-data")
    public ApiResponse<String> seedData() {
        log.info("Yêu cầu nạp lại dữ liệu RAG mẫu thủ công...");
        try {
            aiDataInitializer.run();
            return ApiResponse.ok("Nạp dữ liệu RAG mẫu vào CSDL tour_embeddings thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi nạp dữ liệu RAG mẫu: {}", e.getMessage(), e);
            throw com.travel.common.core.exception.BusinessException.of(
                    com.travel.common.core.exception.ErrorCode.INTERNAL_SERVER_ERROR,
                    "Lỗi khi nạp dữ liệu RAG: " + e.getMessage()
            );
        }
    }

    private String getCurrentUserIdString() {
        try {
            return UserContext.getUserId() != null ? UserContext.getUserId().toString() : "Anonymous";
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}
