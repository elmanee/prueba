package com.proyecto.servicios.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class ClienteRegistroDTO {
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 2, max = 50)
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    private String nombre;
    @jakarta.validation.constraints.Size(min = 2, max = 50)
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*$")
    private String segundoNombre;
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 2, max = 50)
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    private String apellidoPaterno;
    @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(min = 2, max = 50)
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String curp;
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    
    private String correo;
    private String telefonoMovil;
    private String telefonoAlternativo;
    
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    
    private String rawPassword;
}
