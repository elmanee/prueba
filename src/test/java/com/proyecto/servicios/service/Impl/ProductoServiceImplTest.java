package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductClient;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.IntegracionExternaException;
import com.proyecto.servicios.model.catalogo.ProductListResponse;
import com.proyecto.servicios.model.catalogo.ProductoItem;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para {@link ProductoServiceImpl}.
 * Se simulan respuestas exitosas y escenarios de error
 * sin levantar el contexto completo de Spring.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private GestoPagoProductClient gestoPagoProductClient;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private static final Integer ID_DISTRIBUIDOR = 83;
    private static final String CODIGO_DISPOSITIVO = "GPS83-TPV-17";
    private static final String TOKEN_ACTIVO = "token-jwt-activo-123";
    private static final String BEARER_TOKEN = "Bearer " + TOKEN_ACTIVO;

    private GestoPagoToken tokenMock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productoService, "idDistribuidor", ID_DISTRIBUIDOR);
        ReflectionTestUtils.setField(productoService, "codigoDispositivo", CODIGO_DISPOSITIVO);

        tokenMock = new GestoPagoToken();
        tokenMock.setToken(TOKEN_ACTIVO);
        tokenMock.setActivo(true);
        tokenMock.setIdDistribuidor(ID_DISTRIBUIDOR);
        tokenMock.setCodigoDispositivo(CODIGO_DISPOSITIVO);
    }

    // -------------------------------------------------------------------------
    // Escenarios exitosos
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe retornar lista de productos cuando el servicio responde correctamente")
    void obtenerListaProductos_exitoso() {
        ProductoItem item = new ProductoItem();
        item.setId("P001");
        item.setNombre("Tiempo Aire Telcel $10");
        item.setDisponible(true);

        ProductListResponse responseEsperado = new ProductListResponse();
        responseEsperado.setCodigo(0);
        responseEsperado.setMensaje("OK");
        responseEsperado.setDatos(List.of(item));

        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenReturn(responseEsperado);

        ProductListResponse resultado = productoService.obtenerListaProductos();

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCodigo()).isEqualTo(0);
        assertThat(resultado.getDatos()).hasSize(1);
        assertThat(resultado.getDatos().get(0).getNombre()).isEqualTo("Tiempo Aire Telcel $10");

        verify(gestoPagoTokenService, times(1)).obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO);
        verify(gestoPagoProductClient, times(1)).getProductList(BEARER_TOKEN);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando el servicio no tiene productos")
    void obtenerListaProductos_listaVacia() {
        ProductListResponse responseEsperado = new ProductListResponse();
        responseEsperado.setCodigo(0);
        responseEsperado.setMensaje("Sin productos");
        responseEsperado.setDatos(Collections.emptyList());

        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenReturn(responseEsperado);

        ProductListResponse resultado = productoService.obtenerListaProductos();

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDatos()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Escenarios de error — token
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IntegracionExternaException (503) cuando no hay token activo")
    void obtenerListaProductos_sinTokenActivo() {
        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtenerListaProductos())
                .isInstanceOf(IntegracionExternaException.class)
                .hasMessageContaining("No hay token de autenticación activo")
                .satisfies(ex -> assertThat(((IntegracionExternaException) ex).getCodigoEstado())
                        .isEqualTo(503));

        // El cliente Feign NO debe invocarse si no hay token
        verify(gestoPagoProductClient, never()).getProductList(anyString());
    }

    // -------------------------------------------------------------------------
    // Escenarios de error — respuesta del servicio
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Debe lanzar IntegracionExternaException (502) cuando el servicio retorna null")
    void obtenerListaProductos_respuestaNula() {
        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenReturn(null);

        assertThatThrownBy(() -> productoService.obtenerListaProductos())
                .isInstanceOf(IntegracionExternaException.class)
                .hasMessageContaining("no retornó contenido")
                .satisfies(ex -> assertThat(((IntegracionExternaException) ex).getCodigoEstado())
                        .isEqualTo(502));
    }

    @Test
    @DisplayName("Debe lanzar IntegracionExternaException (401) ante error de autenticación")
    void obtenerListaProductos_errorAutenticacion() {
        Request mockRequest = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        FeignException.Unauthorized unauthorizedException =
                new FeignException.Unauthorized("Unauthorized", mockRequest, null, null);

        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenThrow(unauthorizedException);

        assertThatThrownBy(() -> productoService.obtenerListaProductos())
                .isInstanceOf(IntegracionExternaException.class)
                .hasMessageContaining("Token inválido o expirado")
                .satisfies(ex -> assertThat(((IntegracionExternaException) ex).getCodigoEstado())
                        .isEqualTo(401));
    }

    @Test
    @DisplayName("Debe lanzar IntegracionExternaException (503) ante error de comunicación")
    void obtenerListaProductos_errorComunicacion() {
        Request mockRequest = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        FeignException.ServiceUnavailable serviceUnavailable =
                new FeignException.ServiceUnavailable("Service unavailable", mockRequest, null, null);

        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenThrow(serviceUnavailable);

        assertThatThrownBy(() -> productoService.obtenerListaProductos())
                .isInstanceOf(IntegracionExternaException.class)
                .hasMessageContaining("Error al comunicarse")
                .satisfies(ex -> assertThat(((IntegracionExternaException) ex).getCodigoEstado())
                        .isEqualTo(503));
    }

    @Test
    @DisplayName("Debe lanzar IntegracionExternaException (504) ante timeout")
    void obtenerListaProductos_timeout() {
        Request mockRequest = Request.create(
                Request.HttpMethod.GET,
                "/sistema/service/getProductList.do",
                Map.of(), null, StandardCharsets.UTF_8, null
        );
        FeignException.GatewayTimeout timeoutException =
                new FeignException.GatewayTimeout("Gateway Timeout", mockRequest, null, null);

        when(gestoPagoTokenService.obtenerTokenActivo(ID_DISTRIBUIDOR, CODIGO_DISPOSITIVO))
                .thenReturn(Optional.of(tokenMock));
        when(gestoPagoProductClient.getProductList(BEARER_TOKEN))
                .thenThrow(timeoutException);

        assertThatThrownBy(() -> productoService.obtenerListaProductos())
                .isInstanceOf(IntegracionExternaException.class)
                .hasMessageContaining("no respondió en el tiempo esperado")
                .satisfies(ex -> assertThat(((IntegracionExternaException) ex).getCodigoEstado())
                        .isEqualTo(504));
    }
}
