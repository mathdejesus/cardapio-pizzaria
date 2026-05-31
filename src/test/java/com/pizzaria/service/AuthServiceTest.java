package com.pizzaria.service;

import com.pizzaria.config.LoginRateLimiter;
import com.pizzaria.dto.AuthResponseDTO;
import com.pizzaria.dto.LoginRequestDTO;
import com.pizzaria.metrics.CardapioMetrics;
import com.pizzaria.security.JwtService;
import com.pizzaria.security.TokenStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

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
    LoginRateLimiter loginRateLimiter;

    @Mock
    TokenStore tokenStore;

    @Mock
    CardapioMetrics metrics;

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

        AuthResponseDTO response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(tokenStore).storeRefreshToken(eq("jti-123"), eq("admin@pizzaria.com"), any(Duration.class));
        verify(metrics).incrementLoginAttempts();
    }

    @Test
    void login_shouldRegisterFailureOnBadCredentials() {
        doThrow(new BadCredentialsException("bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginRateLimiter).registerFailure();
        verify(metrics).incrementLoginFailures();
    }

    @Test
    void refreshToken_shouldReturnNewTokenPair() {
        when(jwtService.isRefreshToken("old-refresh")).thenReturn(true);
        when(jwtService.extractJti("old-refresh")).thenReturn("old-jti");
        when(jwtService.extractUsername("old-refresh")).thenReturn("admin@pizzaria.com");
        when(tokenStore.isBlocklisted("old-jti")).thenReturn(false);
        when(tokenStore.getRefreshTokenEmail("old-jti")).thenReturn(Optional.of("admin@pizzaria.com"));
        when(userDetailsService.loadUserByUsername("admin@pizzaria.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new-access");
        when(jwtService.generateRefreshToken("admin@pizzaria.com")).thenReturn("new-refresh");
        when(jwtService.extractJti("new-refresh")).thenReturn("new-jti");

        AuthResponseDTO response = authService.refreshToken("old-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(tokenStore).deleteRefreshToken("old-jti");
        verify(tokenStore).storeRefreshToken(eq("new-jti"), eq("admin@pizzaria.com"), any(Duration.class));
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
        when(tokenStore.isBlocklisted("revoked-jti")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken("revoked-refresh"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("revogado");
    }

    @Test
    void logout_shouldAddTokenToBlocklist() {
        when(jwtService.extractJti("token")).thenReturn("jti-123");
        when(jwtService.getRemainingExpirySeconds("token")).thenReturn(3600L);

        authService.logout("token");

        verify(tokenStore).addToBlocklist(eq("jti-123"), any(Duration.class));
    }
}
