package com.proyecto.servicios.repository;

import com.proyecto.servicios.entity.Cuenta;
import com.proyecto.servicios.entity.EstadoTransaccion;
import com.proyecto.servicios.entity.TransaccionServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionServicioRepository extends JpaRepository<TransaccionServicio, Long> {

    /**
     * Busca una transacción por su referencia o folio único.
     */
    Optional<TransaccionServicio> findByReferencia(String referencia);

    /**
     * Verifica la existencia de una transacción con la misma referencia (clave de idempotencia).
     */
    boolean existsByReferencia(String referencia);

    /**
     * Busca transacciones recientes para la misma cuenta, producto y referencia de servicio dentro de una ventana de tiempo.
     * Útil para prevenir operaciones duplicadas accidentales del usuario (doble click / reintentos rápidos).
     */
    boolean existsByCuentaAndIdProductoAndNumeroReferenciaServicioAndFechaCreacionAfterAndEstadoNot(
            Cuenta cuenta,
            String idProducto,
            String numeroReferenciaServicio,
            LocalDateTime fechaLimite,
            EstadoTransaccion estadoExcluido
    );

    /**
     * Obtiene el historial de transacciones de una cuenta ordenadas de la más reciente a la más antigua.
     */
    List<TransaccionServicio> findByCuentaOrderByFechaCreacionDesc(Cuenta cuenta);
}
