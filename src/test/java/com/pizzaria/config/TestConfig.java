package com.pizzaria.config;

import com.pizzaria.metrics.CardapioMetrics;
import com.pizzaria.security.JwtService;
import com.pizzaria.security.TokenStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public LoginRateLimiter loginRateLimiter() {
        return mock(LoginRateLimiter.class);
    }

    @Bean
    @Primary
    public TokenStore tokenStore() {
        return mock(TokenStore.class);
    }

    @Bean
    @Primary
    public CardapioMetrics cardapioMetrics() {
        return mock(CardapioMetrics.class);
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return mock(UserDetailsService.class);
    }
}
