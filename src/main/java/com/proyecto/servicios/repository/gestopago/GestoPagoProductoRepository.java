package com.proyecto.servicios.repository.gestopago;

import com.proyecto.servicios.entity.gestopago.GestoPagoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GestoPagoProductoRepository extends JpaRepository<GestoPagoProducto, String> {
}
