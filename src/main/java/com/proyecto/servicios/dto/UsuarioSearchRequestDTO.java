package com.proyecto.servicios.dto;

import lombok.Data;

/**
 * DTO de búsqueda de usuarios.
 * Se recibe mediante @RequestBody en el único endpoint de consulta.
 */
@Data
public class UsuarioSearchRequestDTO {
    private Long id;
    private String correo;
    private String estatus;
    private Long clienteId;
}
