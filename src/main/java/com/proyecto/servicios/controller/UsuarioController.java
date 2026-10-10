package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado"));
        
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial.");
        }
        
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        
        return ResponseEntity.noContent().build();
    }
}
