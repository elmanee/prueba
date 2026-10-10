package com.proyecto.servicios.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cuentas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Cuenta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Cliente cliente;

    @Column(name = "numero_cuenta", nullable = false, length = 20, unique = true)
    private String numeroCuenta;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String moneda = "MXN";

    @Builder.Default
    @jakarta.validation.constraints.PositiveOrZero
    @Column(nullable = false)
    private BigDecimal saldo = new BigDecimal("0.00");

    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private String tipoCuenta;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;
}