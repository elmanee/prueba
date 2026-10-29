package com.proyecto.servicios.controller;

import com.proyecto.servicios.dto.PagoServicioRequestDTO;
import com.proyecto.servicios.dto.PagoServicioResponseDTO;
import com.proyecto.servicios.entity.EstadoTransaccion;
import com.proyecto.servicios.service.PagoServicioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones transaccionales de pago, recarga, consulta y conciliación de servicios.
 */
@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
@Tag(name = "Pagos y Recargas", description = "Endpoints para el pago de servicios, recargas, consulta y conciliación mediante GestoPago")
public class PagoServicioController {

    private final PagoServicioService pagoServicioService;

    /**
     * Procesa el pago o recarga de un servicio debitando de la cuenta del cliente e interactuando con GestoPago.
     *
     * @param request Datos de la operación
     * @return {@link PagoServicioResponseDTO} con el resultado de la transacción
     */
    @PostMapping(
        value = "/pagar",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Pagar o recargar servicio", description = "Realiza el débito en la cuenta y procesa la transacción contra el proveedor externo Gestopago")
    public ResponseEntity<PagoServicioResponseDTO> pagarServicio(@Valid @RequestBody PagoServicioRequestDTO request) {
        PagoServicioResponseDTO response = pagoServicioService.procesarPago(request);

        if (response.getEstatus() == EstadoTransaccion.EXITOSA) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getEstatus() == EstadoTransaccion.TIMEOUT) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } else {
            // Operación FALLIDA o rechazada por el proveedor
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
    }

    /**
     * Permite consultar el estado actual de una transacción por su referencia única (ideal para seguimiento de TIMEOUT).
     *
     * @param referencia Folio único de la transacción
     * @return {@link PagoServicioResponseDTO} con los detalles y estado actual
     */
    @GetMapping(
        value = "/transacciones/{referencia}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Consultar estado de transacción", description = "Consulta el estado actual de una operación previamente iniciada")
    public ResponseEntity<PagoServicioResponseDTO> consultarTransaccion(@PathVariable String referencia) {
        return ResponseEntity.ok(pagoServicioService.consultarTransaccion(referencia));
    }

    /**
     * Concilia una transacción cuyo estado quedó indeterminado (TIMEOUT / PENDIENTE).
     *
     * @param referencia Folio único de la transacción
     * @param confirmarExitosa True para confirmar como EXITOSA, False para marcar como FALLIDA y devolver el saldo al cliente
     * @param folioProveedor Folio retornado por el proveedor externo
     * @return {@link PagoServicioResponseDTO} resultado de la conciliación
     */
    @PostMapping(
        value = "/transacciones/{referencia}/conciliar",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(summary = "Conciliar transacción", description = "Permite conciliar manualmente o por proceso automático una transacción en estado TIMEOUT o PENDIENTE")
    public ResponseEntity<PagoServicioResponseDTO> conciliarTransaccion(
            @PathVariable String referencia,
            @RequestParam(required = false, defaultValue = "false") boolean confirmarExitosa,
            @RequestParam(required = false) String folioProveedor) {
        return ResponseEntity.ok(pagoServicioService.conciliarTransaccion(referencia, confirmarExitosa, folioProveedor));
    }
}
