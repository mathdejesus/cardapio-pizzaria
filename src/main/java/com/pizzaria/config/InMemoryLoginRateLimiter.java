package com.pizzaria.config;

import com.pizzaria.exception.RateLimitExceededException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryLoginRateLimiter implements LoginRateLimiter {

    private static final int MAX_ATTEMPTS = RedisLoginRateLimiter.MAX_ATTEMPTS;
    private static final long WINDOW_SECONDS = RedisLoginRateLimiter.WINDOW.toSeconds();

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Override
    public void checkBlocked() {
        if (currentCount(IpUtils.getClientIP()) >= MAX_ATTEMPTS) {
            throw new RateLimitExceededException();
        }
    }

    @Override
    public void registerFailure() {
        String ip = IpUtils.getClientIP();
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
        return Math.max(0, MAX_ATTEMPTS - currentCount(IpUtils.getClientIP()));
    }

    private int currentCount(String ip) {
        AttemptWindow window = attempts.get(ip);
        if (window == null || window.isExpired(Instant.now())) {
            return 0;
        }
        return window.count();
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
