# PR02 — Sistema de citas académicas

**Experiencia educativa:** Desarrollo de Sistemas Web  
**Actividad:** P03 — Aplicación Web 2.0  
**Tecnologías principales:** JSF, PrimeFaces, JDBC y PostgreSQL  

## Objetivo

Evolucionar el incremento Web 1.0 de PR02 a una aplicación Web 2.0 con JSF y
PrimeFaces. El sistema permite que un estudiante consulte horarios disponibles,
seleccione un motivo, solicite una cita, consulte sus citas y cancele las que le
pertenecen. Los cambios se persisten en PostgreSQL mediante JDBC.

El incremento conserva el problema, los datos y el flujo principal de P02. La
evolución se concentra en las vistas XHTML, los componentes PrimeFaces, los
Managed Beans, las validaciones y los mensajes presentados al usuario.

## Alcance de P03

### Incluido

- Consulta de horarios disponibles.
- Selección de un horario.
- Solicitud de cita con motivo obligatorio.
- Validación del horario y del motivo en el servidor.
- Prevención de doble reserva.
- Persistencia transaccional de la cita en PostgreSQL.
- Registro de cambios de estado.
- Consulta de las citas del estudiante de demostración.
- Cancelación autorizada por estudiante.
- Liberación y reutilización de horarios cancelados.
- Mensajes comprensibles mediante JSF y PrimeFaces.

### Fuera del alcance

- Autenticación real y administración avanzada de identidades.
- Interfaces para asesor y coordinación.
- API REST, Angular e integración IoT.

## Trazabilidad P02 → P03

| Elemento | P02 — Web 1.0 | P03 — Web 2.0 |
| --- | --- | --- |
| Vistas | JSP y JSTL | XHTML con JSF y PrimeFaces |
| Control de vista | Servlets | Managed Beans con alcance de vista |
| Navegación | Enlaces y redirecciones Servlet | Resultados JSF y componentes PrimeFaces |
| Formularios | HTML/JSP | Formularios JSF con validación declarativa y de servidor |
| Persistencia | JDBC y PostgreSQL | Se conservan los DAO, JDBC y PostgreSQL |
| Reglas de negocio | Servlet y DAO | Bean y DAO; las operaciones críticas permanecen en el servidor |
| Código anterior | Implementación principal | Se conserva como antecedente durante la migración incremental |

## Tecnologías y versiones del proyecto

Las versiones siguientes se obtienen de `pom.xml`:

| Tecnología o dependencia | Versión |
| --- | ---: |
| Java | 11 |
| Servlet API (`javax.servlet`) | 4.0.1 |
| JSTL | 1.2 |
| Apache MyFaces | 2.3.12 |
| PrimeFaces | 14.0.12 |
| PostgreSQL JDBC | 42.7.3 |
| JAXB API | 2.3.1 |
| Maven WAR Plugin | 3.4.0 |

La aplicación está preparada para Apache Tomcat 9. La versión exacta de Maven,
Tomcat y del servidor PostgreSQL debe registrarse con la evidencia del entorno
utilizado para la entrega; no está fijada en `pom.xml`.

## Arquitectura

| Capa | Ubicación | Responsabilidad |
| --- | --- | --- |
| Vistas JSF | `src/main/webapp/*.xhtml` | Formularios, tablas, navegación y mensajes PrimeFaces |
| Managed Beans | `src/main/java/mx/uv/dsw/citas/controlador` | Estado de las vistas y coordinación del flujo JSF |
| DAO | `src/main/java/mx/uv/dsw/citas/dao` | Consultas preparadas, transacciones y reglas de persistencia |
| Modelo | `src/main/java/mx/uv/dsw/citas/modelo` | Entidades del dominio |
| Configuración web | `src/main/webapp/WEB-INF/web.xml` | Registro y mapeo de `FacesServlet` |
| Base de datos | `sql` | Esquema, datos sintéticos y migración desde P02 |

