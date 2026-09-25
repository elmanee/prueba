package com.proyecto.servicios.client;

import com.proyecto.servicios.exception.IntegracionExternaException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Intercepta los errores HTTP devueltos por GestoPago a través de Feign.
 * Traduce los códigos de estado HTTP a excepciones controladas por la aplicación.
 */
@Component
@Slf4j
public class GestoPagoErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        log.error("Error en GestoPago - Método: {}, Status: {}", methodKey, status);

        return switch (status) {
            case 400 -> new IntegracionExternaException("Petición incorrecta hacia GestoPago (Bad Request)", HttpStatus.BAD_REQUEST.value());
            case 401 -> new IntegracionExternaException("Token o credenciales inválidas para GestoPago", HttpStatus.UNAUTHORIZED.value());
            case 403 -> new IntegracionExternaException("Acceso denegado en GestoPago (Forbidden)", HttpStatus.FORBIDDEN.value());
            case 404 -> new IntegracionExternaException("Recurso no encontrado en GestoPago", HttpStatus.NOT_FOUND.value());
            case 500 -> new IntegracionExternaException("Error interno en el servidor de GestoPago", HttpStatus.INTERNAL_SERVER_ERROR.value());
            case 503 -> new IntegracionExternaException("Servicio de GestoPago temporalmente no disponible", HttpStatus.SERVICE_UNAVAILABLE.value());
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
