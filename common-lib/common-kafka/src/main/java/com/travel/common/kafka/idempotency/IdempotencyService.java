package com.travel.common.kafka.idempotency;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {
    private static final String KEY_PREFIX = "event:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isProcessed(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            return false;
        }
        String key = KEY_PREFIX + eventId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSED", DEFAULT_TTL);
        return Boolean.FALSE.equals(success);
    }
}
