package com.pizzaria.config;

import com.pizzaria.exception.RateLimitExceededException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class InMemoryLoginRateLimiter implements LoginRateLimiter {

    private static final long WINDOW_SECONDS = LoginRateLimiter.WINDOW.toSeconds();
    private static final int MAX_ENTRIES = 10000;

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Override
    public void checkBlocked() {
        cleanupExpired();
        String ip = IpUtils.getClientIP();
        if (currentCount(ip) >= LoginRateLimiter.MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            throw new RateLimitExceededException();
        }
    }

    @Override
    public void registerFailure() {
        cleanupExpired();
        enforceMaxSize();
        String ip = IpUtils.getClientIP();
        log.debug("Login failure registered for IP: {}", ip);
        attempts.compute(ip, (key, window) -> {
            Instant now = Instant.now();
            if (window == null || window.isExpired(now)) {
                return new AttemptWindow(now, 1);
            }
            window.increment();
            return window;
        });
    }

    @Override
    public void registerSuccess() {
        attempts.remove(IpUtils.getClientIP());
    }

    @Override
    public int getRemainingAttempts() {
        cleanupExpired();
        return Math.max(0, LoginRateLimiter.MAX_ATTEMPTS - currentCount(IpUtils.getClientIP()));
    }

    private int currentCount(String ip) {
        AttemptWindow window = attempts.get(ip);
        if (window == null || window.isExpired(Instant.now())) {
            return 0;
        }
        return window.count();
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        attempts.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    private void enforceMaxSize() {
        if (attempts.size() >= MAX_ENTRIES) {
            attempts.clear();
        }
    }

    private static final class AttemptWindow {
        private final Instant startedAt;
        private int count;

        AttemptWindow(Instant startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }

        void increment() {
            count++;
        }

        int count() {
            return count;
        }

        boolean isExpired(Instant now) {
            return now.isAfter(startedAt.plusSeconds(WINDOW_SECONDS));
        }
    }
}
