package com.proyecto.servicios.exception;

import com.proyecto.servicios.model.GenericResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;

/**
 * Manejador global de excepciones.
 * Centraliza el tratamiento de errores de integración, timeout y
 * autenticación, devolviendo respuestas homogéneas al cliente.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores propios de integración con servicios externos.
     */
    @ExceptionHandler(IntegracionExternaException.class)
    public ResponseEntity<GenericResponse> handleIntegracionExterna(IntegracionExternaException ex) {
        log.error("Error de integración con servicio externo - código: {}, mensaje: {}",
                ex.getCodigoEstado(), ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(ex.getCodigoEstado());
        response.setMensaje(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getCodigoEstado()));
    }

    /**
     * Maneja errores de comunicación Feign (connection refused, reset, etc.).
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<GenericResponse> handleFeignException(FeignException ex) {
        log.error("Error de comunicación con servicio externo - status: {}", ex.status());
        GenericResponse response = new GenericResponse();

        if (ex.status() == HttpStatus.UNAUTHORIZED.value()) {
            response.setCodigo(HttpStatus.UNAUTHORIZED.value());
            response.setMensaje("Error de autenticación con el servicio externo.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.setCodigo(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setMensaje("El servicio externo no está disponible en este momento.");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Maneja timeouts de conexión o lectura.
     */
    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<GenericResponse> handleTimeout(SocketTimeoutException ex) {
        log.error("Timeout en llamada a servicio externo: {}", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setCodigo(HttpStatus.GATEWAY_TIMEOUT.value());
        response.setMensaje("El servicio externo no respondió en el tiempo esperado.");
        return new ResponseEntity<>(response, HttpStatus.GATEWAY_TIMEOUT);
    }
}
