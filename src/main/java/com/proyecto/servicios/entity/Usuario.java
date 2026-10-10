package com.proyecto.servicios.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cliente cliente;

    @NotBlank @Email
    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estatus = "ACTIVO";
}
