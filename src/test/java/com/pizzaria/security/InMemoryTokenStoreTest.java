package com.pizzaria.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTokenStoreTest {

    private InMemoryTokenStore tokenStore;

    @BeforeEach
    void setUp() {
        tokenStore = new InMemoryTokenStore();
    }

    @Test
    void storeAndRetrieveRefreshToken() {
        tokenStore.storeRefreshToken("jti-1", "admin@pizzaria.com", Duration.ofDays(7));

        Optional<String> email = tokenStore.getRefreshTokenEmail("jti-1");

        assertThat(email).contains("admin@pizzaria.com");
    }

    @Test
    void getRefreshTokenEmail_shouldReturnEmptyForExpiredToken() {
        tokenStore.storeRefreshToken("jti-1", "admin@pizzaria.com", Duration.ofSeconds(-1));

        Optional<String> email = tokenStore.getRefreshTokenEmail("jti-1");

        assertThat(email).isEmpty();
    }

    @Test
    void getRefreshTokenEmail_shouldReturnEmptyForUnknownJti() {
        Optional<String> email = tokenStore.getRefreshTokenEmail("unknown");

        assertThat(email).isEmpty();
    }

    @Test
    void deleteRefreshToken_shouldRemoveToken() {
        tokenStore.storeRefreshToken("jti-1", "admin@pizzaria.com", Duration.ofDays(7));

        tokenStore.deleteRefreshToken("jti-1");

        assertThat(tokenStore.getRefreshTokenEmail("jti-1")).isEmpty();
    }

    @Test
    void addToBlocklist_shouldBlockToken() {
        tokenStore.addToBlocklist("jti-1", Duration.ofMinutes(5));

        assertThat(tokenStore.isBlocklisted("jti-1")).isTrue();
    }

    @Test
    void isBlocklisted_shouldReturnFalseForUnknownJti() {
        assertThat(tokenStore.isBlocklisted("unknown")).isFalse();
    }

    @Test
    void isBlocklisted_shouldReturnFalseForExpiredBlocklistEntry() {
        tokenStore.addToBlocklist("jti-1", Duration.ofSeconds(-1));

        assertThat(tokenStore.isBlocklisted("jti-1")).isFalse();
    }

    @Test
    void storeRefreshToken_shouldCleanupExpiredEntries() {
        tokenStore.storeRefreshToken("expired-1", "old@email.com", Duration.ofSeconds(-1));
        tokenStore.storeRefreshToken("valid-1", "new@email.com", Duration.ofDays(7));

        // expired-1 should be cleaned up during storeRefreshToken
        assertThat(tokenStore.getRefreshTokenEmail("expired-1")).isEmpty();
        assertThat(tokenStore.getRefreshTokenEmail("valid-1")).contains("new@email.com");
    }

    @Test
    void addToBlocklist_shouldCleanupExpiredEntries() {
        tokenStore.addToBlocklist("expired-1", Duration.ofSeconds(-1));
        tokenStore.addToBlocklist("valid-1", Duration.ofMinutes(5));

        assertThat(tokenStore.isBlocklisted("expired-1")).isFalse();
        assertThat(tokenStore.isBlocklisted("valid-1")).isTrue();
    }
}
