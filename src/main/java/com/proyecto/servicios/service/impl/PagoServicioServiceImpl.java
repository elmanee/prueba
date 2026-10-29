package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.dto.PagoServicioRequestDTO;
import com.proyecto.servicios.dto.PagoServicioResponseDTO;
import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.entity.EstadoTransaccion;
import com.proyecto.servicios.entity.TransaccionServicio;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.exception.AccesoDenegadoException;
import com.proyecto.servicios.exception.CuentaInactivaException;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.IntegracionExternaException;
import com.proyecto.servicios.exception.ProductoInvalidoException;
import com.proyecto.servicios.exception.SaldoInsuficienteException;
import com.proyecto.servicios.exception.TransaccionDuplicadaException;
import com.proyecto.servicios.model.gestopago.GestoPagoTxRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoTxResponse;
import com.proyecto.servicios.repository.CuentaRepository;
import com.proyecto.servicios.repository.TransaccionServicioRepository;
import com.proyecto.servicios.repository.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.service.PagoServicioService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PagoServicioServiceImpl implements PagoServicioService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CuentaRepository cuentaRepository;
    private final TransaccionServicioRepository transaccionRepository;
    private final GestoPagoProductoRepository productoRepository;
    private final GestoPagoTxClient gestoPagoTxClient;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.service.id-distribuidor:83}")
    private Integer idDistribuidor;

    @Value("${gestopago.service.codigo-dispositivo:GPS83-TPV-17}")
    private String codigoDispositivo;

    @Override
    @Transactional
    public PagoServicioResponseDTO procesarPago(PagoServicioRequestDTO request) {
        log.info("Iniciando procesamiento de pago de servicio. Cuenta={}, Producto={}, Monto={}",
                request.getNumeroCuenta(), request.getIdProducto(), request.getMonto());

        // 1. Validación de Idempotencia y Duplicados por Clave Única
        String referenciaUnica = (request.getReferenciaCliente() != null && !request.getReferenciaCliente().isBlank())
                ? request.getReferenciaCliente().trim()
                : UUID.randomUUID().toString();

        if (transaccionRepository.existsByReferencia(referenciaUnica)) {
            log.warn("Transacción duplicada detectada con referencia={}", referenciaUnica);
            throw new TransaccionDuplicadaException(
                    "Ya existe una transacción procesada o en curso con la referencia proporcionada: " + referenciaUnica
            );
        }

        // 2. Validación de Catálogo de Productos y Restricciones de Servicio y Monto
        GestoPagoProducto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new ProductoInvalidoException(
                        "El producto con ID '" + request.getIdProducto() + "' no existe en el catálogo de servicios."
                ));

        if (producto.getDisponible() != null && !producto.getDisponible()) {
            throw new ProductoInvalidoException(
                    "El producto '" + producto.getNombre() + "' (" + request.getIdProducto() + ") no se encuentra disponible actualmente."
            );
        }

        // Validar correspondencia estricta entre el ID del servicio y el producto seleccionado
        if (request.getIdServicio() != null && !request.getIdServicio().isBlank()) {
            String servReq = request.getIdServicio().trim().toUpperCase()
                    .replace("SERV_", "")
                    .replace("SERVICIO_", "")
                    .replace("¡", "")
                    .replace("!", "");
            String cat = (producto.getCategoria() != null)
                    ? producto.getCategoria().trim().toUpperCase().replace("¡", "").replace("!", "")
                    : "";
            String nom = (producto.getNombre() != null) ? producto.getNombre().trim().toUpperCase() : "";

            boolean coincideCategoria = !cat.isBlank() && (cat.equals(servReq) || cat.contains(servReq) || servReq.contains(cat));
            boolean coincideNombre = !nom.isBlank() && (nom.contains(servReq) || servReq.contains(nom));

            if (!coincideCategoria && !coincideNombre) {
                throw new ProductoInvalidoException(String.format(
                        "El ID de servicio '%s' no corresponde al producto seleccionado '%s' (Servicio/Categoría esperada: '%s')",
                        request.getIdServicio(), producto.getNombre(), producto.getCategoria()
                ));
            }
        }

        // Validar si el producto tiene un precio fijo configurado en el catálogo
        if (producto.getPrecio() != null && !producto.getPrecio().isBlank()) {
            try {
                BigDecimal precioCatalogo = new BigDecimal(producto.getPrecio().trim());
                if (precioCatalogo.compareTo(BigDecimal.ZERO) > 0 && precioCatalogo.compareTo(request.getMonto()) != 0) {
                    throw new ProductoInvalidoException(
                            String.format("El monto enviado (%s MXN) no coincide con el valor requerido del producto (%s MXN)",
                                    request.getMonto(), precioCatalogo)
                    );
                }
            } catch (NumberFormatException ignored) {
                // Si el precio en catálogo es variable o rango, se respeta la validación numérica del DTO
            }
        }

        // 3. Verificación de Identidad del Cliente, Autorización y Estado de la Cuenta
        Cuenta cuenta = cuentaRepository.findByNumeroCuentaWithLock(request.getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(
                        "No se encontró la cuenta con el número: " + request.getNumeroCuenta()
                ));

        if (cuenta.getActivo() == null || !cuenta.getActivo()) {
            throw new CuentaInactivaException("La cuenta " + request.getNumeroCuenta() + " se encuentra inactiva.");
        }

        if (cuenta.getCliente() != null && (cuenta.getCliente().getActivo() == null || !cuenta.getCliente().getActivo())) {
            throw new CuentaInactivaException("El cliente titular de la cuenta " + request.getNumeroCuenta() + " se encuentra inactivo.");
        }

        validarAutorizacionCliente(cuenta);

        // 4. Prevención de Doble Clic / Reintentos Inmediatos (Ventana de tiempo de 60 segundos)
        LocalDateTime ventanaLimite = LocalDateTime.now().minusMinutes(1);
        boolean duplicadaReciente = transaccionRepository
                .existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
                        cuenta,
                        request.getIdProducto(),
                        request.getReferenciaServicio(),
                        ventanaLimite,
                        EstadoTransaccion.FALLIDA
                );

        if (duplicadaReciente) {
            log.warn("Operación idéntica reciente detectada para cuenta={}, producto={}, ref={}",
                    cuenta.getNumeroCuenta(), request.getIdProducto(), request.getReferenciaServicio());
            throw new TransaccionDuplicadaException(
                    "Ya existe una transacción idéntica en proceso o recientemente completada para el servicio "
                            + request.getReferenciaServicio() + ". Por favor espere un momento antes de reintentar."
            );
        }

        // 5. Verificación de Saldo y Débito Preventivo Atómico (Estado PENDIENTE)
        BigDecimal saldoActual = cuenta.getSaldo() != null ? cuenta.getSaldo() : BigDecimal.ZERO;
        if (saldoActual.compareTo(request.getMonto()) < 0) {
            log.warn("Saldo insuficiente en cuenta {}. Saldo disponible={}, Requerido={}",
                    cuenta.getNumeroCuenta(), saldoActual, request.getMonto());
            throw new SaldoInsuficienteException(
                    String.format("Saldo insuficiente. Disponible: %s MXN, Requerido: %s MXN", saldoActual, request.getMonto())
            );
        }

        BigDecimal saldoRestante = saldoActual.subtract(request.getMonto());
        cuenta.setSaldo(saldoRestante);
        cuentaRepository.save(cuenta);

        TransaccionServicio transaccion = TransaccionServicio.builder()
                .referencia(referenciaUnica)
                .cuenta(cuenta)
                .idServicio(request.getIdServicio())
                .idProducto(request.getIdProducto())
                .numeroReferenciaServicio(request.getReferenciaServicio())
                .monto(request.getMonto())
                .estado(EstadoTransaccion.PENDIENTE)
                .mensajeRespuesta("Transacción iniciada - pendiente de confirmación de proveedor")
                .fechaCreacion(LocalDateTime.now())
                .build();

        transaccion = transaccionRepository.saveAndFlush(transaccion);
        log.info("Registro de auditoría persistido con estado PENDIENTE. ID={}, Referencia={}",
                transaccion.getId(), referenciaUnica);

        // 6. Consumo Externo de GestoPago con Manejo de Errores y Timeouts
        String token = resolverToken();

        GestoPagoTxRequest txRequest = GestoPagoTxRequest.builder()
                .idDistribuidor(idDistribuidor)
                .codigoDispositivo(codigoDispositivo)
                .idServicio(request.getIdServicio())
                .idProducto(request.getIdProducto())
                .referencia(request.getReferenciaServicio())
                .monto(request.getMonto())
                .build();

        try {
            GestoPagoTxResponse response = gestoPagoTxClient.sendTx(BEARER_PREFIX + token, txRequest);

            if (response != null && response.esExitosa()) {
                // Caso Exitoso
                String folioProveedor = response.obtenerFolioFinal();
                transaccion.setEstado(EstadoTransaccion.EXITOSA);
                transaccion.setFolioAutorizacionProveedor(folioProveedor);
                transaccion.setMensajeRespuesta(response.obtenerMensajeFinal());
                transaccionRepository.save(transaccion);

                log.info("Pago procesado exitosamente. Referencia={}, FolioProveedor={}",
                        referenciaUnica, folioProveedor);

                return PagoServicioResponseDTO.builder()
                        .folio(referenciaUnica)
                        .estatus(EstadoTransaccion.EXITOSA)
                        .mensaje(transaccion.getMensajeRespuesta())
                        .idServicio(request.getIdServicio())
                        .idProducto(request.getIdProducto())
                        .monto(request.getMonto())
                        .saldoActualizado(cuenta.getSaldo())
                        .fechaOperacion(LocalDateTime.now())
                        .folioProveedor(folioProveedor)
                        .build();
            } else {
                // Rechazo por parte de GestoPago
                String mensajeError = (response != null)
                        ? response.obtenerMensajeFinal()
                        : "Transacción rechazada por el proveedor";

                log.warn("Transacción rechazada por GestoPago. Motivo={}", mensajeError);

                transaccion.setEstado(EstadoTransaccion.FALLIDA);
                transaccion.setMensajeRespuesta(mensajeError);
                if (response != null) {
                    transaccion.setFolioAutorizacionProveedor(response.obtenerFolioFinal());
                }
                transaccionRepository.save(transaccion);

                // Reverso del saldo al cliente
                cuenta.setSaldo(saldoActual);
                cuentaRepository.save(cuenta);

                return PagoServicioResponseDTO.builder()
                        .folio(referenciaUnica)
                        .estatus(EstadoTransaccion.FALLIDA)
                        .mensaje(mensajeError)
                        .idServicio(request.getIdServicio())
                        .idProducto(request.getIdProducto())
                        .monto(request.getMonto())
                        .saldoActualizado(cuenta.getSaldo())
                        .fechaOperacion(LocalDateTime.now())
                        .folioProveedor(transaccion.getFolioAutorizacionProveedor())
                        .build();
            }

        } catch (FeignException.GatewayTimeout | feign.RetryableException ex) {
            // Caso TIMEOUT o error de red: se mantiene en TIMEOUT sin reversar el saldo para no duplicar pagos
            log.error("Timeout de comunicación con GestoPago para referencia={}. {}", referenciaUnica, ex.getMessage());
            transaccion.setEstado(EstadoTransaccion.TIMEOUT);
            transaccion.setMensajeRespuesta("Timeout con el proveedor externo. La transacción está en revisión y conciliación.");
            transaccionRepository.save(transaccion);

            return PagoServicioResponseDTO.builder()
                    .folio(referenciaUnica)
                    .estatus(EstadoTransaccion.TIMEOUT)
                    .mensaje("El proveedor no respondió a tiempo. La transacción se encuentra en estado de verificación para evitar doble cargo.")
                    .idServicio(request.getIdServicio())
                    .idProducto(request.getIdProducto())
                    .monto(request.getMonto())
                    .saldoActualizado(cuenta.getSaldo())
                    .fechaOperacion(LocalDateTime.now())
                    .build();

        } catch (Exception ex) {
            // Error en la invocación externa: marcar como FALLIDA y reversar saldo
            log.error("Fallo inesperado al conectar con GestoPago para referencia={}: {}", referenciaUnica, ex.getMessage());
            transaccion.setEstado(EstadoTransaccion.FALLIDA);
            transaccion.setMensajeRespuesta("Error de integración: " + ex.getMessage());
            transaccionRepository.save(transaccion);

            // Reverso de saldo
            cuenta.setSaldo(saldoActual);
            cuentaRepository.save(cuenta);

            return PagoServicioResponseDTO.builder()
                    .folio(referenciaUnica)
                    .estatus(EstadoTransaccion.FALLIDA)
                    .mensaje("No fue posible completar la operación con el proveedor externo: " + ex.getMessage())
                    .idServicio(request.getIdServicio())
                    .idProducto(request.getIdProducto())
                    .monto(request.getMonto())
                    .saldoActualizado(cuenta.getSaldo())
                    .fechaOperacion(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagoServicioResponseDTO consultarTransaccion(String referencia) {
        TransaccionServicio tx = transaccionRepository.findByReferencia(referencia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró transacción con la referencia: " + referencia));

        return PagoServicioResponseDTO.builder()
                .folio(tx.getReferencia())
                .estatus(tx.getEstado())
                .mensaje(tx.getMensajeRespuesta())
                .idServicio(tx.getIdServicio())
                .idProducto(tx.getIdProducto())
                .monto(tx.getMonto())
                .saldoActualizado(tx.getCuenta() != null ? tx.getCuenta().getSaldo() : null)
                .fechaOperacion(tx.getFechaCreacion())
                .folioProveedor(tx.getFolioAutorizacionProveedor())
                .build();
    }

    @Override
    @Transactional
    public PagoServicioResponseDTO conciliarTransaccion(String referencia, boolean confirmarExitosa, String folioProveedor) {
        TransaccionServicio tx = transaccionRepository.findByReferencia(referencia)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró transacción con la referencia: " + referencia));

        if (tx.getEstado() != EstadoTransaccion.TIMEOUT && tx.getEstado() != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden conciliar transacciones en estado TIMEOUT o PENDIENTE. Estado actual: " + tx.getEstado());
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuentaWithLock(tx.getCuenta().getNumeroCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException("No se encontró la cuenta vinculada"));

        if (confirmarExitosa) {
            tx.setEstado(EstadoTransaccion.EXITOSA);
            tx.setFolioAutorizacionProveedor(folioProveedor != null ? folioProveedor : "CONCILIADO-MANUAL");
            tx.setMensajeRespuesta("Transacción confirmada exitosamente mediante conciliación");
            log.info("Transacción {} conciliada exitosamente", referencia);
        } else {
            tx.setEstado(EstadoTransaccion.FALLIDA);
            tx.setMensajeRespuesta("Transacción conciliada como FALLIDA. Saldo reembolsado al cliente.");
            // Reembolsar saldo retenido
            cuenta.setSaldo(cuenta.getSaldo().add(tx.getMonto()));
            cuentaRepository.save(cuenta);
            log.info("Transacción {} conciliada como FALLIDA. Reembolsados {} MXN a cuenta {}",
                    referencia, tx.getMonto(), cuenta.getNumeroCuenta());
        }

        transaccionRepository.save(tx);

        return PagoServicioResponseDTO.builder()
                .folio(tx.getReferencia())
                .estatus(tx.getEstado())
                .mensaje(tx.getMensajeRespuesta())
                .idServicio(tx.getIdServicio())
                .idProducto(tx.getIdProducto())
                .monto(tx.getMonto())
                .saldoActualizado(cuenta.getSaldo())
                .fechaOperacion(LocalDateTime.now())
                .folioProveedor(tx.getFolioAutorizacionProveedor())
                .build();
    }

    private void validarAutorizacionCliente(Cuenta cuenta) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String usuarioAutenticado = auth.getName();
            if (cuenta.getCliente() != null && cuenta.getCliente().getUsuario() != null) {
                String correoTitular = cuenta.getCliente().getUsuario().getCorreo();
                if (correoTitular != null && !correoTitular.equalsIgnoreCase(usuarioAutenticado)) {
                    log.warn("Intento de operación no autorizada: Usuario '{}' no tiene permisos sobre la cuenta '{}' del titular '{}'",
                            usuarioAutenticado, cuenta.getNumeroCuenta(), correoTitular);
                    throw new AccesoDenegadoException(
                            "No tiene autorización para realizar operaciones en la cuenta: " + cuenta.getNumeroCuenta()
                    );
                }
            }
        }
    }

    private String resolverToken() {
        return gestoPagoTokenService
                .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                .map(GestoPagoToken::getToken)
                .orElseThrow(() -> new IntegracionExternaException(
                        "No se cuenta con un token activo de GestoPago. Espere la renovación automática.",
                        HttpStatus.SERVICE_UNAVAILABLE.value()
                ));
    }
}
