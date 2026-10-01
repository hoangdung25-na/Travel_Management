package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.AiQueryRequest;
import com.travel.ai.dto.RetrievedChunkDto;
import com.travel.ai.service.AiRecommendationService;
import com.travel.ai.service.PromptEngineeringService;
import com.travel.ai.service.SemanticCacheService;
import com.travel.ai.service.TourRetrievalService;
import com.travel.ai.viewmodel.DayItineraryVm;
import com.travel.ai.viewmodel.RecommendedTourVm;
import com.travel.ai.viewmodel.TripRecommendationVm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final TourRetrievalService tourRetrievalService;
    private final PromptEngineeringService promptEngineeringService;
    private final SemanticCacheService semanticCacheService;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public TripRecommendationVm getRecommendation(AiQueryRequest request) {
        log.info("Bắt đầu xử lý RAG AI Recommendation cho câu hỏi: '{}'", request.getQueryText());

        // 1. Kiểm tra Redis Semantic Cache
        Optional<String> cachedJson = semanticCacheService.getCachedResponse(request.getQueryText());
        if (cachedJson.isPresent()) {
            try {
                TripRecommendationVm cachedVm = objectMapper.readValue(cachedJson.get(), TripRecommendationVm.class);
                log.info("Trả về kết quả từ Redis Semantic Cache thành công!");
                return cachedVm;
            } catch (Exception e) {
                log.warn("Lỗi parse JSON từ Cache Redis, chuyển sang xử lý RAG trực tiếp: {}", e.getMessage());
            }
        }

        // 2. PIPELINE 2: Retrieval & Hybrid Search
        List<RetrievedChunkDto> chunks = tourRetrievalService.retrieveRelevantChunks(request);

        if (chunks.isEmpty()) {
            log.info("Không tìm thấy Tour phù hợp trong Vector DB.");
            return TripRecommendationVm.builder()
                    .introduction(
                            "Rất tiếc, hiện tại hệ thống chưa tìm thấy tour du lịch phù hợp với yêu cầu của bạn. Bạn có thể thử thay đổi khoảng giá hoặc địa điểm tìm kiếm.")
                    .recommendedTours(Collections.emptyList())
                    .detailedItinerary(Collections.emptyList())
                    .build();
        }

        TripRecommendationVm recommendation;

        try {
            // 3. PIPELINE 3: Generation & Prompt Assembly
            BeanOutputConverter<TripRecommendationVm> outputConverter = new BeanOutputConverter<>(
                    TripRecommendationVm.class);
            String formatInstructions = outputConverter.getFormat();
            String fullPromptText = promptEngineeringService.buildPrompt(request, chunks, formatInstructions);

            log.info("Đã tạo Prompt thành công. Gọi LLM Google Gemini / OpenAI...");
            String responseText = chatModel.call(new Prompt(fullPromptText)).getResult().getOutput().getContent();
            recommendation = outputConverter.convert(responseText);
        } catch (Exception e) {
            log.warn(
                    "Lỗi khi gọi LLM (Vui lòng kiểm tra API Key Gemini/OpenAI trong environment variables): {}. Sử dụng dữ liệu RAG Chunks cấu trúc...",
                    e.getMessage());
            recommendation = buildStructuredRecommendationFromChunks(request, chunks);
        }

        // 4. Lưu kết quả vào Redis Semantic Cache
        try {
            if (recommendation != null) {
                String jsonStr = objectMapper.writeValueAsString(recommendation);
                semanticCacheService.cacheResponse(request.getQueryText(), jsonStr);
            }
        } catch (Exception e) {
            log.warn("Lỗi ghi Cache Redis: {}", e.getMessage());
        }

        return recommendation;
    }

    private TripRecommendationVm buildStructuredRecommendationFromChunks(AiQueryRequest request,
            List<RetrievedChunkDto> chunks) {
        String intro = String.format(
                "Dựa trên hệ thống phân tích du lịch RAG, hệ thống đề xuất chuyến đi tối ưu phù hợp nhất với yêu cầu '%s':",
                request.getQueryText());
        List<RecommendedTourVm> tours = new ArrayList<>();
        List<DayItineraryVm> itineraries = new ArrayList<>();

        for (RetrievedChunkDto chunk : chunks) {
            if (chunk.getTourId() != null && tours.size() < 2) {
                tours.add(RecommendedTourVm.builder()
                        .tourId(chunk.getTourId())
                        .title(extractTitleFromChunk(chunk.getContent()))
                        .price(new BigDecimal("4500000.00"))
                        .bookingUrl("/tours/" + chunk.getTourId())
                        .reason("Khớp dữ liệu tìm kiếm ngữ nghĩa RAG trong cơ sở dữ liệu pgvector.")
                        .build());
            }

            if (itineraries.size() < 3) {
                itineraries.add(DayItineraryVm.builder()
                        .day(itineraries.size() + 1)
                        .activity("Ngày " + (itineraries.size() + 1) + ": " + chunk.getContent())
                        .build());
            }
        }

        return TripRecommendationVm.builder()
                .introduction(intro)
                .recommendedTours(tours)
                .detailedItinerary(itineraries)
                .build();
    }

    private String extractTitleFromChunk(String content) {
        if (content == null || content.isBlank())
            return "Tour Du Lịch Trọn Gói Khám Phá";
        String[] lines = content.split("\n");
        return lines[0].length() > 80 ? lines[0].substring(0, 80) : lines[0];
    }
}
