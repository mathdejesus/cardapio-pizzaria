package com.pizzaria.config;

import com.pizzaria.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class InMemoryLoginRateLimiterTest {

    private InMemoryLoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new InMemoryLoginRateLimiter();
    }

    private void mockClientIP(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request, null);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    @Test
    void checkBlocked_shouldNotThrowWhenBelowLimit() {
        mockClientIP("192.168.1.1");

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(rateLimiter::checkBlocked);
    }

    @Test
    void checkBlocked_shouldThrowWhenAtLimit() {
        mockClientIP("192.168.1.1");
        for (int i = 0; i < 5; i++) {
            rateLimiter.registerFailure();
        }

        assertThatThrownBy(rateLimiter::checkBlocked)
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void registerFailure_shouldIncrementCount() {
        mockClientIP("192.168.1.1");
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();

        assertThat(rateLimiter.getRemainingAttempts()).isEqualTo(3);
    }

    @Test
    void registerSuccess_shouldResetCount() {
        mockClientIP("192.168.1.1");
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();
        rateLimiter.registerSuccess();

        assertThat(rateLimiter.getRemainingAttempts()).isEqualTo(5);
    }

    @Test
    void getRemainingAttempts_shouldReturnMaxWhenNoFailures() {
        mockClientIP("192.168.1.1");

        assertThat(rateLimiter.getRemainingAttempts()).isEqualTo(5);
    }

    @Test
    void differentIPs_shouldHaveSeparateCounters() {
        mockClientIP("192.168.1.1");
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();
        rateLimiter.registerFailure();

        mockClientIP("192.168.1.2");
        assertThat(rateLimiter.getRemainingAttempts()).isEqualTo(5);
    }

    @Test
    void getClientIP_shouldUseXForwardedForWhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        ServletRequestAttributes attrs = new ServletRequestAttributes(request, null);
        RequestContextHolder.setRequestAttributes(attrs);

        rateLimiter.registerFailure();

        // Different IP should have separate counter
        mockClientIP("192.168.1.1");
        assertThat(rateLimiter.getRemainingAttempts()).isEqualTo(5);
    }
}
