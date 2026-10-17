package com.proyecto.servicios.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * <p>Centraliza el tratamiento de errores para toda la aplicación,
 * evitando duplicar bloques try-catch en los controladores.
 * Nunca expone trazas técnicas, SQL ni detalles internos al cliente.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Errores de validación Bean Validation (@Valid)
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                detalle
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // -------------------------------------------------------------------------
    // Recursos no encontrados
    // -------------------------------------------------------------------------

    @ExceptionHandler({ClienteNoEncontradoException.class, UsuarioNoEncontradoException.class,
            CuentaNoEncontradaException.class})
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // -------------------------------------------------------------------------
    // Conflictos de datos duplicados
    // -------------------------------------------------------------------------

    @ExceptionHandler({CurpDuplicadaException.class, RfcDuplicadaException.class,
            ClienteYaRegistradoException.class})
    public ResponseEntity<ErrorResponse> handleConflicto(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflicto de datos",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // -------------------------------------------------------------------------
    // Errores de autenticación / autorización
    // -------------------------------------------------------------------------

    @ExceptionHandler({UsuarioInactivoException.class, CredencialesInvalidasException.class,
            org.springframework.security.authentication.BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuth(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "No autorizado",
                "Credenciales inválidas o usuario inactivo"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // -------------------------------------------------------------------------
    // Errores de negocio (argumentos inválidos)
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud inválida",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // -------------------------------------------------------------------------
    // Errores de integración externa
    // -------------------------------------------------------------------------

    @ExceptionHandler(IntegracionExternaException.class)
    public ResponseEntity<ErrorResponse> handleIntegracionExterna(IntegracionExternaException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getCodigoEstado(),
                "Error de integración",
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getCodigoEstado()).body(error);
    }

    // -------------------------------------------------------------------------
    // Ruta no encontrada (404) — endpoint inexistente
    // -------------------------------------------------------------------------

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaNoEncontrada(NoResourceFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Ruta no encontrada",
                "El endpoint solicitado no existe: " + ex.getResourcePath()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // -------------------------------------------------------------------------
    // Método HTTP no permitido (405)
    // -------------------------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Método no permitido",
                "El método " + ex.getMethod() + " no está soportado para esta ruta. "
                        + "Métodos permitidos: " + ex.getSupportedHttpMethods()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    // -------------------------------------------------------------------------
    // Error genérico no controlado (última línea de defensa)
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ocurrió un error inesperado. Por favor contacte al administrador."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
