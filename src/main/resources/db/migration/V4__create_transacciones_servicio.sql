CREATE TABLE IF NOT EXISTS transacciones_servicio (
    id BIGSERIAL PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    referencia VARCHAR(100) NOT NULL UNIQUE, 
    id_servicio VARCHAR(50) NOT NULL,
    id_producto VARCHAR(50) NOT NULL,
    numero_referencia_servicio VARCHAR(100) NOT NULL, 
    monto NUMERIC(12, 2) NOT NULL CHECK (monto > 0),
    estado VARCHAR(20) NOT NULL,
    mensaje_respuesta VARCHAR(255),
    folio_autorizacion_proveedor VARCHAR(100),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transaccion_cuenta FOREIGN KEY (cuenta_id) 
        REFERENCES cuentas(id) 
        ON DELETE RESTRICT
);

-- Índices estratégicos para búsquedas rápidas y control de concurrencia/duplicados
CREATE INDEX IF NOT EXISTS idx_transacciones_cuenta_fecha ON transacciones_servicio(cuenta_id, fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_transacciones_referencia_cliente ON transacciones_servicio(referencia);