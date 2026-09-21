package com.proyecto.servicios.exception;

/**
 * Excepción personalizada para errores en la integración con servicios externos.
 * Encapsula los distintos tipos de fallo (comunicación, autenticación, timeout)
 * sin exponer información sensible hacia el exterior.
 */
public class IntegracionExternaException extends RuntimeException {

    private final int codigoEstado;

    public IntegracionExternaException(String mensaje, int codigoEstado) {
        super(mensaje);
        this.codigoEstado = codigoEstado;
    }

    public IntegracionExternaException(String mensaje, int codigoEstado, Throwable causa) {
        super(mensaje, causa);
        this.codigoEstado = codigoEstado;
    }

    public int getCodigoEstado() {
        return codigoEstado;
    }
}
