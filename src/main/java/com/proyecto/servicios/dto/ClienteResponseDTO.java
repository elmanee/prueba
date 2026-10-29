package com.proyecto.servicios.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para datos de Cliente.
 * Nunca se expone la entidad JPA directamente.
 */
@Data
@Builder
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String curp;
    private String rfc;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    private String telefonoMovil;
    private String telefonoAlternativo;
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private String correo;
}
