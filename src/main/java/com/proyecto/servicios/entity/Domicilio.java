package com.proyecto.servicios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "domicilios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Domicilio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cliente cliente;

    @NotBlank @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String calle;

    @NotBlank @Size(max = 20)
    @Column(name = "numero_exterior", nullable = false, length = 20)
    private String numeroExterior;

    @Size(max = 20)
    @Column(name = "numero_interior", length = 20)
    private String numeroInterior;

    @NotBlank @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String colonia;

    @NotBlank @Pattern(regexp = "^[0-9]{5}$")
    @Column(name = "codigo_postal", nullable = false, length = 5)
    private String codigoPostal;

    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String municipio;

    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String estado;
    
    @NotBlank @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String pais;
}