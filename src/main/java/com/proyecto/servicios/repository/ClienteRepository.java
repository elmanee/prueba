package com.proyecto.servicios.repository;

import com.proyecto.servicios.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCurp(String curp);
    Optional<Cliente> findByRfc(String rfc);
    List<Cliente> findByActivoTrue();
    boolean existsByCurp(String curp);
    boolean existsByRfc(String rfc);
    Optional<Cliente> findByUsuarioCorreo(String correo);
    List<Cliente> findByCreadoEnBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fin);
}