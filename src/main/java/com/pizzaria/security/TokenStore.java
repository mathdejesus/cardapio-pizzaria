package com.pizzaria.security;

import java.time.Duration;
import java.util.Optional;

public interface TokenStore {

    void storeRefreshToken(String jti, String email, Duration ttl);

    Optional<String> getRefreshTokenEmail(String jti);

    void deleteRefreshToken(String jti);

    void addToBlocklist(String jti, Duration ttl);

    boolean isBlocklisted(String jti);
}
