package com.pizzaria.service;

import com.pizzaria.config.LoginRateLimiter;
import com.pizzaria.dto.AuthResponseDTO;
import com.pizzaria.dto.LoginRequestDTO;
import com.pizzaria.metrics.CardapioMetrics;
import com.pizzaria.security.JwtService;
import com.pizzaria.security.TokenStore;
import lombok.RequiredArgsConstructor;
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
    private final LoginRateLimiter loginRateLimiter;
    private final TokenStore tokenStore;
    private final CardapioMetrics metrics;

    public AuthResponseDTO login(LoginRequestDTO request) {
        metrics.incrementLoginAttempts();
        loginRateLimiter.checkBlocked();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        } catch (BadCredentialsException ex) {
            loginRateLimiter.registerFailure();
            metrics.incrementLoginFailures();
            throw ex;
        }

        loginRateLimiter.registerSuccess();
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());
        String jti = jwtService.extractJti(refreshToken);

        tokenStore.storeRefreshToken(jti, request.getEmail(), Duration.ofDays(7));

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

        if (tokenStore.isBlocklisted(jti)) {
            throw new IllegalArgumentException("Refresh token revogado");
        }

        String storedEmail = tokenStore.getRefreshTokenEmail(jti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token invalido ou expirado"));

        if (!storedEmail.equals(email)) {
            throw new IllegalArgumentException("Refresh token invalido ou expirado");
        }

        tokenStore.deleteRefreshToken(jti);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(email);
        String newJti = jwtService.extractJti(newRefreshToken);

        tokenStore.storeRefreshToken(newJti, email, Duration.ofDays(7));

        return AuthResponseDTO.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String token) {
        try {
            String jti = jwtService.extractJti(token);
            long remaining = jwtService.getRemainingExpirySeconds(token);
            if (remaining > 0) {
                tokenStore.addToBlocklist(jti, Duration.ofSeconds(remaining));
            }
        } catch (Exception e) {
            // Token inválido ou malformado — ignora graciosamente
        }
    }
}
