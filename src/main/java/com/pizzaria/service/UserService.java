package com.pizzaria.service;

import com.pizzaria.dto.UserProfileDTO;
import com.pizzaria.model.Usuario;
import com.pizzaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return UserProfileDTO.builder()
                .email(usuario.getEmail())
                .role(usuario.getRole())
                .build();
    }
}
