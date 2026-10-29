package com.proyecto.servicios.client;

import com.proyecto.servicios.model.gestopago.GestoPagoTxRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoTxResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente OpenFeign para procesar transacciones (pagos y recargas de servicios) en GestoPago.
 * Apunta al endpoint /sistema/service/sendTx.do utilizando la URL configurada en gestopago.service.url.
 * Emplea el GestoPagoErrorDecoder registrado en el contexto para mapear fallos HTTP.
 */
@FeignClient(
    name = "gestoPagoTx",
    url = "${gestopago.service.url}",
    configuration = GestoPagoErrorDecoder.class
)
public interface GestoPagoTxClient {

    /**
     * Envía una transacción de pago o recarga a GestoPago.
     *
     * @param authorization Header de autorización Bearer Token
     * @param request Payload de la transacción
     * @return Respuesta estructurada de GestoPago
     */
    @PostMapping(
        value = "/sistema/service/sendTx.do",
        consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "*/*"}
    )
    GestoPagoTxResponse sendTx(
        @RequestHeader("Authorization") String authorization,
        @RequestBody GestoPagoTxRequest request
    );
}
