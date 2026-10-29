package com.proyecto.servicios.model.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un producto individual dentro de la lista
 * retornada por el servicio externo getProductList.do.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoItem {

    @JsonProperty("idproducto")
    @JacksonXmlProperty(localName = "idproducto")
    private String id;

    @JsonProperty("producto")
    @JacksonXmlProperty(localName = "producto")
    private String nombre;

    @JsonProperty("legend")
    @JacksonXmlProperty(localName = "legend")
    private String descripcion;

    @JsonProperty("precio")
    @JacksonXmlProperty(localName = "precio")
    private String precio;

    @JsonProperty("servicio")
    @JacksonXmlProperty(localName = "servicio")
    private String categoria;

    @JsonProperty("disponible")
    @JacksonXmlProperty(localName = "disponible")
    @Builder.Default
    private Boolean disponible = true;
}
