# Correcciones incrementales Web 1.0

Se mantienen Java 11, javax.servlet, JSP, DAO, PreparedStatement y transacciones JDBC.

## Comportamiento corregido

- Al abrir `solicitar?id=2` se selecciona ese horario, si sigue disponible.
  Ante una entrada inválida se muestra un mensaje; no se selecciona otro bloque automáticamente.
- Solicitud y cancelación rechazan identificadores ausentes, no numéricos, fuera del rango de int o no positivos.
- La solicitud comprueba estudiante activo con rol ESTUDIANTE, motivo activo y
  bloque futuro DISPONIBLE de un asesor activo. La toma condicional del bloque,
  la cita y la bitácora forman una transacción.
- Una cancelación conserva la cita histórica y libera el bloque para una nueva cita.
  Un índice único parcial impide dos citas no canceladas para la misma disponibilidad.
- Cancelar exige que la cita pertenezca al usuario sintético fijado por el servidor
  y esté SOLICITADA o CONFIRMADA. Manipular idCita no permite cancelar citas ajenas.

## Migración de una base existente

1. Detener la aplicación y respaldar la base con las herramientas habituales.
2. Desde la raíz del proyecto, utilizando las credenciales de tu entorno:

```bash
psql -v ON_ERROR_STOP=1 -U TU_USUARIO -d citas_academicas -f sql/03_reutilizar_disponibilidad.sql
```

3. Compilar/desplegar la versión corregida y reiniciar la aplicación.

La migración es transaccional y repetible. Crea el índice parcial antes de retirar
la restricción anterior; no borra citas ni bitácoras. Para una instalación nueva,
utilizar `01_schema.sql` y `02_datos_prueba.sql`; no es necesario migrar.

## Pruebas reproducibles

Requisitos: Python 3, JDK con soporte de `--release 11`, Maven, herramientas de
servidor PostgreSQL (`initdb`, `pg_ctl`, `psql`, `createdb`, `pg_config`) y las
dependencias Maven descargadas. Ejecutar como usuario normal, no como root.
Si faltan dependencias, ejecutar primero `mvn test-compile` con acceso al repositorio Maven.

```bash
python3 scripts/verificar-correcciones.py
```

`PG_BIN` permite seleccionar otra instalación de PostgreSQL; `MAVEN_REPO` permite
indicar el repositorio local si no está en `~/.m2/repository`.
El verificador crea un clúster temporal que escucha solo en localhost en un puerto
libre, con datos sintéticos y autenticación trust solo durante la prueba. No usa
la conexión ni la base de la aplicación. Detiene y elimina ese clúster al terminar.

La clase `PruebaCorrecciones` es una prueba de integración ejecutable mediante
este script; no se ejecuta automáticamente con `mvn test`. Usa los DAO reales
contra PostgreSQL y dobles de petición/respuesta para invocar los Servlets.

## Resultado de la revisión del 25 de septiembre de 2026

VERIFICADO:
- Compilación Maven y compilación de código y pruebas con `javac --release 11`.
- 73 comprobaciones sobre esquema nuevo y 73 sobre esquema migrado.
- Selección del horario en el controlador, validación de entradas, motivos inactivos,
  usuarios no habilitados, horarios pasados/bloqueados/inexistentes, doble reserva,
  cancelación ajena/repetida/inexistente, reutilización y preservación del historial.
- Dos solicitudes concurrentes: solo una tiene éxito.
- Una falla forzada al escribir la bitácora revierte cita y ocupación del bloque.
- Migración aplicada dos veces sin pérdida de historial.

Entorno de base de pruebas: PostgreSQL 11.22, disponible localmente.

NO_VERIFICADO: renderizado JSP y recorrido HTTP en Tomcat 9; ejecución con PostgreSQL 16.

PENDIENTE: aplicar la migración a la base de la aplicación y verificar su despliegue.
No se modificó esa base durante esta revisión.
