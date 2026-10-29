package com.proyecto.servicios.dto;

import com.proyecto.servicios.entity.EstadoTransaccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioResponseDTO {

    private String folio;
    private EstadoTransaccion estatus;
    private String mensaje;
    private String idServicio;
    private String idProducto;
    private BigDecimal monto;
    private BigDecimal saldoActualizado;
    private LocalDateTime fechaOperacion;
    private String folioProveedor;
}
