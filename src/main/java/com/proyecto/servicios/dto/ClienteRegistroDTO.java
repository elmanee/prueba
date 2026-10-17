package com.proyecto.servicios.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class ClienteRegistroDTO {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$", message = "Formato de nombre inválido")
    private String nombre;
    @jakarta.validation.constraints.Size(min = 2, max = 50)
    @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*$")
    private String segundoNombre;
    @NotBlank(message = "El apellido paterno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido paterno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$", message = "Formato de apellido paterno inválido")
    private String apellidoPaterno;
    @NotBlank(message = "El apellido materno es obligatorio")
    @Size(min = 2, max = 50, message = "El apellido materno debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$", message = "Formato de apellido materno inválido")
    private String apellidoMaterno;
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;
    @NotBlank(message = "La CURP es obligatoria")
    @Size(min = 18, max = 18, message = "La CURP debe tener exactamente 18 caracteres")
    @Pattern(regexp = "^[A-Z0-9]{18}$", message = "Formato de CURP inválido")
    private String curp;
    @NotBlank(message = "El RFC es obligatorio")
    @Pattern(regexp = "^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$", message = "Formato de RFC inválido")
    private String rfc;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String correo;
    private String telefonoMovil;
    private String telefonoAlternativo;
    
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String rawPassword;
}
