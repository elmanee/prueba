package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.IntegracionExternaException;
import com.proyecto.servicios.model.catalogo.ProductListResponse;
import com.proyecto.servicios.service.GestoPagoTokenService;
import com.proyecto.servicios.service.ProductoService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de catálogo de productos GestoPago.
 * <p>
 * El Bearer Token se obtiene dinámicamente desde {@link GestoPagoTokenService},
 * el cual lo renueva automáticamente via scheduler. Nunca se hardcodea en el código fuente.
 * </p>
 */
@Service
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final GestoPagoProductClient gestoPagoProductClient;
    private final GestoPagoTokenService gestoPagoTokenService;

    @Value("${gestopago.service.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.service.codigo-dispositivo}")
    private String codigoDispositivo;

    public ProductoServiceImpl(GestoPagoProductClient gestoPagoProductClient,
                               GestoPagoTokenService gestoPagoTokenService) {
        this.gestoPagoProductClient = gestoPagoProductClient;
        this.gestoPagoTokenService = gestoPagoTokenService;
    }

    /**
     * Obtiene la lista de productos del servicio externo GestoPago.
     * <p>
     * Registra en log el inicio y fin de la invocación.
     * El token nunca se expone en los registros de log.
     * </p>
     *
     * @return {@link ProductListResponse} con los productos disponibles
     * @throws IntegracionExternaException si no hay token activo, error de comunicación,
     *                                     autenticación o timeout
     */
    @Override
    public ProductListResponse obtenerListaProductos() {
        log.info("Iniciando consulta de lista de productos GestoPago. distribuidor={}",
                idDistribuidor);

        String token = resolverToken();

        try {
            ProductListResponse response = gestoPagoProductClient.getProductList(
                    BEARER_PREFIX + token
            );

            if (response == null) {
                log.error("El servicio externo GestoPago retornó una respuesta nula en getProductList.");
                throw new IntegracionExternaException(
                        "El servicio externo no retornó contenido.",
                        HttpStatus.BAD_GATEWAY.value()
                );
            }

            log.info("Consulta de lista de productos GestoPago finalizada. totalProductos={}",
                    response.getDatos() != null ? response.getDatos().size() : 0);
            return response;

        } catch (FeignException.Unauthorized ex) {
            log.error("Error de autenticación en getProductList GestoPago. status=401");
            throw new IntegracionExternaException(
                    "Token inválido o expirado para el servicio de productos GestoPago.",
                    HttpStatus.UNAUTHORIZED.value(),
                    ex
            );
        } catch (FeignException.GatewayTimeout | feign.RetryableException ex) {
            log.error("Timeout en getProductList GestoPago: {}", ex.getMessage());
            throw new IntegracionExternaException(
                    "El servicio de productos GestoPago no respondió en el tiempo esperado.",
                    HttpStatus.GATEWAY_TIMEOUT.value(),
                    ex
            );
        } catch (FeignException ex) {
            log.error("Error de comunicación en getProductList GestoPago. status={}, detalle={}", ex.status(), ex.getMessage());
            throw new IntegracionExternaException(
                    "Error al comunicarse con el servicio de productos GestoPago: " + ex.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    ex
            );
        }
    }

    /**
     * Resuelve el token activo desde {@link GestoPagoTokenService}.
     * Si no existe token activo lanza {@link IntegracionExternaException}.
     */
    private String resolverToken() {
        return gestoPagoTokenService
                .obtenerTokenActivo(idDistribuidor, codigoDispositivo)
                .map(GestoPagoToken::getToken)
                .orElseThrow(() -> {
                    log.error("No hay token GestoPago activo para distribuidor={}", idDistribuidor);
                    return new IntegracionExternaException(
                            "No hay token de autenticación activo. Por favor, espere la renovación automática.",
                            HttpStatus.SERVICE_UNAVAILABLE.value()
                    );
                });
    }
}
