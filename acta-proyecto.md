
# Acta breve de proyecto — PR02 Sistema de Citas Académicas

**Experiencia educativa:** Desarrollo de Sistemas Web  
**Proyecto:** PR02 — Sistema de citas académicas  
**Incremento actual:** Web 1.0 (JSP/Servlet + Tomcat 9 + PostgreSQL 16)  
**Equipo:** Uriel Cruz, Erick Romero García, Josue Sánchez Valente 
**Facilitador:** Dr. Gabriel Rodríguez Vásquez  
**Fecha:** Septiembre 2026

---

## 1. Problema

Estudiantes y personal académico coordinan asesorías mediante mensajes aislados (WhatsApp, correo, conversaciones presenciales), lo que provoca:

- Cruces de horario entre múltiples solicitudes.
- Falta de trazabilidad sobre qué cita fue confirmada, cancelada o atendida.
- Dificultad para que coordinación escolar supervise la demanda real de asesorías.

No existe un registro único que permita al estudiante consultar disponibilidad real y al asesor administrar su agenda con reglas claras.

---

## 2. Usuarios

| Rol | Necesidad principal |
| :--- | :--- |
| **Estudiante** | Consultar horarios disponibles, solicitar una cita y cancelarla si es necesario. |
| **Asesor académico** | Publicar bloques de disponibilidad y confirmar o atender las citas solicitadas. |
| **Coordinación** | Supervisar la demanda de asesorías y el uso de los espacios académicos. |

Para este incremento Web 1.0 se implementa únicamente la vista del **estudiante**; los roles de asesor y coordinación se abordarán en incrementos posteriores.

---

## 3. Alcance mínimo (incremento Web 1.0)

**Incluido en este incremento:**

- Catálogo de motivos de asesoría.
- Consulta de bloques horarios disponibles por asesor.
- Solicitud de cita con validación de doble reserva.
- Consulta de citas propias del estudiante.
- Cancelación de cita con liberación del bloque horario.
- Bitácora de cambios de estado por cada transición.

**Excluido explícitamente (según C11):**

- Videollamada o conferencia remota.
- Expediente escolar oficial.
- Notificaciones reales de pago.
- Administración avanzada de identidad o roles.
- Confirmación automática por parte del asesor (queda para Web 2.0).

---

## 4. Flujo principal

```
Estudiante entra a /horarios
       │
       ▼
Selecciona un bloque DISPONIBLE
       │
       ▼
Envía POST a /solicitar con idDisponibilidad e idMotivo
       │
       ▼
Servlet valida disponibilidad (¿sigue en estado DISPONIBLE?)
       │
   ┌───┴────┐
   │        │
   ▼        ▼
  SÍ       NO ──► Mensaje de error, sin insertar cita
   │
   ▼
Inserta cita + registra bitácora + marca bloque OCUPADO (misma transacción)
   │
   ▼
Redirige a /mis-citas con confirmación
   │
   ▼
Estudiante puede cancelar → bloque vuelve a DISPONIBLE
```

---

## 5. Modelo de datos (5 entidades)

| Entidad | Propósito | Relaciones principales |
| :--- | :--- | :--- |
| `usuario` | Personas con rol ESTUDIANTE, ASESOR o COORDINACION. | Referenciada por `disponibilidad`, `cita`, `cambio_estado`. |
| `motivo` | Catálogo de motivos de asesoría. | Referenciada por `cita`. |
| `disponibilidad` | Bloques horarios ofrecidos por cada asesor. | FK a `usuario`; UNIQUE (id_asesor, fecha, hora_inicio). |
| `cita` | Solicitudes registradas por estudiantes. | FK a `usuario`, `disponibilidad`, `motivo`; UNIQUE (id_disponibilidad). |
| `cambio_estado` | Bitácora de transiciones de estado. | FK a `cita` y `usuario`. |

**Restricciones de integridad destacadas:**

- `CHECK (hora_fin > hora_inicio)` en `disponibilidad`.
- `CHECK (estado IN (...))` en `disponibilidad`, `cita` y `usuario`.
- `UNIQUE (id_disponibilidad)` en `cita` para impedir doble reserva a nivel de BD.
- Transacciones con `AUTOCOMMIT=false` + `rollback` en caso de error.

