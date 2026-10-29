package com.proyecto.servicios.repository;

import com.proyecto.servicios.dto.ClienteSearchRequestDTO;
import com.proyecto.servicios.entity.Cliente;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para búsqueda dinámica de clientes.
 * Permite combinar múltiples filtros opcionales en una sola consulta.
 */
public class ClienteSpecification {

    private ClienteSpecification() {}

    /**
     * Construye una {@link Specification} a partir de un DTO de búsqueda.
     * Todos los criterios son opcionales y se combinan con AND.
     */
    public static Specification<Cliente> fromSearchRequest(ClienteSearchRequestDTO req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (req.getId() != null) {
                predicates.add(cb.equal(root.get("id"), req.getId()));
            }
            if (req.getNombre() != null && !req.getNombre().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + req.getNombre().toLowerCase() + "%"));
            }
            if (req.getApellidoPaterno() != null && !req.getApellidoPaterno().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("apellidoPaterno")),
                        "%" + req.getApellidoPaterno().toLowerCase() + "%"));
            }
            if (req.getCurp() != null && !req.getCurp().isBlank()) {
                predicates.add(cb.equal(root.get("curp"), req.getCurp().toUpperCase()));
            }
            if (req.getRfc() != null && !req.getRfc().isBlank()) {
                predicates.add(cb.equal(root.get("rfc"), req.getRfc().toUpperCase()));
            }
            if (req.getSexo() != null && !req.getSexo().isBlank()) {
                predicates.add(cb.equal(root.get("sexo"), req.getSexo()));
            }
            if (req.getNacionalidad() != null && !req.getNacionalidad().isBlank()) {
                predicates.add(cb.equal(root.get("nacionalidad"), req.getNacionalidad()));
            }
            if (req.getActivo() != null) {
                predicates.add(cb.equal(root.get("activo"), req.getActivo()));
            }
            if (req.getFechaNacimientoDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaNacimiento"),
                        req.getFechaNacimientoDesde()));
            }
            if (req.getFechaNacimientoHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaNacimiento"),
                        req.getFechaNacimientoHasta()));
            }
            if (req.getCreadoDesde() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("creadoEn"), req.getCreadoDesde()));
            }
            if (req.getCreadoHasta() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("creadoEn"), req.getCreadoHasta()));
            }
            if (req.getIngresoMensualMinimo() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ingresoMensual"),
                        req.getIngresoMensualMinimo()));
            }
            if (req.getCorreo() != null && !req.getCorreo().isBlank()) {
                Join<Object, Object> usuario = root.join("usuario", JoinType.LEFT);
                predicates.add(cb.equal(cb.lower(usuario.get("correo")),
                        req.getCorreo().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
