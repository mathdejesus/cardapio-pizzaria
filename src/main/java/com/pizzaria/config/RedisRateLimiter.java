package com.pizzaria.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final String PREFIX = "rate_limit:";
    private final StringRedisTemplate redis;

    public void checkBlocked() {
        String ip = getClientIP();
        String key = keyFor(ip);
        String countStr = redis.opsForValue().get(key);
        int count = (countStr != null) ? Integer.parseInt(countStr) : 0;
        if (count >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Muitas tentativas de login. Aguarde e tente novamente.");
        }
    }

    public void registerFailure() {
        String ip = getClientIP();
        String key = keyFor(ip);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }
    }

    public void registerSuccess() {
        String ip = getClientIP();
        redis.delete(keyFor(ip));
    }

    public int getRemainingAttempts() {
        String ip = getClientIP();
        String key = keyFor(ip);
        String countStr = redis.opsForValue().get(key);
        int count = (countStr != null) ? Integer.parseInt(countStr) : 0;
        return Math.max(0, MAX_ATTEMPTS - count);
    }

    private String keyFor(String ip) {
        return PREFIX + ip + ":" + (Instant.now().getEpochSecond() / 60);
    }

    private String getClientIP() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String xfwd = request.getHeader("X-Forwarded-For");
            if (xfwd != null && !xfwd.isBlank()) {
                return xfwd.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
