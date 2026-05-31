package com.pizzaria.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {

    private final StringRedisTemplate redis;

    @Override
    public void storeRefreshToken(String jti, String email, Duration ttl) {
        redis.opsForValue().set("refresh_token:" + jti, email, ttl);
    }

    @Override
    public Optional<String> getRefreshTokenEmail(String jti) {
        return Optional.ofNullable(redis.opsForValue().get("refresh_token:" + jti));
    }

    @Override
    public void deleteRefreshToken(String jti) {
        redis.delete("refresh_token:" + jti);
    }

    @Override
    public void addToBlocklist(String jti, Duration ttl) {
        redis.opsForValue().set("blocklist:" + jti, "true", ttl);
    }

    @Override
    public boolean isBlocklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey("blocklist:" + jti));
    }
}
