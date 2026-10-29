package com.proyecto.servicios.model.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Respuesta del servicio externo getProductList.do de GestoPago.
 * Soporta deserialización tanto JSON como XML sin conflicto de propiedades Jackson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class ProductListResponse {

    @JsonProperty("codigo")
    @JacksonXmlProperty(localName = "codigo")
    private Integer codigo;

    @JsonProperty("mensaje")
    @JacksonXmlProperty(localName = "mensaje")
    private String mensaje;

    @JsonProperty("productos")
    @JacksonXmlElementWrapper(localName = "productos")
    @JacksonXmlProperty(localName = "producto")
    @Builder.Default
    private List<ProductoItem> datos = new ArrayList<>();
}
