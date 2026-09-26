#!/usr/bin/env python3
"""Compila y prueba en PostgreSQL temporal; nunca utiliza la BD de la aplicación."""
import os
from pathlib import Path
import socket
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
PG_BIN = Path(os.environ["PG_BIN"] if "PG_BIN" in os.environ else subprocess.check_output(
    ["pg_config", "--bindir"], text=True).strip())
REPO = Path(os.environ.get("MAVEN_REPO", str(Path.home() / ".m2/repository")))
JARS = [REPO / "javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar",
        REPO / "org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar"]

def run(args, **kwargs):
    return subprocess.run([str(a) for a in args], check=True, **kwargs)

run(["mvn", "-o", "test-compile"], cwd=ROOT)
for jar in JARS:
    if not jar.is_file():
        raise SystemExit("Dependencia no disponible: " + str(jar))
run([PG_BIN / "pg_ctl", "--version"])
# Comprobar además APIs Java 11 aunque Maven se ejecute con otro JDK.
with tempfile.TemporaryDirectory(prefix="pr02-regresion-") as temporal:
    tmp = Path(temporal)
    run(["javac", "--release", "11", "-encoding", "UTF-8", "-cp", os.pathsep.join(map(str, JARS)),
         "-d", tmp / "clases", *sorted((ROOT / "src/main/java").rglob("*.java")),
         *sorted((ROOT / "src/test/java").rglob("*.java"))])
    with socket.socket() as sock:
        sock.bind(("127.0.0.1", 0)); port = sock.getsockname()[1]
    data = tmp / "data"
    run([PG_BIN / "initdb", "-D", data, "-U", "pr02_test", "--auth=trust", "--encoding=UTF8"],
        stdout=subprocess.DEVNULL)
    iniciado = False
    try:
        run([PG_BIN / "pg_ctl", "-D", data, "-l", tmp / "postgres.log", "-o",
             f"-h 127.0.0.1 -p {port} -k {tmp}", "-w", "start"])
        iniciado = True
        run([PG_BIN / "createdb", "-h", "127.0.0.1", "-p", port,
             "-U", "pr02_test", "pr02_regresion"])
        psql = [PG_BIN / "psql", "-h", "127.0.0.1", "-p", port, "-U", "pr02_test",
                "-d", "pr02_regresion", "-v", "ON_ERROR_STOP=1"]
        env = dict(os.environ, CITAS_DB_URL=f"jdbc:postgresql://127.0.0.1:{port}/pr02_regresion",
                   CITAS_DB_USER="pr02_test", CITAS_DB_PASS="")
        java = ["java", "-cp", os.pathsep.join(map(str, [tmp / "clases", *JARS])),
                "mx.uv.dsw.citas.PruebaCorrecciones"]
        run([*psql, "-f", ROOT / "sql/01_schema.sql"], stdout=subprocess.DEVNULL)
        run([*psql, "-f", ROOT / "sql/02_datos_prueba.sql"], stdout=subprocess.DEVNULL)
        run(java, env=env)
        # Restaurar solo la base desechable y simular la restricción del esquema anterior.
        run([*psql, "-c", "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"],
            stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        run([*psql, "-f", ROOT / "sql/01_schema.sql"], stdout=subprocess.DEVNULL)
        run([*psql, "-f", ROOT / "sql/02_datos_prueba.sql"], stdout=subprocess.DEVNULL)
        run([*psql, "-c", "DROP INDEX uq_disponibilidad_no_cancelada; "
             "ALTER TABLE cita ADD CONSTRAINT uq_disponibilidad UNIQUE(id_disponibilidad); "
             "INSERT INTO cita(id_estudiante,id_disponibilidad,id_motivo,estado) "
             "VALUES(1,1,1,'CANCELADA'); "
             "INSERT INTO cambio_estado(id_cita,estado_nuevo,id_usuario) VALUES(1,'CANCELADA',1);"],
            stdout=subprocess.DEVNULL)
        for _ in range(2):
            run([*psql, "-f", ROOT / "sql/03_reutilizar_disponibilidad.sql"], stdout=subprocess.DEVNULL)
        run([*psql, "-c", "DO $$ BEGIN "
             "IF (SELECT count(*) FROM cita) <> 1 OR (SELECT count(*) FROM cambio_estado) <> 1 "
             "THEN RAISE EXCEPTION 'Se perdió historial'; END IF; END $$; "
             "INSERT INTO cita(id_estudiante,id_disponibilidad,id_motivo) VALUES(1,1,1);"],
            stdout=subprocess.DEVNULL)
        print("VERIFICADO: migración repetible, historial conservado y bloque reutilizable.", flush=True)
        run([*psql, "-c", "TRUNCATE cambio_estado, cita RESTART IDENTITY;"], stdout=subprocess.DEVNULL)
        run(java, env=env)
    finally:
        if iniciado:
            run([PG_BIN / "pg_ctl", "-D", data, "-m", "fast", "-w", "stop"])