Los JSP y Servlets de Web 1.0 permanecen en el repositorio para conservar la
trazabilidad del incremento. El flujo P03 se prueba mediante las páginas XHTML.

## Estructura principal

```text
.
├── pom.xml
├── README.md
├── docs/
├── sql/
│   ├── 01_schema.sql
│   ├── 02_datos_prueba.sql
│   └── 03_reutilizar_disponibilidad.sql
└── src/
    ├── main/
    │   ├── java/mx/uv/dsw/citas/
    │   │   ├── controlador/
    │   │   ├── dao/
    │   │   └── modelo/
    │   └── webapp/
    │       ├── WEB-INF/web.xml
    │       ├── index.xhtml
    │       ├── horarios.xhtml
    │       ├── solicitar-cita.xhtml
    │       └── mis-citas.xhtml
    └── test/
```

## Modelo PostgreSQL

| Entidad | Propósito |
| --- | --- |
| `usuario` | Estudiantes, asesores y coordinación |
| `disponibilidad` | Bloques de fecha y horario publicados por un asesor |
| `motivo` | Catálogo de motivos de asesoría |
| `cita` | Solicitudes asociadas con estudiante, horario y motivo |
| `cambio_estado` | Historial de transiciones de estado de una cita |

El esquema contiene claves foráneas, restricciones de estado y un índice único
parcial para impedir más de una cita no cancelada sobre la misma disponibilidad.
La creación y la cancelación se realizan dentro de transacciones JDBC.

## Requisitos previos

Antes de iniciar, se necesita:

- JDK 11.
- Maven.
- Apache Tomcat 9.
- PostgreSQL en ejecución.
- Cliente `psql` para preparar y consultar la base de datos.
- Una terminal ubicada en la raíz que contiene `pom.xml`.

Compruebe las herramientas disponibles:

```bash
java -version
mvn -version
psql --version
```

## Preparación de PostgreSQL

### Base de datos nueva

Cree una base vacía y ejecute los scripts en este orden:

```bash
psql -U postgres -c "CREATE DATABASE citas_academicas;"
psql -U postgres -d citas_academicas -f sql/01_schema.sql
psql -U postgres -d citas_academicas -f sql/02_datos_prueba.sql
```

`02_datos_prueba.sql` contiene únicamente información sintética para demostrar
el flujo. No sustituya esos registros con información personal real.

### Base proveniente de P02

Si las tablas ya existían antes de la corrección que permite reutilizar una
disponibilidad cancelada, aplique solamente la migración correspondiente:

```bash
psql -U postgres -d citas_academicas -f sql/03_reutilizar_disponibilidad.sql
```

No vuelva a ejecutar `01_schema.sql` ni `02_datos_prueba.sql` sobre una base que
ya contiene información. Consulte también
[`docs/correcciones-web1.md`](docs/correcciones-web1.md).

## Variables de entorno

La conexión utiliza las siguientes variables:

| Variable | Contenido esperado |
| --- | --- |
| `CITAS_DB_URL` | URL JDBC de la base de datos |
| `CITAS_DB_USER` | Usuario PostgreSQL de la aplicación |
| `CITAS_DB_PASS` | Contraseña local del usuario |

Defínalas en la misma terminal desde la que se iniciará Tomcat:

```bash
export CITAS_DB_URL='jdbc:postgresql://localhost:5432/citas_academicas'
export CITAS_DB_USER='postgres'
export CITAS_DB_PASS='<contraseña-local>'
```

El texto `<contraseña-local>` es un marcador y debe reemplazarse localmente. 

## Compilación y generación del WAR

Desde la raíz del proyecto ejecute:

```bash
mvn clean package
```

El resultado correcto termina con `BUILD SUCCESS`. Maven genera:

```text
target/citas-academicas.war
```

