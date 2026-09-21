package com.proyecto.servicios.client;

import com.proyecto.servicios.model.catalogo.ProductListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente Feign para el servicio externo de catálogo de productos GestoPago.
 * La URL base se obtiene de la propiedad {@code gestopago.service.url}.
 * El nombre {@code gestoPagoProduct} corresponde al bloque de timeout configurado en
 * {@code spring.cloud.openfeign.client.config.gestoPagoProduct.*}.
 * El Bearer Token se inyecta dinámicamente desde {@link com.proyecto.servicios.service.GestoPagoTokenService},
 * nunca quedará hardcodeado en el código fuente.
 */
@FeignClient(name = "gestoPagoProduct", url = "${gestopago.service.url}")
public interface GestoPagoProductClient {

    /**
     * Obtiene la lista de productos del servicio externo GestoPago.
     *
     * @param authorization Header de autorización con formato {@code Bearer <token>}
     * @return Respuesta con la lista de productos
     */
    @GetMapping(value = "/sistema/service/getProductList.do", produces = MediaType.APPLICATION_JSON_VALUE)
    ProductListResponse getProductList(
            @RequestHeader("Authorization") String authorization
    );
}
