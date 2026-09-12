-- Conectarse primero: psql -U postgres -d citas_academicas -f sql/01_schema.sql

CREATE TABLE usuario (
    id            SERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(120) UNIQUE NOT NULL,
    rol           VARCHAR(20) NOT NULL
                  CHECK (rol IN ('ESTUDIANTE','ASESOR','COORDINACION')),
    password_hash VARCHAR(255) NOT NULL,
    activo        BOOLEAN DEFAULT TRUE,
    creado_en     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE motivo (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo      BOOLEAN DEFAULT TRUE
);

CREATE TABLE disponibilidad (
    id           SERIAL PRIMARY KEY,
    id_asesor    INT NOT NULL REFERENCES usuario(id),
    fecha        DATE NOT NULL,
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME NOT NULL,
    estado       VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE'
                 CHECK (estado IN ('DISPONIBLE','OCUPADO','BLOQUEADO')),
    CONSTRAINT chk_horario CHECK (hora_fin > hora_inicio),
    UNIQUE (id_asesor, fecha, hora_inicio)
);

CREATE TABLE cita (
    id                SERIAL PRIMARY KEY,
    id_estudiante     INT NOT NULL REFERENCES usuario(id),
    id_disponibilidad INT NOT NULL REFERENCES disponibilidad(id),
    id_motivo         INT NOT NULL REFERENCES motivo(id),
    estado            VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA'
                      CHECK (estado IN ('SOLICITADA','CONFIRMADA','CANCELADA','ATENDIDA')),
    fecha_solicitud   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones     VARCHAR(255),
    CONSTRAINT uq_disponibilidad UNIQUE (id_disponibilidad)
);

CREATE TABLE cambio_estado (
    id              SERIAL PRIMARY KEY,
    id_cita         INT NOT NULL REFERENCES cita(id),
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20) NOT NULL,
    id_usuario      INT NOT NULL REFERENCES usuario(id),
    fecha_cambio    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion     VARCHAR(255)
);

CREATE INDEX idx_disponibilidad_fecha ON disponibilidad(fecha);
CREATE INDEX idx_cita_estudiante ON cita(id_estudiante);
CREATE INDEX idx_cita_estado ON cita(estado);