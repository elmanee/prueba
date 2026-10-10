package com.proyecto.servicios.service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRegistroDTO {
    private String nombre;
    private String apellidos;
    private String curp;
    private String rfc;
    private LocalDate fechaNacimiento;
    private String correo;
    private String rawPassword;
}