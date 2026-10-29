package com.proyecto.servicios.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de búsqueda de clientes.
 * Todos los campos son opcionales y se combinan dinámicamente (AND).
 * Se recibe mediante @RequestBody, NO mediante @RequestParam ni @PathVariable.
 */
@Data
public class ClienteSearchRequestDTO {
    private Long id;
    private String nombre;
    private String apellidoPaterno;
    private String curp;
    private String rfc;
    private String correo;
    private String sexo;
    private String nacionalidad;
    private Boolean activo;
    private LocalDate fechaNacimientoDesde;
    private LocalDate fechaNacimientoHasta;
    private LocalDateTime creadoDesde;
    private LocalDateTime creadoHasta;
    private BigDecimal ingresoMensualMinimo;
}
