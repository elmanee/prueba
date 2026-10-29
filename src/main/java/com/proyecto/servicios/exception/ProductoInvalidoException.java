package com.proyecto.servicios.exception;

public class ProductoInvalidoException extends RuntimeException {
    public ProductoInvalidoException(String message) {
        super(message);
    }
}
