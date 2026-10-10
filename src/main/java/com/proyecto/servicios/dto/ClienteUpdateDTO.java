package com.proyecto.servicios.dto;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
public class ClienteUpdateDTO {
    private String nombre;
    private String segundoNombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String nacionalidad;
    private String estadoCivil;
    
    private String correo;
    private String telefonoMovil;
    private String telefonoAlternativo;
    
    private String ocupacion;
    private String empresa;
    private BigDecimal ingresoMensual;
    
    // Datos de domicilio
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String colonia;
    private String codigoPostal;
    private String municipio;
    private String estado;
    private String pais;
}
