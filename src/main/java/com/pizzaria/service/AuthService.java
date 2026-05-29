package com.pizzaria.service;

import com.pizzaria.config.RedisRateLimiter;
import com.pizzaria.dto.AuthResponseDTO;
import com.pizzaria.dto.LoginRequestDTO;
import com.pizzaria.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RedisRateLimiter loginRateLimiter;
    private final StringRedisTemplate redis;

    public AuthResponseDTO login(LoginRequestDTO request) {
        loginRateLimiter.checkBlocked();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        } catch (BadCredentialsException ex) {
            loginRateLimiter.registerFailure();
            throw ex;
        }

        loginRateLimiter.registerSuccess();
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());
        String jti = jwtService.extractJti(refreshToken);

        redis.opsForValue().set("refresh_token:" + jti, request.getEmail(), Duration.ofDays(7));

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponseDTO refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Token invalido");
        }

        String jti = jwtService.extractJti(refreshToken);
        String email = jwtService.extractUsername(refreshToken);

        if (Boolean.TRUE.equals(redis.hasKey("blocklist:" + jti))) {
            throw new IllegalArgumentException("Refresh token revogado");
        }

        String storedEmail = redis.opsForValue().get("refresh_token:" + jti);
        if (storedEmail == null || !storedEmail.equals(email)) {
            throw new IllegalArgumentException("Refresh token invalido ou expirado");
        }

        redis.delete("refresh_token:" + jti);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(email);
        String newJti = jwtService.extractJti(newRefreshToken);

        redis.opsForValue().set("refresh_token:" + newJti, email, Duration.ofDays(7));

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String token) {
        String jti = jwtService.extractJti(token);
        long remaining = jwtService.getRemainingExpirySeconds(token);
        if (remaining > 0) {
            redis.opsForValue().set("blocklist:" + jti, "true", Duration.ofSeconds(remaining));
        }
    }
}
