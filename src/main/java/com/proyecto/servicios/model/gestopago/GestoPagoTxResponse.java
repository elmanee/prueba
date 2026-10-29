package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "AUTORIZACION")
public class GestoPagoTxResponse {

    @JsonProperty("codigo")
    @JacksonXmlProperty(localName = "codigo")
    private Integer codigo;

    @JsonProperty("CODIGO_MENSAJE")
    @JacksonXmlProperty(localName = "CODIGO_MENSAJE")
    private String codigoMensaje;

    @JsonProperty("mensaje")
    @JacksonXmlProperty(localName = "mensaje")
    private String mensaje;

    @JsonProperty("MENSAJE")
    @JacksonXmlProperty(localName = "MENSAJE")
    private String mensajeGestoPago;

    @JsonProperty("folio")
    @JacksonXmlProperty(localName = "folio")
    private String folio;

    @JsonProperty("autorizacion")
    @JacksonXmlProperty(localName = "autorizacion")
    private String autorizacion;

    @JsonProperty("NUM_AUTORIZACION")
    @JacksonXmlProperty(localName = "NUM_AUTORIZACION")
    private String numAutorizacion;

    @JsonProperty("status")
    @JacksonXmlProperty(localName = "status")
    private String status;

    public String obtenerFolioFinal() {
        if (folio != null && !folio.isBlank()) {
            return folio.trim();
        }
        if (autorizacion != null && !autorizacion.isBlank()) {
            return autorizacion.trim();
        }
        if (numAutorizacion != null && !numAutorizacion.isBlank() && !"-1".equals(numAutorizacion.trim())) {
            return numAutorizacion.trim();
        }
        return null;
    }

    public String obtenerMensajeFinal() {
        if (mensajeGestoPago != null && !mensajeGestoPago.isBlank()) {
            return mensajeGestoPago.trim();
        }
        if (mensaje != null && !mensaje.isBlank()) {
            return mensaje.trim();
        }
        return "Transacción rechazada por el proveedor";
    }

    public boolean esExitosa() {
        if (numAutorizacion != null && !numAutorizacion.isBlank()) {
            String num = numAutorizacion.trim();
            if (!"-1".equals(num) && !"0".equals(num)) {
                return true;
            }
        }
        if (codigoMensaje != null) {
            String cod = codigoMensaje.trim();
            if ("00".equals(cod) || "0".equals(cod)) {
                return true;
            }
        }
        if (codigo != null && (codigo == 0 || codigo == 200)) {
            return true;
        }
        if (status != null && ("SUCCESS".equalsIgnoreCase(status)
                || "EXITOSA".equalsIgnoreCase(status)
                || "OK".equalsIgnoreCase(status))) {
            return true;
        }
        return false;
    }
}
