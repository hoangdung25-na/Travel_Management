package com.travel.ai.service.impl;

import com.travel.ai.service.SemanticCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticCacheServiceImpl implements SemanticCacheService {

    private final StringRedisTemplate redisTemplate;
    private static final String CACHE_PREFIX = "ai:cache:rag:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(6);

    @Override
    public Optional<String> getCachedResponse(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return Optional.empty();
        }

        try {
            String cacheKey = buildCacheKey(queryText);
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                log.info("HIT Cache Redis cho câu hỏi: '{}'", queryText);
                return Optional.of(cachedData);
            }
        } catch (Exception e) {
            log.warn("Lỗi đọc Redis Semantic Cache (bỏ qua cache): {}", e.getMessage());
        }

        log.info("MISS Cache Redis cho câu hỏi: '{}'", queryText);
        return Optional.empty();
    }

    @Override
    public void cacheResponse(String queryText, String responseJson) {
        if (queryText == null || queryText.isBlank() || responseJson == null) {
            return;
        }

        try {
            String cacheKey = buildCacheKey(queryText);
            redisTemplate.opsForValue().set(cacheKey, responseJson, DEFAULT_TTL);
            log.info("Đã lưu Cache Redis cho câu hỏi với TTL 6h: '{}'", queryText);
        } catch (Exception e) {
            log.warn("Lỗi ghi Redis Semantic Cache: {}", e.getMessage());
        }
    }

    private String buildCacheKey(String queryText) {
        String normalizedQuery = queryText.trim().toLowerCase();
        return CACHE_PREFIX + sha256Hex(normalizedQuery);
    }

    private String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
