package com.proyecto.servicios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.servicios.dto.PagoServicioRequestDTO;
import com.proyecto.servicios.dto.PagoServicioResponseDTO;
import com.proyecto.servicios.entity.EstadoTransaccion;
import com.proyecto.servicios.service.PagoServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioControllerTest {

    @Mock
    private PagoServicioService pagoServicioService;

    @InjectMocks
    private PagoServicioController pagoServicioController;

    private PagoServicioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = PagoServicioRequestDTO.builder()
                .numeroCuenta("1234567890")
                .idServicio("TELMEX")
                .idProducto("PROD-01")
                .referenciaServicio("5512345678")
                .monto(new BigDecimal("150.00"))
                .referenciaCliente("REF-TEST-001")
                .build();
    }

    @Test
    @DisplayName("Debe responder HTTP 201 CREATED cuando la transacción es EXITOSA")
    void pagarServicio_exitoso_retorna201() {
        PagoServicioResponseDTO responseDTO = PagoServicioResponseDTO.builder()
                .folio("REF-TEST-001")
                .estatus(EstadoTransaccion.EXITOSA)
                .mensaje("Transacción exitosa")
                .monto(new BigDecimal("150.00"))
                .folioProveedor("FOLIO-123")
                .fechaOperacion(LocalDateTime.now())
                .build();

        when(pagoServicioService.procesarPago(any(PagoServicioRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<PagoServicioResponseDTO> response = pagoServicioController.pagarServicio(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEstatus()).isEqualTo(EstadoTransaccion.EXITOSA);
    }

    @Test
    @DisplayName("Debe responder HTTP 202 ACCEPTED cuando ocurre TIMEOUT con el proveedor")
    void pagarServicio_timeout_retorna202() {
        PagoServicioResponseDTO responseDTO = PagoServicioResponseDTO.builder()
                .folio("REF-TEST-001")
                .estatus(EstadoTransaccion.TIMEOUT)
                .mensaje("El proveedor no respondió a tiempo")
                .monto(new BigDecimal("150.00"))
                .fechaOperacion(LocalDateTime.now())
                .build();

        when(pagoServicioService.procesarPago(any(PagoServicioRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<PagoServicioResponseDTO> response = pagoServicioController.pagarServicio(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEstatus()).isEqualTo(EstadoTransaccion.TIMEOUT);
    }

    @Test
    @DisplayName("Debe responder HTTP 422 UNPROCESSABLE_ENTITY cuando la transacción es rechazada (FALLIDA)")
    void pagarServicio_fallida_retorna422() {
        PagoServicioResponseDTO responseDTO = PagoServicioResponseDTO.builder()
                .folio("REF-TEST-001")
                .estatus(EstadoTransaccion.FALLIDA)
                .mensaje("Transacción rechazada por el proveedor")
                .monto(new BigDecimal("150.00"))
                .fechaOperacion(LocalDateTime.now())
                .build();

        when(pagoServicioService.procesarPago(any(PagoServicioRequestDTO.class))).thenReturn(responseDTO);

        ResponseEntity<PagoServicioResponseDTO> response = pagoServicioController.pagarServicio(requestDTO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEstatus()).isEqualTo(EstadoTransaccion.FALLIDA);
    }

    @Test
    @DisplayName("Debe consultar transacción existente por referencia vía GET")
    void consultarTransaccion_exitoso() {
        PagoServicioResponseDTO responseDTO = PagoServicioResponseDTO.builder()
                .folio("REF-TEST-001")
                .estatus(EstadoTransaccion.TIMEOUT)
                .monto(new BigDecimal("150.00"))
                .build();

        when(pagoServicioService.consultarTransaccion("REF-TEST-001")).thenReturn(responseDTO);

        ResponseEntity<PagoServicioResponseDTO> response = pagoServicioController.consultarTransaccion("REF-TEST-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFolio()).isEqualTo("REF-TEST-001");
    }

    @Test
    @DisplayName("Debe conciliar transacción vía POST")
    void conciliarTransaccion_exitoso() {
        PagoServicioResponseDTO responseDTO = PagoServicioResponseDTO.builder()
                .folio("REF-TEST-001")
                .estatus(EstadoTransaccion.EXITOSA)
                .folioProveedor("PROV-CONFIRMADO")
                .build();

        when(pagoServicioService.conciliarTransaccion("REF-TEST-001", true, "PROV-CONFIRMADO"))
                .thenReturn(responseDTO);

        ResponseEntity<PagoServicioResponseDTO> response =
                pagoServicioController.conciliarTransaccion("REF-TEST-001", true, "PROV-CONFIRMADO");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEstatus()).isEqualTo(EstadoTransaccion.EXITOSA);
    }
}

