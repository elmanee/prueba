package com.proyecto.servicios.service;

import com.proyecto.servicios.dto.PagoServicioRequestDTO;
import com.proyecto.servicios.dto.PagoServicioResponseDTO;

public interface PagoServicioService {

    /**
     * Procesa de forma transaccional el pago o recarga de un servicio mediante Gestopago.
     *
     * @param request Datos de la operación validados
     * @return Respuesta con el resultado de la transacción
     */
    PagoServicioResponseDTO procesarPago(PagoServicioRequestDTO request);

    /**
     * Consulta el estado de una transacción previamente registrada por su referencia o folio único.
     * Esencial para verificar operaciones en estado TIMEOUT o PENDIENTE.
     *
     * @param referencia Referencia única de la transacción
     * @return Respuesta con el estado actual de la transacción
     */
    PagoServicioResponseDTO consultarTransaccion(String referencia);

    /**
     * Concilia una transacción cuyo estado quedó indeterminado (TIMEOUT / PENDIENTE),
     * permitiendo actualizarla a EXITOSA o FALLIDA con el respectivo ajuste de saldo.
     *
     * @param referencia Referencia única de la transacción
     * @param confirmarExitosa Si true, se asienta como EXITOSA; si false, se marca como FALLIDA y se reversa el saldo
     * @param folioProveedor Folio de confirmación del proveedor si existió
     * @return Respuesta con el resultado de la conciliación
     */
    PagoServicioResponseDTO conciliarTransaccion(String referencia, boolean confirmarExitosa, String folioProveedor);
}
