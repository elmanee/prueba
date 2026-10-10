package com.proyecto.servicios.service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ClienteActualizarDTO {
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    // Aquí irían datos de domicilio e info laboral si estuvieran mapeados
}
