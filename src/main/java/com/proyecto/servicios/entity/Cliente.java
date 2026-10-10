package com.proyecto.servicios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    @Column(nullable = false, length = 50)
    private String nombre;
    
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]*$")
    @Column(name = "segundo_nombre", length = 50)
    private String segundoNombre;

    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    @Column(name = "apellido_paterno", nullable = false, length = 50)
    private String apellidoPaterno;
    
    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s]+$")
    @Column(name = "apellido_materno", nullable = false, length = 50)
    private String apellidoMaterno;

    @NotBlank @Pattern(regexp = "^[A-Z0-9]{18}$")
    @Column(nullable = false, length = 18, unique = true)
    private String curp;

    @NotBlank @Pattern(regexp = "^[A-Z0-9]{12,13}$")
    @Column(nullable = false, length = 13, unique = true)
    private String rfc;

    @NotNull @Past
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;
    
    @NotBlank @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String sexo;
    
    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nacionalidad;
    
    @NotBlank @Size(max = 50)
    @Column(name = "estado_civil", nullable = false, length = 50)
    private String estadoCivil;
    
    @NotBlank @Pattern(regexp = "^[0-9]{10}$")
    @Column(name = "telefono_movil", nullable = false, length = 10)
    private String telefonoMovil;
    
    @Pattern(regexp = "^[0-9]{10}$")
    @Column(name = "telefono_alternativo", length = 10)
    private String telefonoAlternativo;
    
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String ocupacion;
    
    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String empresa;
    
    @NotNull @Positive
    @Column(name = "ingreso_mensual", nullable = false)
    private BigDecimal ingresoMensual;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Usuario usuario;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Domicilio> domicilios;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Cuenta> cuentas;
}