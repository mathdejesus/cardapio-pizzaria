package com.pizzaria.controller;

import com.pizzaria.config.TestConfig;
import com.pizzaria.dto.AuthResponseDTO;
import com.pizzaria.dto.LoginRequestDTO;
import com.pizzaria.dto.RefreshTokenRequestDTO;
import com.pizzaria.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthControllerTest {

    @Autowired
    TestRestTemplate rest;

    @MockBean
    AuthService authService;

    @Test
    void login_shouldReturnTokens() {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .accessToken("access-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .refreshToken("refresh-token")
                .build();

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        ResponseEntity<AuthResponseDTO> result = rest.postForEntity(
                "/api/auth/login",
                new LoginRequestDTO("admin@pizzaria.com", "admin123"),
                AuthResponseDTO.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getAccessToken()).isEqualTo("access-token");
        assertThat(result.getBody().getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void refresh_shouldReturnNewTokens() {
        AuthResponseDTO response = AuthResponseDTO.builder()
                .accessToken("new-access")
                .tokenType("Bearer")
                .expiresIn(3600)
                .refreshToken("new-refresh")
                .build();

        when(authService.refreshToken(anyString())).thenReturn(response);

        ResponseEntity<AuthResponseDTO> result = rest.postForEntity(
                "/api/auth/refresh",
                new RefreshTokenRequestDTO("old-refresh"),
                AuthResponseDTO.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getAccessToken()).isEqualTo("new-access");
        assertThat(result.getBody().getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logout_shouldReturnNoContent() {
        ResponseEntity<Void> result = rest.withBasicAuth("admin@pizzaria.com", "admin123")
                .postForEntity("/api/auth/logout", null, Void.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
