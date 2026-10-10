-- ==========================================
-- SCRIPT DDL: ONBOARDING CLIENTES
-- DIALECTO: PostgreSQL
-- ==========================================

-- 1. Tabla: CLIENTES
CREATE TABLE IF NOT EXISTS clientes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100) NOT NULL,
    curp VARCHAR(18) NOT NULL UNIQUE,
    rfc VARCHAR(13) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    sexo VARCHAR(20) NOT NULL,
    nacionalidad VARCHAR(50) NOT NULL,
    estado_civil VARCHAR(50) NOT NULL,
    telefono_movil VARCHAR(10) NOT NULL,
    telefono_alternativo VARCHAR(10),
    ocupacion VARCHAR(100) NOT NULL,
    empresa VARCHAR(100) NOT NULL,
    ingreso_mensual NUMERIC(19, 2) NOT NULL CHECK (ingreso_mensual > 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla: USUARIOS
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estatus VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_usuarios_cliente 
        FOREIGN KEY (cliente_id) 
        REFERENCES clientes(id) 
        ON DELETE CASCADE
);

-- 3. Tabla: CUENTAS
CREATE TABLE IF NOT EXISTS cuentas (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    moneda VARCHAR(3) NOT NULL DEFAULT 'MXN',
    saldo NUMERIC(19, 2) NOT NULL DEFAULT 0.00 CHECK (saldo >= 0),
    tipo_cuenta VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cuentas_cliente 
        FOREIGN KEY (cliente_id) 
        REFERENCES clientes(id) 
        ON DELETE CASCADE
);

-- 4. Tabla: DOMICILIOS
CREATE TABLE IF NOT EXISTS domicilios (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    calle VARCHAR(150) NOT NULL,
    numero_exterior VARCHAR(20) NOT NULL,
    numero_interior VARCHAR(20),
    colonia VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(5) NOT NULL,
    municipio VARCHAR(50) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    pais VARCHAR(50) NOT NULL,
    CONSTRAINT fk_domicilios_cliente 
        FOREIGN KEY (cliente_id) 
        REFERENCES clientes(id) 
        ON DELETE CASCADE
);

-- ==========================================
-- ÍNDICES PARA OPTIMIZACIÓN DE BÚSQUEDAS
-- ==========================================

-- Acelerar búsquedas críticas de cliente
CREATE INDEX IF NOT EXISTS idx_clientes_curp ON clientes(curp);
CREATE INDEX IF NOT EXISTS idx_clientes_rfc ON clientes(rfc);
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_creacion ON clientes(creado_en);

-- Acelerar el proceso de Login
CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);

-- Acelerar búsqueda de cuentas
CREATE INDEX IF NOT EXISTS idx_cuentas_numero_cuenta ON cuentas(numero_cuenta);

-- Acelerar joins de llaves foráneas
CREATE INDEX IF NOT EXISTS idx_cuentas_cliente_id ON cuentas(cliente_id);
CREATE INDEX IF NOT EXISTS idx_domicilios_cliente_id ON domicilios(cliente_id);
