package com.proyecto.servicios.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * Wrapper para el endpoint de actualización de cliente.
 * Contiene el identificador del cliente (id) y los campos a modificar.
 */
@Data
public class ClienteActualizarRequestDTO {

    /** Criterio de identificación del cliente (al menos id es obligatorio). */
    @Valid
    private ClienteSearchRequestDTO busqueda;

    /** Datos a actualizar (todos los campos son opcionales). */
    @Valid
    private ClienteUpdateDTO cambios;
}
