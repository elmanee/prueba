package com.proyecto.servicios.model.gestopago;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GestoPagoTxRequest {

    @JsonProperty("idDistribuidor")
    private Integer idDistribuidor;

    @JsonProperty("codigoDispositivo")
    private String codigoDispositivo;

    @JsonProperty("idServicio")
    private String idServicio;

    @JsonProperty("idProducto")
    private String idProducto;

    @JsonProperty("referencia")
    private String referencia;

    @JsonProperty("monto")
    private BigDecimal monto;

    @JsonProperty("token")
    private String token;
}
