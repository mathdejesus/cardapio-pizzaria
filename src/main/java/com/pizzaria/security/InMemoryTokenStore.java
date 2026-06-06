package com.pizzaria.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, Entry> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> blocklist = new ConcurrentHashMap<>();
    private final Map<String, String> accessToRefresh = new ConcurrentHashMap<>();

    @Override
    public void storeRefreshToken(String jti, String email, Duration ttl) {
        cleanupExpiredRefreshTokens();
        refreshTokens.put(jti, new Entry(email, Instant.now().plus(ttl)));
    }

    @Override
    public Optional<String> getRefreshTokenEmail(String jti) {
        Entry entry = refreshTokens.get(jti);
        if (entry == null || entry.isExpired()) {
            refreshTokens.remove(jti);
            return Optional.empty();
        }
        return Optional.of(entry.email());
    }

    @Override
    public void deleteRefreshToken(String jti) {
        refreshTokens.remove(jti);
    }

    @Override
    public void addToBlocklist(String jti, Duration ttl) {
        cleanupExpiredBlocklist();
        blocklist.put(jti, Instant.now().plus(ttl));
    }

    @Override
    public boolean isBlocklisted(String jti) {
        Instant expiry = blocklist.get(jti);
        if (expiry == null) {
            return false;
        }
        if (Instant.now().isAfter(expiry)) {
            blocklist.remove(jti);
            return false;
        }
        return true;
    }

    @Override
    public void storeAccessTokenMapping(String accessJti, String refreshJti, Duration ttl) {
        cleanupExpiredAccessMappings();
        accessToRefresh.put(accessJti, refreshJti);
    }

    @Override
    public Optional<String> getRefreshJtiByAccessJti(String accessJti) {
        String refreshJti = accessToRefresh.get(accessJti);
        return Optional.ofNullable(refreshJti);
    }

    @Override
    public void deleteAccessTokenMapping(String accessJti) {
        accessToRefresh.remove(accessJti);
    }

    private record Entry(String email, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private void cleanupExpiredRefreshTokens() {
        refreshTokens.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    private void cleanupExpiredBlocklist() {
        blocklist.entrySet().removeIf(e -> Instant.now().isAfter(e.getValue()));
    }

    private void cleanupExpiredAccessMappings() {
    }
}
