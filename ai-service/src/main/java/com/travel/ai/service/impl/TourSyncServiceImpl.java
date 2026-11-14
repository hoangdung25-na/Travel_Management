package com.travel.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.dto.ItineraryItemDto;
import com.travel.ai.dto.event.TourUpdatedEvent;
import com.travel.ai.service.TourIngestionService;
import com.travel.ai.service.TourSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourSyncServiceImpl implements TourSyncService {

    private final TourIngestionService tourIngestionService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;
    private final com.travel.ai.service.CompanyPolicyService companyPolicyService;

    @Value("${app.services.tour-service-url:http://localhost:8082}")
    private String tourServiceUrl;

    @Override
    public int syncAllToursFromTourService() {
        log.info("Bắt đầu quy trình đồng bộ Tour thật từ Tour Service (URL: {})...", tourServiceUrl);

        // 1. Đảm bảo Tri thức Chính sách Công ty luôn đầy đủ
        try {
            companyPolicyService.initCompanyPolicies();
        } catch (Exception e) {
            log.warn("Cảnh báo khi nạp chính sách trong sync: {}", e.getMessage());
        }

        // 2. Xóa các bản ghi Tour cũ trong tour_embeddings (giữ lại các bản ghi POLICY)
        try {
            jdbcTemplate.update("DELETE FROM tour_embeddings WHERE chunk_type != 'POLICY'");
            log.info("Đã xóa sạch các bản ghi Tour cũ trong bảng tour_embeddings.");
        } catch (Exception e) {
            log.warn("Cảnh báo khi dọn dẹp tour_embeddings: {}", e.getMessage());
        }

        // 2. Xóa sạch Redis Semantic Cache cũ để không bị vướng các link tour giả lập 5555..., 2222...
        try {
            java.util.Set<String> keys = redisTemplate.keys("ai:cache:rag:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Đã xóa sạch {} keys trong Redis Semantic Cache.", keys.size());
            }
        } catch (Exception e) {
            log.warn("Cảnh báo khi xóa Redis Semantic Cache: {}", e.getMessage());
        }

        // 3. Gọi sang tour-service để lấy danh sách các Tour thật
        RestClient restClient = RestClient.builder().baseUrl(tourServiceUrl).build();
        JsonNode listResponse;
        try {
            String jsonStr = restClient.get()
                    .uri("/api/v1/tours?page=0&size=100")
                    .retrieve()
                    .body(String.class);

            listResponse = objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            log.error("Không thể kết nối đến Tour Service tại '{}': {}", tourServiceUrl, e.getMessage());
            throw new RuntimeException("Không thể kết nối đến Tour Service để đồng bộ: " + e.getMessage());
        }

        if (listResponse == null || !listResponse.has("data")) {
            log.warn("Không nhận được dữ liệu hợp lệ từ Tour Service.");
            return 0;
        }

        JsonNode contentArray = listResponse.path("data").path("content");
        if (!contentArray.isArray() || contentArray.isEmpty()) {
            log.info("Tour Service hiện chưa có tour nào để đồng bộ.");
            return 0;
        }

        int syncedCount = 0;

        // 4. Với mỗi Tour thật, lấy thông tin chi tiết và nạp vào Vector DB RAG của AI
        for (JsonNode item : contentArray) {
            String tourIdStr = item.path("tourId").asText();
            if (tourIdStr == null || tourIdStr.isBlank()) {
                continue;
            }

            try {
                String detailJsonStr = restClient.get()
                        .uri("/api/v1/tours/" + tourIdStr)
                        .retrieve()
                        .body(String.class);

                JsonNode detailResponse = objectMapper.readTree(detailJsonStr);
                if (detailResponse == null || !detailResponse.has("data")) {
                    continue;
                }

                JsonNode data = detailResponse.path("data");
                TourUpdatedEvent event = convertDetailJsonToEvent(data);

                tourIngestionService.processTourUpdate(event);
                syncedCount++;
                log.info("Đã đồng bộ thành công Tour thật ID: {} - Title: '{}'", event.getTourId(), event.getTitle());

            } catch (Exception ex) {
                log.error("Lỗi khi đồng bộ Tour ID {}: {}", tourIdStr, ex.getMessage(), ex);
            }
        }

        log.info("HOÀN TẤT ĐỒNG BỘ: Đã nạp thành công {} tour thật vào Vector DB!", syncedCount);
        return syncedCount;
    }

    private TourUpdatedEvent convertDetailJsonToEvent(JsonNode data) {
        UUID tourId = UUID.fromString(data.path("tourId").asText());
        String code = data.path("code").asText("N/A");
        String title = data.path("title").asText("Tour Du Lịch");
        String description = data.path("description").asText("");

        // Trích xuất điểm đến và tỉnh thành
        List<String> destList = new ArrayList<>();
        JsonNode destinationsNode = data.path("destinations");
        if (destinationsNode.isArray()) {
            for (JsonNode d : destinationsNode) {
                String name = d.path("name").asText("");
                String city = d.path("city").asText("");
                if (!city.isBlank() && !name.contains(city)) {
                    destList.add(name + " (" + city + ")");
                } else if (!name.isBlank()) {
                    destList.add(name);
                } else if (!city.isBlank()) {
                    destList.add(city);
                }
            }
        }
        if (destList.isEmpty()) {
            String combined = (title + " " + description).toLowerCase();
            if (combined.contains("nghệ an") || combined.contains("cửa lò")) destList.add("Nghệ An");
            if (combined.contains("đà nẵng") || combined.contains("hội an") || combined.contains("bà nà")) destList.add("Đà Nẵng");
            if (combined.contains("phú quốc") || combined.contains("hòn thơm")) destList.add("Phú Quốc");
            if (combined.contains("sa pa") || combined.contains("sapa") || combined.contains("fansipan")) destList.add("Sa Pa");
            if (combined.contains("hà giang") || combined.contains("mã pí lèng")) destList.add("Hà Giang");
            if (combined.contains("nha trang")) destList.add("Nha Trang");
            if (combined.contains("hạ long") || combined.contains("quảng ninh")) destList.add("Hạ Long");
        }
        String destinationStr = destList.isEmpty() ? "Việt Nam" : String.join(", ", destList);

        // Trích xuất mức giá nhỏ nhất từ schedules
        BigDecimal minPrice = null;
        JsonNode schedulesNode = data.path("schedules");
        if (schedulesNode.isArray()) {
            for (JsonNode s : schedulesNode) {
                if (s.has("priceAdult") && !s.path("priceAdult").isNull()) {
                    BigDecimal price = new BigDecimal(s.path("priceAdult").asText());
                    if (minPrice == null || price.compareTo(minPrice) < 0) {
                        minPrice = price;
                    }
                }
            }
        }
        if (minPrice == null) {
            minPrice = new BigDecimal("4500000");
        }

        // Trích xuất lịch trình các ngày
        List<ItineraryItemDto> itineraries = new ArrayList<>();
        JsonNode itinNode = data.path("itineraries");
        if (itinNode.isArray()) {
            for (JsonNode it : itinNode) {
                itineraries.add(ItineraryItemDto.builder()
                        .dayNumber(it.path("dayNumber").asInt(1))
                        .title(it.path("title").asText(""))
                        .description(it.path("content").asText(""))
                        .activity("Tham quan, trải nghiệm")
                        .build());
            }
        }

        return TourUpdatedEvent.builder()
                .tourId(tourId)
                .code(code)
                .title(title)
                .description(description)
                .destination(destinationStr)
                .minPrice(minPrice)
                .durationDays(itineraries.isEmpty() ? 3 : itineraries.size())
                .suitableFor("Gia đình, bạn bè, khách du lịch")
                .itineraries(itineraries)
                .build();
    }
}
