package com.proyecto.servicios.service;

import com.proyecto.servicios.model.catalogo.ProductListResponse;

/**
 * Contrato del servicio de productos del catálogo externo.
 */
public interface ProductoService {

    /**
     * Consulta la lista de productos disponibles en el servicio externo.
     *
     * @return Respuesta con la lista de productos
     */
    ProductListResponse obtenerListaProductos();
}
