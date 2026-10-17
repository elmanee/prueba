package com.proyecto.servicios.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteRegistroDTO {
    private String nombre;
    private String apellidos;
    private String curp;
    private String rfc;
    private LocalDate fechaNacimiento;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String correo;
    private String rawPassword;
}