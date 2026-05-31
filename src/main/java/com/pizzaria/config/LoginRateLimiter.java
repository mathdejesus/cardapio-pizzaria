package com.pizzaria.config;

public interface LoginRateLimiter {

    void checkBlocked();

    void registerFailure();

    void registerSuccess();

    int getRemainingAttempts();
}