Si Maven no puede descargar una dependencia, compruebe la conexión al
repositorio Maven configurado y vuelva a ejecutar el mismo comando. No copie un
WAR si la compilación terminó con error.

## Despliegue en Tomcat 9

Defina `ejemplo_HOME` con la carpeta real de su instalación de Tomcat. Después
copie el WAR en `webapps`:

```bash
cp target/citas-academicas.war "$ejemplo_HOME/webapps/"
```

### Primer inicio

```bash
"$ejemplo_HOME/bin/startup.sh"
```

### Redespliegue o reinicio

Cuando recompile la aplicación, detenga Tomcat, sustituya el WAR y vuelva a
iniciarlo:

```bash
"$ejemplo_HOME/bin/shutdown.sh"
cp target/citas-academicas.war "$ejemplo_HOME/webapps/"
"$ejemplo_HOME/bin/startup.sh"
```

Tomcat debe iniciarse desde un entorno que tenga disponibles
`CITAS_DB_URL`, `CITAS_DB_USER` y `CITAS_DB_PASS`. Si Tomcat se ejecuta como
servicio, configure esas variables en el servicio correspondiente.

## URLs de acceso

Con Tomcat en el puerto local predeterminado `8080` y el WAR desplegado con su
nombre actual, abra:

| Pantalla | URL |
| --- | --- |
| Inicio P03 | `http://localhost:8080/citas-academicas/index.xhtml` |
| Horarios | `http://localhost:8080/citas-academicas/horarios.xhtml` |
| Solicitud | Se abre después de seleccionar un horario válido |
| Mis citas | `http://localhost:8080/citas-academicas/mis-citas.xhtml` |

Si el puerto o el nombre de contexto se cambian en Tomcat, ajuste la URL de
acuerdo con esa configuración local.

## Flujo principal paso a paso

1. Abra `index.xhtml`.
2. Seleccione **Consultar horarios disponibles**.
3. Compruebe que la tabla muestre asesor, fecha, hora de inicio y hora de fin.
4. Pulse **Seleccionar horario** en una disponibilidad.
5. Compruebe los datos del horario en `solicitar-cita.xhtml`.
6. Seleccione un motivo activo.
7. Pulse **Solicitar cita**.
8. Compruebe el mensaje de éxito.
9. Abra **Mis citas** y localice la cita recién creada.
10. Pulse **Cancelar cita**.
11. Compruebe el mensaje de cancelación y la ausencia del botón para una cita
    ya cancelada.
12. Regrese a horarios y compruebe que el bloque liberado puede reservarse de
    nuevo.

Durante el flujo se espera que PostgreSQL registre la cita y sus cambios de
estado sin dejar operaciones parciales.

## Pruebas reproducibles

### Casos positivos

| Prueba | Procedimiento | Resultado esperado |
| --- | --- | --- |
| Solicitud válida | Seleccionar un horario disponible, elegir un motivo activo y enviar | Se crea una cita, la disponibilidad queda ocupada y se registra `cambio_estado` |
| Cancelación y reutilización | Cancelar una cita propia y volver a consultar horarios | La cita queda `CANCELADA`, se registra el cambio y el horario vuelve a estar disponible |

### Casos negativos

| Prueba | Procedimiento | Resultado esperado |
| --- | --- | --- |
| Motivo obligatorio | Intentar enviar la solicitud sin seleccionar motivo | JSF rechaza el formulario y muestra un mensaje comprensible |
| Identificador inválido | Abrir la solicitud con un identificador inexistente o manipulado | El servidor rechaza la solicitud sin modificar la base de datos |
| Doble reserva | Abrir el mismo horario en dos sesiones antes de confirmar y enviar ambas solicitudes | Solo una solicitud se confirma; la otra muestra un error comprensible |
| Cancelación inválida | Intentar cancelar una cita ajena o ya cancelada | El servidor rechaza el cambio y no altera la disponibilidad ni el historial |

