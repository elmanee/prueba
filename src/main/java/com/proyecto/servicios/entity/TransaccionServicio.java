package com.proyecto.servicios.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "transacciones_servicio",
    indexes = {
        @Index(name = "idx_transaccion_referencia", columnList = "referencia", unique = true),
        @Index(name = "idx_transaccion_cuenta_fecha", columnList = "cuenta_id, fecha_creacion")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referencia", nullable = false, length = 100, unique = true)
    private String referencia;

    @Column(name = "id_servicio", nullable = false, length = 50)
    private String idServicio;

    @Column(name = "id_producto", nullable = false, length = 50)
    private String idProducto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransaccion estado;

    @Column(name = "numero_referencia_servicio", length = 100)
    private String numeroReferenciaServicio;

    @Column(name = "mensaje_respuesta", length = 255)
    private String mensajeRespuesta;

    @Column(name = "folio_autorizacion_proveedor", length = 100)
    private String folioAutorizacionProveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    @Builder.Default
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }
}
