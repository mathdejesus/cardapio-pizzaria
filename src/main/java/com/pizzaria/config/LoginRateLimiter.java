package com.pizzaria.config;

public interface LoginRateLimiter {

    int MAX_ATTEMPTS = 5;
    java.time.Duration WINDOW = java.time.Duration.ofMinutes(5);

    void checkBlocked();

    void registerFailure();

    void registerSuccess();

    int getRemainingAttempts();
}
