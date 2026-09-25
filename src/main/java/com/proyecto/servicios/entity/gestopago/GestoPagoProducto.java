package com.proyecto.servicios.entity.gestopago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gestopago_productos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestoPagoProducto {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "precio", length = 50)
    private String precio;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "disponible")
    private Boolean disponible;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
