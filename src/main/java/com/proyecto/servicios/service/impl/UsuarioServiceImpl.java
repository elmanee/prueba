package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.dto.CambioPasswordRequestDTO;
import com.proyecto.servicios.dto.UsuarioResponseDTO;
import com.proyecto.servicios.dto.UsuarioSearchRequestDTO;
import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.exception.UsuarioNoEncontradoException;
import com.proyecto.servicios.repository.UsuarioRepository;
import com.proyecto.servicios.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de administración de usuarios.
 * Devuelve DTOs, nunca entidades JPA directamente.
 */
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> buscar(UsuarioSearchRequestDTO req) {
        // Búsqueda dinámica: si viene id, busca por id; si viene correo, por correo;
        // si viene estatus, filtra por estatus; de lo contrario devuelve todos.
        List<Usuario> usuarios;

        if (req.getId() != null) {
            Usuario u = usuarioRepository.findById(req.getId())
                    .orElseThrow(() -> new UsuarioNoEncontradoException(
                            "Usuario no encontrado con id: " + req.getId()));
            usuarios = List.of(u);
        } else if (req.getCorreo() != null && !req.getCorreo().isBlank()) {
            Usuario u = usuarioRepository.findByCorreo(req.getCorreo())
                    .orElseThrow(() -> new UsuarioNoEncontradoException(
                            "Usuario no encontrado con correo: " + req.getCorreo()));
            usuarios = List.of(u);
        } else {
            usuarios = usuarioRepository.findAll();
        }

        // Filtro en memoria por estatus y clienteId si se especificaron
        return usuarios.stream()
                .filter(u -> req.getEstatus() == null || req.getEstatus().equalsIgnoreCase(u.getEstatus()))
                .filter(u -> req.getClienteId() == null
                        || (u.getCliente() != null && req.getClienteId().equals(u.getCliente().getId())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarPassword(CambioPasswordRequestDTO req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.getCorreo())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado con correo: " + req.getCorreo()));
        usuario.setPasswordHash(passwordEncoder.encode(req.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    // -------------------------------------------------------------------------
    // Mapeo entidad → DTO
    // -------------------------------------------------------------------------

    private UsuarioResponseDTO toDTO(Usuario u) {
        String nombreCompleto = null;
        Long clienteId = null;
        if (u.getCliente() != null) {
            clienteId = u.getCliente().getId();
            nombreCompleto = u.getCliente().getNombre() + " " + u.getCliente().getApellidoPaterno();
        }
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .correo(u.getCorreo())
                .estatus(u.getEstatus())
                .clienteId(clienteId)
                .nombreCompleto(nombreCompleto)
                .build();
    }
}
