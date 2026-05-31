package com.pizzaria.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        if (currentCount(getClientIP()) >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Muitas tentativas de login. Aguarde e tente novamente.");
        }
    }

    @Override
    public void registerFailure() {
        String ip = getClientIP();
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
        attempts.remove(getClientIP());
    }

    @Override
    public int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - currentCount(getClientIP()));
    }

    private int currentCount(String ip) {
        AttemptWindow window = attempts.get(ip);
        if (window == null || window.isExpired(Instant.now())) {
            return 0;
        }
        return window.count();
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
