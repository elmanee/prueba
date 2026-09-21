package com.proyecto.servicios.scheduler;

import com.proyecto.servicios.service.ProductoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@Slf4j
@RequiredArgsConstructor
public class GestoPagoProductScheduler {

    private final ProductoService productoService;

    /**
     * Tarea programada para sincronizar el catálogo de productos de GestoPago.
     * Se ejecuta según el cron especificado en application.properties.
     */
    @Scheduled(cron = "${gestopago.productos.cron}")
    public void sincronizarCatalogoProductos() {
        log.info("Iniciando tarea programada: Sincronización de catálogo de productos GestoPago...");
        try {
            // Lógica de sincronización delegada al servicio
            // Ejemplo: productoService.sincronizarProductos();
            log.info("Tarea programada de sincronización finalizada con éxito.");
        } catch (Exception e) {
            log.error("Error durante la sincronización del catálogo de productos: {}", e.getMessage(), e);
        }
    }
}
