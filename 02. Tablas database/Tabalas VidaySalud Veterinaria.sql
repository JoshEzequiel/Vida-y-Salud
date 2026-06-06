CREATE DATABASE IF NOT EXISTS veterinaria_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE veterinaria_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notificacion;
DROP TABLE IF EXISTS pago;
DROP TABLE IF EXISTS movimiento_inventario;
DROP TABLE IF EXISTS detalle_receta;
DROP TABLE IF EXISTS receta;
DROP TABLE IF EXISTS consulta;
DROP TABLE IF EXISTS cita;
DROP TABLE IF EXISTS medicamento;
DROP TABLE IF EXISTS mascota;
DROP TABLE IF EXISTS veterinario;
DROP TABLE IF EXISTS cliente;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    rut VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(200),
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cliente_rut UNIQUE (rut),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE veterinario (
    id_veterinario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    especialidad VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    email VARCHAR(100),
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uq_veterinario_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE mascota (
    id_mascota INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    especie VARCHAR(50) NOT NULL,
    raza VARCHAR(100),
    fecha_nacimiento DATE,
    sexo ENUM('Macho','Hembra') NOT NULL,
    peso DECIMAL(5,2),
    activa TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_mascota_peso CHECK (peso IS NULL OR peso >= 0)
) ENGINE=InnoDB;
CREATE INDEX idx_mascota_cliente ON mascota(id_cliente);

CREATE TABLE cita (
    id_cita INT AUTO_INCREMENT PRIMARY KEY,
    id_mascota INT NOT NULL,
    id_veterinario INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado ENUM('Pendiente','Confirmada','Atendida','Cancelada') NOT NULL DEFAULT 'Pendiente',
    motivo VARCHAR(255),
    observaciones VARCHAR(500),
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota)
        REFERENCES mascota(id_mascota) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_veterinario FOREIGN KEY (id_veterinario)
        REFERENCES veterinario(id_veterinario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_cita_mascota ON cita(id_mascota);
CREATE INDEX idx_cita_veterinario ON cita(id_veterinario);
CREATE INDEX idx_cita_fecha_hora ON cita(fecha_hora);

CREATE TABLE consulta (
    id_consulta INT AUTO_INCREMENT PRIMARY KEY,
    id_mascota INT NOT NULL,
    id_veterinario INT NOT NULL,
    id_cita INT NULL,
    fecha_consulta DATETIME NOT NULL,
    motivo TEXT,
    diagnostico TEXT,
    observaciones TEXT,
    peso_actual DECIMAL(5,2),
    temperatura DECIMAL(4,2),
    costo_consulta DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_consulta_mascota FOREIGN KEY (id_mascota)
        REFERENCES mascota(id_mascota) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_consulta_veterinario FOREIGN KEY (id_veterinario)
        REFERENCES veterinario(id_veterinario) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_consulta_cita FOREIGN KEY (id_cita)
        REFERENCES cita(id_cita) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT uq_consulta_cita UNIQUE (id_cita),
    CONSTRAINT ck_consulta_costo CHECK (costo_consulta >= 0)
) ENGINE=InnoDB;
CREATE INDEX idx_consulta_mascota ON consulta(id_mascota);
CREATE INDEX idx_consulta_veterinario ON consulta(id_veterinario);

CREATE TABLE medicamento (
    id_medicamento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    stock INT NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT uq_medicamento_nombre UNIQUE (nombre),
    CONSTRAINT ck_medicamento_stock CHECK (stock >= 0),
    CONSTRAINT ck_medicamento_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

CREATE TABLE receta (
    id_receta INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta INT NOT NULL,
    fecha_emision DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    indicaciones_generales TEXT,
    CONSTRAINT fk_receta_consulta FOREIGN KEY (id_consulta)
        REFERENCES consulta(id_consulta) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_receta_consulta ON receta(id_consulta);

CREATE TABLE detalle_receta (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_receta INT NOT NULL,
    id_medicamento INT NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    frecuencia VARCHAR(100),
    duracion VARCHAR(100),
    indicaciones TEXT,
    CONSTRAINT fk_detalle_receta FOREIGN KEY (id_receta)
        REFERENCES receta(id_receta) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_medicamento FOREIGN KEY (id_medicamento)
        REFERENCES medicamento(id_medicamento) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_detalle_receta_medicamento UNIQUE (id_receta,id_medicamento)
) ENGINE=InnoDB;

CREATE TABLE movimiento_inventario (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_medicamento INT NOT NULL,
    tipo_movimiento ENUM('ENTRADA','SALIDA','AJUSTE') NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_posterior INT NOT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo VARCHAR(255),
    CONSTRAINT fk_movimiento_medicamento FOREIGN KEY (id_medicamento)
        REFERENCES medicamento(id_medicamento) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_movimiento_stock_anterior CHECK (stock_anterior >= 0),
    CONSTRAINT ck_movimiento_stock_posterior CHECK (stock_posterior >= 0)
) ENGINE=InnoDB;

CREATE TABLE pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
    estado ENUM('PENDIENTE','PAGADO','ANULADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATETIME,
    referencia VARCHAR(100),
    CONSTRAINT uq_pago_consulta UNIQUE (id_consulta),
    CONSTRAINT fk_pago_consulta FOREIGN KEY (id_consulta)
        REFERENCES consulta(id_consulta) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_pago_monto CHECK (monto > 0)
) ENGINE=InnoDB;

CREATE TABLE notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    tipo ENUM('EMAIL','SMS','PUSH') NOT NULL,
    destinatario VARCHAR(150) NOT NULL,
    asunto VARCHAR(150) NOT NULL,
    mensaje TEXT NOT NULL,
    estado ENUM('PENDIENTE','ENVIADA','FALLIDA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio DATETIME,
    CONSTRAINT fk_notificacion_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;
