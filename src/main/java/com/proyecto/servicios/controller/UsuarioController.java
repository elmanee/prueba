package com.proyecto.servicios.controller;

import com.proyecto.servicios.dto.CambioPasswordRequestDTO;
import com.proyecto.servicios.dto.UsuarioResponseDTO;
import com.proyecto.servicios.dto.UsuarioSearchRequestDTO;
import com.proyecto.servicios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la administración de usuarios.
 *
 * <p>Restricciones aplicadas:</p>
 * <ul>
 *   <li>No se usa {@code @PathVariable}.</li>
 *   <li>No se usa {@code @RequestParam}.</li>
 *   <li>Los criterios de búsqueda se reciben por {@code @RequestBody}.</li>
 *   <li>No se devuelven entidades JPA; solo DTOs de respuesta.</li>
 *   <li>No hay validaciones manuales en el controlador; se usan anotaciones Bean Validation.</li>
 *   <li>No hay bloques try-catch; el manejo de errores está en {@link com.proyecto.servicios.exception.GlobalExceptionHandler}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * POST /api/v1/usuarios/buscar
     * Busca usuarios por criterios dinámicos combinables.
     * Todos los campos del DTO son opcionales; al enviar {} retorna todos.
     */
    @PostMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscar(@RequestBody UsuarioSearchRequestDTO filtros) {
        return ResponseEntity.ok(usuarioService.buscar(filtros));
    }

    /**
     * PUT /api/v1/usuarios/cambiar-password
     * Cambia la contraseña del usuario identificado por correo.
     * Las validaciones de formato se ejecutan mediante @Valid y Bean Validation.
     */
    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambioPasswordRequestDTO req) {
        usuarioService.cambiarPassword(req);
        return ResponseEntity.noContent().build();
    }
}
