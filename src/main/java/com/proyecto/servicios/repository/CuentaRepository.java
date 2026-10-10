package com.proyecto.servicios.repository;

import com.proyecto.servicios.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
    List<Cuenta> findByActivoTrue();
}