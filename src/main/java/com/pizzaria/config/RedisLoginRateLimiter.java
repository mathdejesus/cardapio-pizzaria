package com.pizzaria.config;

import com.pizzaria.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisLoginRateLimiter implements LoginRateLimiter {

    static final int MAX_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(5);
    private static final String PREFIX = "rate_limit:";

    private final StringRedisTemplate redis;

    @Override
    public void checkBlocked() {
        int count = currentCount();
        if (count >= MAX_ATTEMPTS) {
            throw new RateLimitExceededException();
        }
    }

    @Override
    public void registerFailure() {
        String key = keyFor(IpUtils.getClientIP());
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, WINDOW);
        }
    }

    @Override
    public void registerSuccess() {
        redis.delete(keyFor(IpUtils.getClientIP()));
    }

    @Override
    public int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - currentCount());
    }

    private int currentCount() {
        String countStr = redis.opsForValue().get(keyFor(IpUtils.getClientIP()));
        return countStr != null ? Integer.parseInt(countStr) : 0;
    }

    private String keyFor(String ip) {
        return PREFIX + ip;
    }
}
