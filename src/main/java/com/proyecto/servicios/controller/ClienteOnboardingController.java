package com.proyecto.servicios.controller;

import com.proyecto.servicios.dto.ClienteUpdateDTO;
import com.proyecto.servicios.dto.ClienteRegistroDTO;
import com.proyecto.servicios.entity.Cliente;
import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.repository.CuentaRepository;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.exception.CuentaNoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ClienteOnboardingController {

    private final ClienteService clienteService;
    private final CuentaRepository cuentaRepository;

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> registrar(@Valid @RequestBody ClienteRegistroDTO dto) {
        return new ResponseEntity<>(clienteService.registrarCliente(dto), HttpStatus.CREATED);
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarActivos() {
        return ResponseEntity.ok(clienteService.obtenerClientesActivos());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/clientes/curp/{curp}")
    public ResponseEntity<Cliente> obtenerPorCurp(@PathVariable String curp) {
        return ResponseEntity.ok(clienteService.buscarPorCurp(curp));
    }

    @GetMapping("/clientes/rfc/{rfc}")
    public ResponseEntity<Cliente> obtenerPorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.buscarPorRfc(rfc));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> darDeBaja(@PathVariable Long id) {
        clienteService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cuentas/{numeroCuenta}")
    public ResponseEntity<Cuenta> obtenerCuenta(@PathVariable String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada"));
        return ResponseEntity.ok(cuenta);
    }

    @GetMapping("/clientes/correo/{correo}")
    public ResponseEntity<Cliente> obtenerPorCorreo(@PathVariable String correo) {
        return ResponseEntity.ok(clienteService.buscarPorCorreo(correo));
    }

    @GetMapping("/cuentas/activas")
    public ResponseEntity<List<Cuenta>> obtenerCuentasActivas() {
        return ResponseEntity.ok(clienteService.obtenerCuentasActivas());
    }

    @GetMapping("/cuentas/{numeroCuenta}/saldo")
    public ResponseEntity<BigDecimal> obtenerSaldoCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(clienteService.obtenerSaldoCuenta(numeroCuenta));
    }

    @GetMapping("/clientes/rango-fechas")
    public ResponseEntity<List<Cliente>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(clienteService.buscarPorRangoFechas(inicio, fin));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, dto));
    }
}
