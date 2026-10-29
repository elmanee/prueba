package com.proyecto.servicios.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de respuesta para Usuario.
 * Nunca se expone la entidad JPA ni el passwordHash al cliente.
 */
@Data
@Builder
public class UsuarioResponseDTO {
    private Long id;
    private String correo;
    private String estatus;
    private Long clienteId;
    private String nombreCompleto;
}
