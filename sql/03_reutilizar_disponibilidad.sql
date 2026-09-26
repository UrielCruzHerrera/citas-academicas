-- Solo para bases existentes. No borra citas ni bitácoras.
-- Ejecutar con la aplicación detenida y una copia de respaldo de la base.
-- psql -v ON_ERROR_STOP=1 -d citas_academicas -f sql/03_reutilizar_disponibilidad.sql
BEGIN;
LOCK TABLE cita IN ACCESS EXCLUSIVE MODE;
CREATE UNIQUE INDEX IF NOT EXISTS uq_disponibilidad_no_cancelada
    ON cita (id_disponibilidad) WHERE estado <> 'CANCELADA';
ALTER TABLE cita DROP CONSTRAINT IF EXISTS uq_disponibilidad;
COMMIT;
