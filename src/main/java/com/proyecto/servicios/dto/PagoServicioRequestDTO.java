package com.proyecto.servicios.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoServicioRequestDTO {

    @NotBlank(message = "El número de cuenta de origen es obligatorio")
    @Size(min = 5, max = 20, message = "El número de cuenta debe tener entre 5 y 20 caracteres")
    private String numeroCuenta;

    @NotBlank(message = "El identificador del servicio es obligatorio")
    @Size(max = 50, message = "El id del servicio no puede exceder 50 caracteres")
    private String idServicio;

    @NotBlank(message = "El identificador del producto es obligatorio")
    @Size(max = 50, message = "El id del producto no puede exceder 50 caracteres")
    private String idProducto;

    @NotBlank(message = "La referencia del servicio a pagar es obligatoria (teléfono, contrato o servicio)")
    @Size(min = 5, max = 50, message = "La referencia del servicio debe tener entre 5 y 50 caracteres")
    private String referenciaServicio;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser estrictamente mayor a 0")
    @DecimalMin(value = "1.00", message = "El monto mínimo permitido es 1.00")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 2 decimales")
    private BigDecimal monto;

    @Size(max = 100, message = "La referencia única de idempotencia no puede superar los 100 caracteres")
    private String referenciaCliente;
}
