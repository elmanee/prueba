package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + username));

        if ("INACTIVO".equalsIgnoreCase(usuario.getEstatus())) {
            throw new com.proyecto.servicios.exception.UsuarioInactivoException("El usuario se encuentra inactivo");
        }

        return new User(usuario.getCorreo(), usuario.getPasswordHash(), new ArrayList<>());
    }
}
