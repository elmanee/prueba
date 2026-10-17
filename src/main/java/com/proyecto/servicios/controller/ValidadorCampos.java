package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ValidadorCampos {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> dtoValidation(MethodArgumentNotValidException ex) {
        Map<String, String> listError = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            listError.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        GenericResponse genericResponse = new GenericResponse();
        genericResponse.setCodigo(1);
        genericResponse.setMensaje("Datos no validos: " + listError);
        return new ResponseEntity<>(genericResponse, HttpStatus.BAD_REQUEST);
    }
}
