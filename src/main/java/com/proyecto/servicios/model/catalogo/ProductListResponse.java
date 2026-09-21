package com.proyecto.servicios.model.catalogo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Respuesta completa del servicio externo getProductList.do.
 * Envuelve la lista de productos junto con metadatos de la operación.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductListResponse {

    @JsonProperty("mensaje")
    private MensajeGestoPago mensajeObj;

    @JsonProperty("productos")
    private List<ProductoItem> datos;

    /**
     * Compatibilidad con la estructura anterior esperada por el controller
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Integer getCodigo() {
        try {
            return mensajeObj != null && mensajeObj.getCodigo() != null ? Integer.parseInt(mensajeObj.getCodigo()) : 0;
        } catch(NumberFormatException e) {
            return 0;
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getMensaje() {
        return mensajeObj != null ? mensajeObj.getTexto() : "";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MensajeGestoPago {
        private String codigo;
        private String texto;
    }
}
