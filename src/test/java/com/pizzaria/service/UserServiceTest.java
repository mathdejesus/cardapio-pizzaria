package com.pizzaria.service;

import com.pizzaria.dto.UserProfileDTO;
import com.pizzaria.enums.Role;
import com.pizzaria.model.Usuario;
import com.pizzaria.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UserService userService;

    private Usuario createUsuario(String email, Role role) {
        return Usuario.builder()
                .email(email)
                .senha("hashed")
                .role(role)
                .build();
    }

    @Test
    void getProfile_shouldReturnUserProfile() {
        Usuario usuario = createUsuario("admin@pizzaria.com", Role.ADMIN);
        when(usuarioRepository.findByEmail("admin@pizzaria.com")).thenReturn(Optional.of(usuario));

        UserProfileDTO profile = userService.getProfile("admin@pizzaria.com");

        assertThat(profile.getEmail()).isEqualTo("admin@pizzaria.com");
        assertThat(profile.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void getProfile_shouldThrowWhenUserNotFound() {
        when(usuarioRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile("unknown@email.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown@email.com");
    }

    @Test
    void hasRole_shouldReturnTrueWhenRoleMatches() {
        Usuario usuario = createUsuario("admin@pizzaria.com", Role.ADMIN);
        when(usuarioRepository.findByEmail("admin@pizzaria.com")).thenReturn(Optional.of(usuario));

        assertThat(userService.hasRole("admin@pizzaria.com", Role.ADMIN)).isTrue();
    }

    @Test
    void hasRole_shouldReturnFalseWhenRoleDoesNotMatch() {
        Usuario usuario = createUsuario("user@pizzaria.com", Role.USER);
        when(usuarioRepository.findByEmail("user@pizzaria.com")).thenReturn(Optional.of(usuario));

        assertThat(userService.hasRole("user@pizzaria.com", Role.ADMIN)).isFalse();
    }

    @Test
    void hasRole_shouldReturnFalseWhenUserNotFound() {
        when(usuarioRepository.findByEmail("unknown@email.com")).thenReturn(Optional.empty());

        assertThat(userService.hasRole("unknown@email.com", Role.ADMIN)).isFalse();
    }
}
