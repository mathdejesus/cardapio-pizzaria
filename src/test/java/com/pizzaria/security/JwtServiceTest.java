package com.pizzaria.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String TEST_SECRET = "test-secret-key-with-at-least-32-characters-here";
    private static final long EXPIRATION_MS = 3600000;
    private static final long REFRESH_EXPIRATION_MS = 604800000;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField("secret", TEST_SECRET);
        setField("expirationMs", EXPIRATION_MS);
        setField("refreshExpirationMs", REFRESH_EXPIRATION_MS);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtService, value);
    }

    private UserDetails createUser() {
        return new User("admin@pizzaria.com", "admin123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(createUser());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@pizzaria.com");
    }

    @Test
    void generateRefreshToken_shouldReturnRefreshToken() {
        String token = jwtService.generateRefreshToken("admin@pizzaria.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isRefreshToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin@pizzaria.com");
    }

    @Test
    void extractJti_shouldReturnUniqueJti() {
        String token = jwtService.generateRefreshToken("admin@pizzaria.com");

        String jti = jwtService.extractJti(token);

        assertThat(jti).isNotBlank();
        assertThat(jti).isNotEqualTo(jwtService.extractJti(jwtService.generateRefreshToken("admin@pizzaria.com")));
    }

    @Test
    void isRefreshToken_shouldReturnFalseForAccessToken() {
        String token = jwtService.generateToken(createUser());

        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        UserDetails user = createUser();
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForWrongUser() {
        UserDetails user = createUser();
        String token = jwtService.generateToken(user);
        UserDetails otherUser = new User("other@pizzaria.com", "pass",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void getExpirationSeconds_shouldReturnCorrectValue() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(EXPIRATION_MS / 1000);
    }

    @Test
    void getRemainingExpirySeconds_shouldReturnPositiveForValidToken() {
        String token = jwtService.generateToken(createUser());

        long remaining = jwtService.getRemainingExpirySeconds(token);

        assertThat(remaining).isGreaterThan(0);
        assertThat(remaining).isLessThanOrEqualTo(EXPIRATION_MS / 1000);
    }

    @Test
    void validateSecret_shouldThrowWhenTooShort() throws Exception {
        JwtService service = new JwtService();
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(service, "short");
        Field expField = JwtService.class.getDeclaredField("expirationMs");
        expField.setAccessible(true);
        expField.set(service, EXPIRATION_MS);
        Field refreshField = JwtService.class.getDeclaredField("refreshExpirationMs");
        refreshField.setAccessible(true);
        refreshField.set(service, REFRESH_EXPIRATION_MS);

        assertThatThrownBy(service::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 characters");
    }

    @Test
    void validateSecret_shouldNotThrowWhenValid() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(jwtService::validateSecret);
    }
}
