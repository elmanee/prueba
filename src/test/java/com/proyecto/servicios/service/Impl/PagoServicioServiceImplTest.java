package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.client.GestoPagoTxClient;
import com.proyecto.servicios.dto.PagoServicioRequestDTO;
import com.proyecto.servicios.dto.PagoServicioResponseDTO;
import com.proyecto.servicios.entity.Cliente;
import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.entity.EstadoTransaccion;
import com.proyecto.servicios.entity.TransaccionServicio;
import com.proyecto.servicios.entity.Usuario;
import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import com.proyecto.servicios.entity.gestopago.GestoPagoToken;
import com.proyecto.servicios.exception.AccesoDenegadoException;
import com.proyecto.servicios.exception.CuentaInactivaException;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import com.proyecto.servicios.exception.ProductoInvalidoException;
import com.proyecto.servicios.exception.SaldoInsuficienteException;
import com.proyecto.servicios.exception.TransaccionDuplicadaException;
import com.proyecto.servicios.model.gestopago.GestoPagoTxRequest;
import com.proyecto.servicios.model.gestopago.GestoPagoTxResponse;
import com.proyecto.servicios.repository.CuentaRepository;
import com.proyecto.servicios.repository.TransaccionServicioRepository;
import com.proyecto.servicios.repository.gestopago.GestoPagoProductoRepository;
import com.proyecto.servicios.service.GestoPagoTokenService;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServicioServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionServicioRepository transaccionRepository;

    @Mock
    private GestoPagoProductoRepository productoRepository;

    @Mock
    private GestoPagoTxClient gestoPagoTxClient;

    @Mock
    private GestoPagoTokenService gestoPagoTokenService;

    @InjectMocks
    private PagoServicioServiceImpl pagoServicioService;

    private PagoServicioRequestDTO requestDTO;
    private Cuenta cuenta;
    private Cliente cliente;
    private Usuario usuario;
    private GestoPagoProducto producto;
    private GestoPagoToken token;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoServicioService, "idDistribuidor", 83);
        ReflectionTestUtils.setField(pagoServicioService, "codigoDispositivo", "GPS83-TPV-17");

        requestDTO = PagoServicioRequestDTO.builder()
                .numeroCuenta("1234567890")
                .idServicio("TELMEX")
                .idProducto("PROD-01")
                .referenciaServicio("5512345678")
                .monto(new BigDecimal("200.00"))
                .referenciaCliente("REF-12345")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .correo("cliente@banco.com")
                .estatus("ACTIVO")
                .build();

        cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("López")
                .activo(true)
                .usuario(usuario)
                .build();

        usuario.setCliente(cliente);

        cuenta = Cuenta.builder()
                .id(1L)
                .numeroCuenta("1234567890")
                .saldo(new BigDecimal("1000.00"))
                .activo(true)
                .cliente(cliente)
                .build();

        producto = GestoPagoProducto.builder()
                .id("PROD-01")
                .nombre("Recarga Telmex 200")
                .precio("200.00")
                .categoria("TELMEX")
                .disponible(true)
                .build();

        token = new GestoPagoToken();
        token.setToken("mock-bearer-token");
        token.setActivo(true);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Debe procesar un pago exitosamente y descontar saldo")
    void procesarPago_exitoso() {
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(transaccionRepository.saveAndFlush(any(TransaccionServicio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(gestoPagoTokenService.obtenerTokenActivo(anyInt(), anyString())).thenReturn(Optional.of(token));

        GestoPagoTxResponse responseMock = new GestoPagoTxResponse();
        responseMock.setCodigo(0);
        responseMock.setMensaje("Aprobada");
        responseMock.setFolio("FOLIO-PROV-999");

        when(gestoPagoTxClient.sendTx(eq("Bearer mock-bearer-token"), any(GestoPagoTxRequest.class)))
                .thenReturn(responseMock);

        PagoServicioResponseDTO response = pagoServicioService.procesarPago(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getEstatus()).isEqualTo(EstadoTransaccion.EXITOSA);
        assertThat(response.getFolioProveedor()).isEqualTo("FOLIO-PROV-999");
        assertThat(cuenta.getSaldo()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(cuentaRepository, times(1)).save(cuenta);
    }

    @Test
    @DisplayName("Debe lanzar TransaccionDuplicadaException si la referencia única ya existe")
    void procesarPago_referenciaDuplicada() {
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(true);

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(TransaccionDuplicadaException.class)
                .hasMessageContaining("Ya existe una transacción procesada o en curso");

        verifyNoInteractions(productoRepository, cuentaRepository, gestoPagoTxClient);
    }

    @Test
    @DisplayName("Debe lanzar TransaccionDuplicadaException por doble clic o reintento reciente")
    void procesarPago_duplicadoRecienteVentanaTiempo() {
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
                any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(TransaccionDuplicadaException.class)
                .hasMessageContaining("recientemente completada");

        verify(cuentaRepository, never()).save(any(Cuenta.class));
        verifyNoInteractions(gestoPagoTxClient);
    }

    @Test
    @DisplayName("Debe lanzar ProductoInvalidoException si el servicio no coincide con la categoría del producto")
    void procesarPago_servicioNoCorrespondeAProducto() {
        requestDTO.setIdServicio("CFE");
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(ProductoInvalidoException.class)
                .hasMessageContaining("no corresponde al producto");

        verifyNoInteractions(cuentaRepository, gestoPagoTxClient);
    }

    @Test
    @DisplayName("Debe lanzar ProductoInvalidoException si el producto no existe en el catálogo")
    void procesarPago_productoInexistente() {
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(ProductoInvalidoException.class)
                .hasMessageContaining("no existe en el catálogo");

        verifyNoInteractions(cuentaRepository, gestoPagoTxClient);
    }

    @Test
    @DisplayName("Debe lanzar SaldoInsuficienteException si el saldo es menor al monto")
    void procesarPago_saldoInsuficiente() {
        cuenta.setSaldo(new BigDecimal("50.00"));
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
                any(), any(), any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(cuentaRepository, never()).save(any(Cuenta.class));
        verifyNoInteractions(gestoPagoTxClient);
    }

    @Test
    @DisplayName("Debe lanzar CuentaInactivaException si la cuenta del cliente está inactiva")
    void procesarPago_cuentaInactiva() {
        cuenta.setActivo(false);
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(CuentaInactivaException.class)
                .hasMessageContaining("se encuentra inactiva");

        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Debe lanzar CuentaInactivaException si el cliente titular está inactivo")
    void procesarPago_clienteInactivo() {
        cliente.setActivo(false);
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(CuentaInactivaException.class)
                .hasMessageContaining("titular de la cuenta");

        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException si el usuario autenticado no es el titular de la cuenta")
    void procesarPago_usuarioNoAutorizado() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("intruso@banco.com", "pass", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> pagoServicioService.procesarPago(requestDTO))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessageContaining("No tiene autorización");

        verify(cuentaRepository, never()).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("Debe gestionar TIMEOUT sin reversar el saldo automáticamente cuando hay Gateway Timeout")
    void procesarPago_manejoTimeout() {
        when(transaccionRepository.existsByReferencia("REF-12345")).thenReturn(false);
        when(productoRepository.findById("PROD-01")).thenReturn(Optional.of(producto));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(transaccionRepository.saveAndFlush(any(TransaccionServicio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(gestoPagoTokenService.obtenerTokenActivo(anyInt(), anyString())).thenReturn(Optional.of(token));

        Request dummyRequest = Request.create(Request.HttpMethod.POST, "http://test", Map.of(), null, null, null);
        when(gestoPagoTxClient.sendTx(anyString(), any(GestoPagoTxRequest.class)))
                .thenThrow(new RetryableException(504, "Read timed out", Request.HttpMethod.POST, new Date(), dummyRequest));

        PagoServicioResponseDTO response = pagoServicioService.procesarPago(requestDTO);

        assertThat(response).isNotNull();
        assertThat(response.getEstatus()).isEqualTo(EstadoTransaccion.TIMEOUT);
        // El saldo debió permanecer debitado (800.00) en estado de verificación para no generar doble cobro
        assertThat(cuenta.getSaldo()).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    @DisplayName("Debe consultar transacción existente por referencia")
    void consultarTransaccion_exitoso() {
        TransaccionServicio tx = TransaccionServicio.builder()
                .referencia("REF-12345")
                .estado(EstadoTransaccion.TIMEOUT)
                .mensajeRespuesta("En verificación")
                .idServicio("TELMEX")
                .idProducto("PROD-01")
                .monto(new BigDecimal("200.00"))
                .cuenta(cuenta)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(transaccionRepository.findByReferencia("REF-12345")).thenReturn(Optional.of(tx));

        PagoServicioResponseDTO response = pagoServicioService.consultarTransaccion("REF-12345");

        assertThat(response).isNotNull();
        assertThat(response.getFolio()).isEqualTo("REF-12345");
        assertThat(response.getEstatus()).isEqualTo(EstadoTransaccion.TIMEOUT);
    }

    @Test
    @DisplayName("Debe conciliar transacción como FALLIDA y reembolsar saldo al cliente")
    void conciliarTransaccion_fallidaConReembolso() {
        TransaccionServicio tx = TransaccionServicio.builder()
                .referencia("REF-12345")
                .estado(EstadoTransaccion.TIMEOUT)
                .monto(new BigDecimal("200.00"))
                .cuenta(cuenta)
                .build();

        cuenta.setSaldo(new BigDecimal("800.00"));

        when(transaccionRepository.findByReferencia("REF-12345")).thenReturn(Optional.of(tx));
        when(cuentaRepository.findByNumeroCuentaWithLock("1234567890")).thenReturn(Optional.of(cuenta));

        PagoServicioResponseDTO response = pagoServicioService.conciliarTransaccion("REF-12345", false, null);

        assertThat(response).isNotNull();
        assertThat(response.getEstatus()).isEqualTo(EstadoTransaccion.FALLIDA);
        assertThat(cuenta.getSaldo()).isEqualByComparingTo(new BigDecimal("1000.00"));
        verify(cuentaRepository, times(1)).save(cuenta);
        verify(transaccionRepository, times(1)).save(tx);
    }
}
