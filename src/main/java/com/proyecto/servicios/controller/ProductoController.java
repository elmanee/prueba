package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.catalogo.ProductListResponse;
import com.proyecto.servicios.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la consulta de catálogo de productos.
 * Expone el endpoint GET /productos que delega al servicio externo
 * a través de {@link ProductoService}.
 */
@RestController
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * Obtiene la lista completa de productos del catálogo externo.
     *
     * @return {@link ProductListResponse} con código 200 si es exitoso
     */
    @GetMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductListResponse> obtenerProductos() {
        return new ResponseEntity<>(productoService.obtenerListaProductos(), HttpStatus.OK);
    }
}