---

## 6. Riesgo principal y control aplicado

**Riesgo identificado (según C11):** Exposición de motivos y horarios de asesoría; pérdida de integridad si dos estudiantes intentan reservar el mismo bloque simultáneamente.

**Controles implementados en este incremento:**

1. **Validación en el servlet** antes de insertar: `DisponibilidadDAO.estaDisponible(idDisponibilidad)`.
2. **Restricción `UNIQUE`** en `cita.id_disponibilidad` como respaldo a nivel de base de datos.
3. **Transacción atómica** en `CitaDAO.crear()`: si falla la inserción de la cita, la bitácora o el cambio de estado del bloque, se revierte todo con `rollback()`.
4. **Bloqueo pesimista** (`SELECT ... FOR UPDATE`) en `CitaDAO.cancelar()` para evitar condiciones de carrera durante la cancelación.
5. **Uso exclusivo de datos sintéticos** (usuarios y motivos ficticios). Sin credenciales reales ni información personal.

---

## 7. Stack tecnológico por etapa

| Etapa | Stack | Capacidad observable que agrega |
| :--- | :--- | :--- |
| **Web 1.0** (este incremento) | JSP + Servlets + Tomcat 9 + PostgreSQL 16 | Renderizado en servidor. Ciclo HTTP completo con códigos 200, 302, 404. Validación de reglas de negocio en el servidor. |
| Web 2.0 (próximo) | JSF + PrimeFaces | Componentes interactivos, validación enriquecida y gestión de estado en servidor sin recargar toda la página. |
| Web 3.0 | Spring Boot (API REST) + Angular | Desacoplamiento cliente-servidor. Consumo de datos JSON por múltiples clientes. |
| Web 4.0 | IoT simulado (evento de ocupación de cubículo) | Reacción automática del sistema ante eventos del contexto físico sin intervención humana. |

---

## 8. Criterios de aceptación cubiertos

| CA | Descripción | Estado |
| :--- | :--- | :---: |
| CA01 | Flujo completo desde base sintética con cambio persistido. | Cumplido |
| CA02 | Caso inválido rechazado sin alterar la BD. | Cumplido |
| CA03 | Respuesta coherente con estados HTTP correctos (302 en éxito, mensaje en error). | Cumplido |
| CA04 | Evento IoT simulado (se abordará en Web 4.0). | ⏳ Pendiente por etapa |
| CA05 | Otra persona puede reproducir el incremento mediante README. | Cumplido |
| CA06 | Autoría, contribución individual y limitaciones documentadas. | Cumplido |

---

## 9. Limitaciones declaradas

- Este incremento **no implementa autenticación real**; el `idEstudiante` se fija en `1` como valor de demostración. La gestión de sesiones se abordará en Web 2.0 con JSF.
- El sistema **no notifica por correo** al asesor; la confirmación se hará en el siguiente incremento.
- La **cancelación solo funciona para el estudiante dueño de la cita** en la práctica (por el id fijo), pero el servlet ya valida la transición de estado de forma genérica para soportar múltiples roles en el futuro.
- **No se usaron datos reales** en ningún momento. Toda la información en `sql/02_datos_prueba.sql` es ficticia.

---

## 10. Contribución individual

| Integrante | Contribución |
| :--- | :--- |
| Uriel Cruz Herrera | Diseño del modelo de datos, implementación de DAOs con transacciones, servlets del flujo principal, vistas JSP con JSTL, scripts SQL, README y evidencia documental. |

---

## 11. Próximo incremento

En **P03 (Web 2.0)** se migrará el flujo a **JSF/PrimeFaces**, agregando:

- Gestión de sesión para identificar al estudiante autenticado.
- Componentes interactivos (`p:dataTable`, `p:calendar`) con validación de conflictos en tiempo real.
- Confirmación de citas por parte del asesor.
- Filtros y búsqueda sobre el catálogo de disponibilidad.

El modelo PostgreSQL, los scripts SQL y los casos de prueba se conservan íntegros, cumpliendo el principio de trazabilidad acumulativa del curso.

Atentamente: 
Estudiantes responsables del incremento P02
