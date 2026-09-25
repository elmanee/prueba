package com.proyecto.servicios.service.impl;

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
import org.springframework.cache.annotation.Cacheable;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.repository.gestopago.GestoPagoProductoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final GestoPagoProductoRepository productoRepository;

    @Value("${gestopago.service.id-distribuidor}")
    private Integer idDistribuidor;

    @Value("${gestopago.service.codigo-dispositivo}")
    private String codigoDispositivo;

    public ProductoServiceImpl(GestoPagoProductClient gestoPagoProductClient,
                               GestoPagoTokenService gestoPagoTokenService,
                               GestoPagoProductoRepository productoRepository) {
        this.gestoPagoProductClient = gestoPagoProductClient;
        this.gestoPagoTokenService = gestoPagoTokenService;
        this.productoRepository = productoRepository;
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
    @Cacheable(value = "productosGestoPago", unless = "#result == null")
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

            if (response.getDatos() != null && !response.getDatos().isEmpty()) {
                // 1. Limpiamos la tabla primero para evitar acumular basura o conflictos de actualización
                productoRepository.deleteAllInBatch();

                // 2. Mapeamos la lista completa. 
                // Usamos UUID.randomUUID() como ID primario porque si la API de GestoPago 
                // devuelve IDs nulos o duplicados (ej. idproducto="0" para todos), JPA sobrescribirá 
                // el mismo registro 901 veces y solo guardará 1.
                List<GestoPagoProducto> entidades = response.getDatos().stream()
                        .map(item -> GestoPagoProducto.builder()
                                .id(java.util.UUID.randomUUID().toString()) // ID único garantizado
                                .nombre(item.getNombre())
                                .descripcion(item.getDescripcion())
                                .precio(item.getPrecio())
                                .categoria(item.getCategoria())
                                .disponible(item.getDisponible() != null ? item.getDisponible() : true)
                                .fechaActualizacion(LocalDateTime.now())
                                .build())
                        .collect(Collectors.toList());
                
                // 3. Guardamos la lista masivamente
                productoRepository.saveAll(entidades);
                log.info("Persistidos {} productos en la base de datos PostgreSQL.", entidades.size());
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
