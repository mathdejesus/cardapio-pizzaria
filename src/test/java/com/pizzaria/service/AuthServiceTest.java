package com.pizzaria.service;

import com.pizzaria.config.RedisRateLimiter;
import com.pizzaria.dto.AuthResponseDTO;
import com.pizzaria.dto.LoginRequestDTO;
import com.pizzaria.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserDetailsService userDetailsService;

    @Mock
    JwtService jwtService;

    @Mock
    RedisRateLimiter loginRateLimiter;

    @Mock
    StringRedisTemplate redis;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    AuthService authService;

    private final LoginRequestDTO loginRequest = new LoginRequestDTO("admin@pizzaria.com", "admin123");
    private final UserDetails userDetails = new User("admin@pizzaria.com", "admin123", List.of());

    @Test
    void login_shouldReturnTokensOnSuccess() {
        when(jwtService.generateToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken("admin@pizzaria.com")).thenReturn("refresh-token");
        when(jwtService.extractJti("refresh-token")).thenReturn("jti-123");
        when(userDetailsService.loadUserByUsername("admin@pizzaria.com")).thenReturn(userDetails);
        when(redis.opsForValue()).thenReturn(valueOps);

        AuthResponseDTO response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(valueOps).set(eq("refresh_token:jti-123"), eq("admin@pizzaria.com"), any());
    }

    @Test
    void login_shouldRegisterFailureOnBadCredentials() {
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginRateLimiter).registerFailure();
    }

    @Test
    void refreshToken_shouldReturnNewTokenPair() {
        when(jwtService.isRefreshToken("old-refresh")).thenReturn(true);
        when(jwtService.extractJti("old-refresh")).thenReturn("old-jti");
        when(jwtService.extractUsername("old-refresh")).thenReturn("admin@pizzaria.com");
        when(redis.hasKey("blocklist:old-jti")).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("refresh_token:old-jti")).thenReturn("admin@pizzaria.com");
        when(userDetailsService.loadUserByUsername("admin@pizzaria.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new-access");
        when(jwtService.generateRefreshToken("admin@pizzaria.com")).thenReturn("new-refresh");
        when(jwtService.extractJti("new-refresh")).thenReturn("new-jti");

        AuthResponseDTO response = authService.refreshToken("old-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(redis).delete("refresh_token:old-jti");
        verify(valueOps).set(eq("refresh_token:new-jti"), eq("admin@pizzaria.com"), any());
    }

    @Test
    void refreshToken_shouldThrowWhenTokenIsNotRefreshType() {
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("access-token"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refreshToken_shouldThrowWhenTokenIsBlocklisted() {
        when(jwtService.isRefreshToken("revoked-refresh")).thenReturn(true);
        when(jwtService.extractJti("revoked-refresh")).thenReturn("revoked-jti");
        when(jwtService.extractUsername("revoked-refresh")).thenReturn("admin@pizzaria.com");
        when(redis.hasKey("blocklist:revoked-jti")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("revoked-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("revogado");
    }

    @Test
    void logout_shouldAddTokenToBlocklist() {
        when(jwtService.extractJti("token")).thenReturn("jti-123");
        when(jwtService.getRemainingExpirySeconds("token")).thenReturn(3600L);
        when(redis.opsForValue()).thenReturn(valueOps);

        authService.logout("token");

        verify(valueOps).set(eq("blocklist:jti-123"), eq("true"), any());
    }
}
