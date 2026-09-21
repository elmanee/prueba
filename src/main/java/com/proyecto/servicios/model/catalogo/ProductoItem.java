package com.proyecto.servicios.model.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Representa un producto individual dentro de la lista
 * retornada por el servicio externo getProductList.do.
 * Los campos desconocidos se ignoran para mayor compatibilidad.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoItem {

    @JsonProperty("idproducto")
    private String id;

    @JsonProperty("producto")
    private String nombre;

    @JsonProperty("legend")
    private String descripcion;

    @JsonProperty("precio")
    private String precio;

    @JsonProperty("servicio")
    private String categoria;

    private Boolean disponible;
}
