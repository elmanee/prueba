package com.proyecto.servicios.service;

import com.proyecto.servicios.dto.CambioPasswordRequestDTO;
import com.proyecto.servicios.dto.UsuarioResponseDTO;
import com.proyecto.servicios.dto.UsuarioSearchRequestDTO;

import java.util.List;

/**
 * Contrato de servicio para la administración de usuarios.
 */
public interface UsuarioService {

    /**
     * Busca usuarios por criterios dinámicos. Todos los campos son opcionales.
     */
    List<UsuarioResponseDTO> buscar(UsuarioSearchRequestDTO req);

    /**
     * Cambia la contraseña del usuario identificado por correo.
     */
    void cambiarPassword(CambioPasswordRequestDTO req);
}
