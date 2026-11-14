package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.constant.AiLexiconConstants;
import com.travel.ai.constant.ChunkType;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationServiceImpl implements AiRecommendationService {

    private final TourRetrievalService tourRetrievalService;
    private final PromptEngineeringService promptEngineeringService;
    private final SemanticCacheService semanticCacheService;
    private final com.travel.ai.service.ConversationMemoryService conversationMemoryService;
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
                    .introduction("Rất tiếc, hiện tại hệ thống chưa tìm thấy tour du lịch phù hợp với yêu cầu của bạn. Bạn có thể thử thay đổi khoảng giá hoặc địa điểm tìm kiếm.")
                    .recommendedTours(Collections.emptyList())
                    .detailedItinerary(Collections.emptyList())
                    .build();
        }

        TripRecommendationVm recommendation;

        try {
            // 3. PIPELINE 3: Generation & Prompt Assembly
            BeanOutputConverter<TripRecommendationVm> outputConverter = new BeanOutputConverter<>(TripRecommendationVm.class);
            String formatInstructions = outputConverter.getFormat();
            String fullPromptText = promptEngineeringService.buildPrompt(request, chunks, formatInstructions);

            CompletableFuture<TripRecommendationVm> llmFuture = CompletableFuture.supplyAsync(() -> {
                log.info("Đã tạo Prompt thành công. Gọi LLM Google Gemini / OpenAI...");
                String responseText = chatModel.call(new Prompt(fullPromptText)).getResult().getOutput().getContent();
                return outputConverter.convert(responseText);
            });
            recommendation = llmFuture.get(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Lỗi hoặc timeout khi gọi LLM: {}. Sử dụng dữ liệu RAG Chunks cấu trúc...", e.getMessage());
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
                "Dựa trên yêu cầu khám phá '%s'%s, hệ thống gợi ý cho bạn các tour du lịch phù hợp nhất:",
                request.getQueryText(),
                request.getMaxBudget() != null ? " với ngân sách dự kiến " + request.getMaxBudget() + " VNĐ" : "");

        Map<UUID, RecommendedTourVm> tourMap = new LinkedHashMap<>();
        List<DayItineraryVm> itineraries = new ArrayList<>();

        for (RetrievedChunkDto chunk : chunks) {
            UUID tourId = chunk.getTourId();
            if (tourId == null) continue;

            if (!tourMap.containsKey(tourId)) {
                String title = extractTitleFromChunk(chunk);
                BigDecimal price = extractPriceFromChunk(chunk);

                tourMap.put(tourId, RecommendedTourVm.builder()
                        .tourId(tourId)
                        .title(title)
                        .reason("Tour phù hợp với địa điểm và ngân sách bạn tìm kiếm.")
                        .price(price)
                        .bookingUrl("/tours/" + tourId)
                        .build());
            }

            if (chunk.getChunkType() == ChunkType.ITINERARY_DAY) {
                itineraries.add(DayItineraryVm.builder()
                        .day(itineraries.size() + 1)
                        .activity(chunk.getContent())
                        .build());
            }
        }

        if (itineraries.isEmpty()) {
            itineraries.add(DayItineraryVm.builder()
                    .day(1)
                    .activity(chunks.get(0).getContent())
                    .build());
        }

        return TripRecommendationVm.builder()
                .introduction(intro)
                .recommendedTours(new ArrayList<>(tourMap.values()))
                .detailedItinerary(itineraries)
                .build();
    }

    private String extractTitleFromChunk(RetrievedChunkDto chunk) {
        if (chunk.getMetadata() != null && !chunk.getMetadata().isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(chunk.getMetadata());
                if (node.has("title") && !node.get("title").isNull()) {
                    return node.get("title").asText();
                }
            } catch (Exception ignored) {}
        }

        String content = chunk.getContent();
        if (content.startsWith("Tour:")) {
            int start = content.indexOf("Tour:") + 5;
            int end = content.indexOf("(", start);
            if (end > start) {
                return content.substring(start, end).trim();
            }
        }

        String[] lines = content.split("\n");
        return lines[0].length() > 80 ? lines[0].substring(0, 80) : lines[0];
    }

    private BigDecimal extractPriceFromChunk(RetrievedChunkDto chunk) {
        if (chunk.getMetadata() != null && !chunk.getMetadata().isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(chunk.getMetadata());
                if (node.has("price") && !node.get("price").isNull()) {
                    return new BigDecimal(node.get("price").asText());
                }
            } catch (Exception ignored) {}
        }

        // Trích xuất giá động từ nội dung chunk nếu metadata chưa có
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)(?:giá|chi phí|tổng|từ)[\\s:]*([0-9.,]+)\\s*(?:vnđ|đ|vnd)?").matcher(chunk.getContent());
            if (m.find()) {
                String cleanPrice = m.group(1).replace(".", "").replace(",", "");
                return new BigDecimal(cleanPrice);
            }
        } catch (Exception ignored) {}

        return BigDecimal.ZERO;
    }

    @Override
    public String chat(String promptText) {
        com.travel.ai.dto.AiChatResponse response = chat(null, promptText);
        return response != null ? response.getReply() : "";
    }

    @Override
    public com.travel.ai.dto.AiChatResponse chat(String conversationId, String promptText) {
        String finalConversationId = (conversationId != null && !conversationId.isBlank()) 
                ? conversationId.trim() 
                : UUID.randomUUID().toString();

        log.info("Xử lý yêu cầu Chatbot AI cho Conversation [{}]: '{}'", finalConversationId, promptText);

        if (promptText == null || promptText.isBlank()) {
            String defaultGreeting = "Xin chào! Tôi là Trợ lý AI Du lịch 24/7 của Travel Platform. Tôi có thể hỗ trợ bạn tìm kiếm tour, giải đáp thủ tục, chính sách hoàn hủy vé hoặc tư vấn kinh nghiệm du lịch tại Việt Nam và quốc tế!";
            return com.travel.ai.dto.AiChatResponse.builder()
                    .conversationId(finalConversationId)
                    .reply(defaultGreeting)
                    .build();
        }

        String queryClean = promptText.trim();
        String lower = queryClean.toLowerCase();

        // 1. Xử lý câu chào hỏi ban đầu (Chỉ khi người dùng đơn thuần gửi lời chào, không kèm câu hỏi)
        boolean isOnlyGreeting = AiLexiconConstants.isGreeting(lower);

        if (isOnlyGreeting) {
            String welcomeMsg = "Dạ xin chào quý khách! 👋 Em là **Trợ lý AI Du lịch 24/7** của Travel Platform.\nEm có thể hỗ trợ quý khách:\n• **Tư vấn và gợi ý tour du lịch** theo ngân sách và điểm đến\n• **Giải đáp chính sách hoàn hủy vé, đặt cọc & thanh toán**\n• **Thủ tục giấy tờ tùy thân (CCCD, Hộ chiếu, Visa)**.\nQuý khách đang muốn đi đâu hoặc cần hỗ trợ gì cứ nhắn em nhé!";
            
            conversationMemoryService.saveMessage(finalConversationId, "user", queryClean);
            conversationMemoryService.saveMessage(finalConversationId, "assistant", welcomeMsg);

            return com.travel.ai.dto.AiChatResponse.builder()
                    .conversationId(finalConversationId)
                    .reply(welcomeMsg)
                    .build();
        }

        // 2. Lấy 4 - 6 tin nhắn gần nhất từ Redis Multi-turn Memory
        List<com.travel.ai.dto.ChatMessageDto> history = conversationMemoryService.getRecentMessages(finalConversationId, 6);

        // 3. Nếu chưa có lịch sử, thử kiểm tra Semantic Cache
        if (history.isEmpty()) {
            try {
                Optional<String> cached = semanticCacheService.getCachedResponse("chat:" + lower);
                if (cached.isPresent()) {
                    log.info("Trả về câu trả lời Chatbot từ Redis Cache siêu tốc!");
                    String cachedReply = cached.get();
                    conversationMemoryService.saveMessage(finalConversationId, "user", queryClean);
                    conversationMemoryService.saveMessage(finalConversationId, "assistant", cachedReply);
                    return com.travel.ai.dto.AiChatResponse.builder()
                            .conversationId(finalConversationId)
                            .reply(cachedReply)
                            .build();
                }
            } catch (Exception e) {
                log.warn("Lỗi kiểm tra Redis Cache cho chatbot: {}", e.getMessage());
            }
        }

        // 4. Mở rộng câu truy vấn nếu có đại từ thay thế (Contextual Query Enrichment)
        String effectiveSearchQuery = enrichSearchQueryWithHistory(queryClean, history);

        // 5. RAG PIPELINE: Truy xuất tri thức thực tế từ CSDL Vector (pgvector & Hybrid SQL)
        List<RetrievedChunkDto> chunks = tourRetrievalService.retrieveKnowledgeChunks(effectiveSearchQuery, 4);

        // 6. DỮ LIỆU THAM KHẢO CHO LLM:
        // Nếu câu hỏi tự nhiên hoặc trừu tượng (như 'mình stress quá', 'chỗ nào chill', 'say sóng thì sao')
        // không khớp trực tiếp tên tour trong vector DB, tự động nạp danh sách tour tiêu biểu làm dữ liệu nền tảng.
        // LLM sẽ tiếp nhận toàn bộ câu hỏi và tự hiểu để tư vấn tour hoặc từ chối nếu hoàn toàn ngoài phạm vi du lịch.
        if (chunks == null || chunks.isEmpty()) {
            log.info("Không có chunks khớp trực tiếp cho '{}'. Tự động nạp dữ liệu tour tiêu biểu làm ngữ cảnh cho LLM.", queryClean);
            chunks = tourRetrievalService.retrieveKnowledgeChunks("", 4);
        }

        final List<RetrievedChunkDto> finalChunks = (chunks != null) ? chunks : Collections.emptyList();

        // 7. Gọi LLM (Gemini / OpenAI) với RAG Chat Prompt từ PromptEngineeringService (kèm Context + History)
        String finalReply = null;
        try {
            CompletableFuture<String> llmFuture = CompletableFuture.supplyAsync(() -> {
                String chatPrompt = promptEngineeringService.buildChatPrompt(queryClean, finalChunks, history);
                Prompt prompt = new Prompt(chatPrompt);
                return chatModel.call(prompt).getResult().getOutput().getContent();
            });

            String aiResponse = llmFuture.get(15, TimeUnit.SECONDS);
            if (aiResponse != null && !aiResponse.isBlank()) {
                finalReply = aiResponse;
                if (history.isEmpty()) {
                    try {
                        semanticCacheService.cacheResponse("chat:" + lower, finalReply);
                    } catch (Exception ignored) {}
                }
            }
        } catch (TimeoutException te) {
            log.warn("Gọi LLM bị quá thời gian 15s, tự động chuyển sang tổng hợp tri thức RAG từ CSDL!");
        } catch (Exception e) {
            log.warn("Không thể gọi LLM bên ngoài ({}): chuyển sang tổng hợp tri thức RAG từ CSDL", e.getMessage());
        }

        // 8. FALLBACK RAG TỰ ĐỘNG TỪ CSDL nếu LLM không trả lời được
        if (finalReply == null || finalReply.isBlank()) {
            finalReply = buildConversationalFallbackFromChunks(finalChunks, queryClean);
            if (history.isEmpty()) {
                try {
                    semanticCacheService.cacheResponse("chat:" + lower, finalReply);
                } catch (Exception ignored) {}
            }
        }

        // 9. LƯU CẢ TIN NHẮN USER VÀ AI VÀO REDIS CONVERSATION MEMORY (TTL 30 phút)
        conversationMemoryService.saveMessage(finalConversationId, "user", queryClean);
        conversationMemoryService.saveMessage(finalConversationId, "assistant", finalReply);

        return com.travel.ai.dto.AiChatResponse.builder()
                .conversationId(finalConversationId)
                .reply(finalReply)
                .build();
    }

    private String enrichSearchQueryWithHistory(String currentQuery, List<com.travel.ai.dto.ChatMessageDto> history) {
        if (history == null || history.isEmpty()) {
            return currentQuery;
        }

        String lower = currentQuery.toLowerCase();
        boolean hasRelativePronoun = AiLexiconConstants.hasRelativePronoun(lower);

        if (hasRelativePronoun || currentQuery.split("\\s+").length <= 6) {
            for (int i = history.size() - 1; i >= 0; i--) {
                com.travel.ai.dto.ChatMessageDto msg = history.get(i);
                if ("user".equalsIgnoreCase(msg.getRole()) && !msg.getContent().equalsIgnoreCase(currentQuery)) {
                    log.info("Phát hiện ngữ cảnh đa lượt: Mở rộng truy vấn từ '{}' kết hợp với câu trước '{}'", currentQuery, msg.getContent());
                    return msg.getContent() + " " + currentQuery;
                }
            }
        }
        return currentQuery;
    }

    /**
     * Tổng hợp câu trả lời tự nhiên từ các Chunks CSDL thật khi LLM bên ngoài bị trễ mạng hoặc hết Quota.
     * HOÀN TOÀN TỰ ĐỘNG THEO DỮ LIỆU CSDL (Data-Driven Routing dựa trên Vector Similarity Score & Chunk Type),
     * TUYỆT ĐỐI KHÔNG CÒN BẤT KỲ DÒNG HARDCODE TỪ KHÓA NÀO.
     */
    private String buildConversationalFallbackFromChunks(List<RetrievedChunkDto> chunks, String userQuery) {
        if (chunks == null || chunks.isEmpty()) {
            return "Dạ cảm ơn bạn đã nhắn tin cho Trợ lý Du lịch! Hiện tại hệ thống chưa tìm thấy thông tin tour hoặc quy định phù hợp với yêu cầu này. Bạn vui lòng liên hệ trực tiếp Tổng đài CSKH 24/7 qua Hotline **1900 6868** hoặc 0909 123 456 để được nhân viên hỗ trợ chi tiết nhé! 📞";
        }

        // 1. Phân loại các Chunks theo nhóm tri thức từ CSDL
        List<RetrievedChunkDto> policyChunks = chunks.stream()
                .filter(c -> c.getChunkType() == ChunkType.POLICY)
                .toList();

        List<RetrievedChunkDto> tourChunks = chunks.stream()
                .filter(c -> c.getChunkType() == ChunkType.METADATA || c.getChunkType() == ChunkType.ITINERARY_DAY)
                .toList();

        // 2. Mẩu tri thức có điểm tương đồng cao nhất do Vector DB xếp hạng (Rank 1)
        RetrievedChunkDto topChunk = chunks.get(0);

        double maxPolicyScore = policyChunks.stream()
                .mapToDouble(c -> c.getSimilarityScore() != null ? c.getSimilarityScore() : 0.0)
                .max().orElse(0.0);

        double maxTourScore = tourChunks.stream()
                .mapToDouble(c -> c.getSimilarityScore() != null ? c.getSimilarityScore() : 0.0)
                .max().orElse(0.0);

        // 3. Quyết định dựa trên độ tương quan toán học từ CSDL (Vector Dominance):
        // Nếu mẩu tri thức đứng đầu từ CSDL là POLICY và điểm số POLICY vượt trội hơn hoặc bằng điểm TOUR
        boolean isPolicyDominant = topChunk.getChunkType() == ChunkType.POLICY 
                && (maxPolicyScore >= maxTourScore || tourChunks.isEmpty());

        if (isPolicyDominant) {
            RetrievedChunkDto bestPolicy = policyChunks.get(0);
            return "📋 **Thông tin từ Hệ thống Quy định & Chính sách Travel Platform:**\n\n" 
                    + bestPolicy.getContent().trim() 
                    + "\n\n👉 Nếu quý khách cần giải đáp thêm, vui lòng liên hệ Tổng đài CSKH: **1900 6868**!";
        }

        // 4. Ngược lại: Ý định tìm Tour du lịch chiếm ưu thế
        if (!tourChunks.isEmpty()) {
            StringBuilder reply = new StringBuilder();
            reply.append("🌟 **Dưới đây là các Tour du lịch phù hợp nhất tìm thấy từ hệ thống dành cho bạn:**\n\n");

            Set<UUID> addedTours = new HashSet<>();
            int index = 1;

            // Thứ tự hiển thị giữ nguyên theo xếp hạng độ tương đồng giảm dần từ pgvector
            for (RetrievedChunkDto chunk : tourChunks) {
                UUID tourId = chunk.getTourId();
                if (tourId != null && addedTours.add(tourId)) {
                    String title = extractTitleFromChunk(chunk);
                    BigDecimal price = extractPriceFromChunk(chunk);
                    String tourLink = "/tours/" + tourId.toString();

                    reply.append(String.format("%d. **[%s](%s)**\n", index++, title, tourLink));
                    if (price.compareTo(BigDecimal.ZERO) > 0) {
                        reply.append(String.format("   • **Giá tham khảo:** %,.0f VNĐ/người\n", price.doubleValue()));
                    }
                    reply.append(String.format("   • **Chi tiết tour:** %s\n", chunk.getContent()));
                    reply.append(String.format("   👉 *Nhấn vào [Xem chi tiết và đặt chỗ](%s)*\n\n", tourLink));
                }
            }

            if (!addedTours.isEmpty()) {
                reply.append("Quý khách có thể bấm trực tiếp vào tên từng tour để xem chi tiết lịch trình và chọn ngày khởi hành nhé! Chúc quý khách có một chuyến đi thật tuyệt vời! ✈️🏖️");
                return reply.toString();
            }
        }

        // Dự phòng an toàn: nếu chỉ có policy chunks
        if (!policyChunks.isEmpty()) {
            return "📋 **Thông tin từ Hệ thống Quy định & Chính sách Travel Platform:**\n\n" 
                    + policyChunks.get(0).getContent().trim() 
                    + "\n\n👉 Nếu quý khách cần giải đáp thêm, vui lòng liên hệ Tổng đài CSKH: **1900 6868**!";
        }

        return topChunk.getContent();
    }
}
