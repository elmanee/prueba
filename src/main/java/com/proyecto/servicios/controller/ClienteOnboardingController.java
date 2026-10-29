package com.proyecto.servicios.controller;

import com.proyecto.servicios.dto.*;
import com.proyecto.servicios.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la administración de clientes.
 *
 * <p>Restricciones aplicadas:</p>
 * <ul>
 *   <li>No se usa {@code @PathVariable}.</li>
 *   <li>No se usa {@code @RequestParam}.</li>
 *   <li>Los criterios de búsqueda se reciben por {@code @RequestBody}.</li>
 *   <li>No se devuelven entidades JPA; solo DTOs de respuesta.</li>
 *   <li>No hay bloques try-catch; el manejo de errores está en
 *       {@link com.proyecto.servicios.exception.GlobalExceptionHandler}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteOnboardingController {

    private final ClienteService clienteService;

    /**
     * POST /api/v1/clientes/registro
     * Registra un nuevo cliente junto con su usuario y cuenta.
     */
    @PostMapping("/registro")
    public ResponseEntity<ClienteResponseDTO> registrar(@Valid @RequestBody ClienteRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrarCliente(dto));
    }

    /**
     * POST /api/v1/clientes/buscar
     * Busca clientes por criterios dinámicos combinables (todos opcionales).
     * Enviar {} retorna todos los clientes.
     */
    @PostMapping("/buscar")
    public ResponseEntity<List<ClienteResponseDTO>> buscar(@RequestBody ClienteSearchRequestDTO filtros) {
        return ResponseEntity.ok(clienteService.buscar(filtros));
    }

    /**
     * PUT /api/v1/clientes/actualizar
     * Actualiza los datos de un cliente. El body debe incluir
     * el id dentro de "busqueda" y los campos a cambiar en "cambios".
     */
    @PutMapping("/actualizar")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @Valid @RequestBody ClienteActualizarRequestDTO req) {
        return ResponseEntity.ok(clienteService.actualizarCliente(req.getBusqueda(), req.getCambios()));
    }

    /**
     * DELETE /api/v1/clientes/baja
     * Da de baja lógica al cliente cuyo id viene en el body.
     */
    @DeleteMapping("/baja")
    public ResponseEntity<Void> darDeBaja(@RequestBody ClienteSearchRequestDTO req) {
        clienteService.darDeBaja(req);
        return ResponseEntity.noContent().build();
    }
}