Para comprobar la persistencia sin exponer credenciales, ejecute localmente:

```sql
SELECT id, id_estudiante, id_disponibilidad, id_motivo, estado
FROM cita
ORDER BY id DESC;

SELECT id, id_cita, estado_anterior, estado_nuevo, fecha_cambio
FROM cambio_estado
ORDER BY id DESC;

SELECT id, estado
FROM disponibilidad
ORDER BY id;
```

## Estado de verificación

| Comprobación | Estado | Observación |
| --- | --- | --- |
| Fuentes JSF y componentes PrimeFaces presentes | **VERIFICADO** | Existen vistas XHTML, Managed Beans y configuración de `FacesServlet` |
| Scripts PostgreSQL presentes | **VERIFICADO** | Existen esquema, datos sintéticos y migración |
| Compilación `mvn clean package` | **VERIFICADO** | `BUILD SUCCESS` obtenido el 25 de septiembre de 2026 |
| Flujo manual en Tomcat y PostgreSQL | **VERIFICADO** | Ejecutado previamente en el entorno local del equipo |
| Evidencia visual específica de P03 | **VERIFICADO** | `docs` conserva actualmente capturas de P02 |

## Índice de evidencias R03

| Criterio R03 | Evidencia |
| --- | --- |
| 1. Uso correcto de JSF y PrimeFaces — 3 pts | `docs/evidencias/01_inicio_p03.png`, `docs/evidencias/02_horarios_primefaces.png`, `docs/evidencias/03_formulario_solicitud.png`, `docs/evidencias/06_mis_citas.png` |
| 2. Formularios, validaciones y componentes — 3 pts | `docs/evidencias/03_formulario_solicitud.png`, `docs/evidencias/04_validacion_motivo_obligatorio.png`, `docs/evidencias/05_cita_creada.png` |
| 3. Persistencia en PostgreSQL — 2.5 pts | `docs/evidencias/05_cita_creada.png`, `docs/evidencias/07_cancelacion_cita.png`, `docs/evidencias/09_persistencia_postgresql.png` |
| 4. Flujo interactivo y experiencia de usuario — 2 pts | `docs/evidencias/01_inicio_p03.png`, `docs/evidencias/02_horarios_primefaces.png`, `docs/evidencias/06_mis_citas.png`, `docs/evidencias/07_cancelacion_cita.png`, `docs/evidencias/08_horario_liberado.png` |
| 5. Evidencia, README y repositorio — 1.5 pts | `README.md` y carpeta `docs/evidencias/` |

Las nuevas evidencias deben usar el prefijo estable indicado por la guía, por
ejemplo `P03_EQUIPO_NN`, sustituyendo `NN` por el número real del equipo. Las
capturas de P02 se conservan como trazabilidad histórica y no deben presentarse
como evidencia visual de JSF/PrimeFaces.

## Limitaciones

- No existe autenticación real. Los Managed Beans usan el estudiante sintético
  con identificador `1` para demostrar el flujo y la autorización en servidor.
- Solo se implementa la experiencia del estudiante.
- No existe una pantalla de confirmación por asesor ni una interfaz de
  coordinación en este incremento.
- Los JSP y Servlets de P02 siguen en el proyecto durante la migración.
- El despliegue descrito es local sobre Tomcat 9; no se requiere infraestructura
  externa.


## Integrantes y contribuciones

| Integrante | Contribución documentada en P02 | Contribución P03 |
| --- | --- | --- |
| Uriel Cruz Herrera | Modelo de datos y DAO con transacciones | Validaciones, vistas XHTML y navegación del flujo principal |
| Erick Romero García | Servlets del flujo y vistas JSP |  Integración de JSF y PrimeFaces, Managed Beans, pruebas funcionales y verificación del flujo de citas |
| Josue Sánchez Valente | Scripts SQL, README y evidencia documental | Persistencia PostgreSQL, scripts SQL, documentación, README y evidencias |


