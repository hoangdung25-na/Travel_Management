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
    private final com.travel.ai.service.TourSyncService tourSyncService;

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

    private final com.travel.ai.service.ConversationMemoryService conversationMemoryService;

    /**
     * POST /api/v1/ai/chat
     * API trò chuyện trực tiếp 24/7 với Trợ lý AI Du lịch hỗ trợ nhớ ngữ cảnh nhiều lượt (Multi-turn Context)
     */
    @PostMapping("/chat")
    public ApiResponse<com.travel.ai.dto.AiChatResponse> chat(@RequestBody(required = false) com.travel.ai.dto.AiChatRequest request) {
        String prompt = request != null ? request.getEffectiveMessage() : "";
        String conversationId = request != null ? request.getConversationId() : null;

        log.info("REST Request Chatbot AI cho Conversation [{}]: '{}'", conversationId, prompt);
        com.travel.ai.dto.AiChatResponse reply = aiRecommendationService.chat(conversationId, prompt);
        return ApiResponse.ok(reply, "Phản hồi từ AI Chatbot thành công");
    }

    /**
     * DELETE /api/v1/ai/chat/{conversationId}
     * Xóa sạch lịch sử hội thoại trên Redis để bắt đầu phiên mới
     */
    @DeleteMapping("/chat/{conversationId}")
    public ApiResponse<String> clearConversation(@PathVariable String conversationId) {
        log.info("REST Request xóa phiên hội thoại [{}]", conversationId);
        conversationMemoryService.clearConversation(conversationId);
        return ApiResponse.ok("Đã xóa phiên hội thoại thành công", "Xóa phiên thành công");
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

    /**
     * POST /api/v1/ai/sync-from-tour-service
     * API xóa toàn bộ bản ghi giả lập và đồng bộ 100% dữ liệu Tour thật từ tour-service sang AI
     */
    @PostMapping("/sync-from-tour-service")
    public ApiResponse<java.util.Map<String, Object>> syncFromTourService() {
        log.info("REST Request kích hoạt đồng bộ toàn bộ Tour thật từ tour-service sang AI...");
        int count = tourSyncService.syncAllToursFromTourService();
        return ApiResponse.ok(
                java.util.Map.of(
                        "syncedCount", count,
                        "message", "Đã xóa sạch các bản giả lập và đồng bộ thành công " + count + " tour thật từ tour-service vào AI Vector DB!"
                ),
                "Đồng bộ dữ liệu Tour thật thành công"
        );
    }

    private String getCurrentUserIdString() {
        try {
            return UserContext.getUserId() != null ? UserContext.getUserId().toString() : "Anonymous";
        } catch (Exception e) {
            return "Anonymous";
        }
    }
}
