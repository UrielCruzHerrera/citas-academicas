# Citas Académicas - PR02 (Incremento Web 1.0)
**Materia:** Desarrollo de Sistemas Web  
**Proyecto:** PR02 - Sistema de citas académicas  
**Etapa:** Web 1.0 (JSP/Servlet + Tomcat + PostgreSQL)    
**Fecha:** Septiembre 2026
---

## Descripción

Aplicación web construida con **JSP + Servlets** que permite a un estudiante consultar horarios disponibles, solicitar citas académicas y cancelarlas. El servidor renderiza el HTML completo y valida las reglas de negocio antes de persistir en PostgreSQL.

## Stack

- Java 11
- Maven 3.9.9
- Apache Tomcat 9.0.115
- PostgreSQL 16
- JSTL 1.2
- Driver JDBC PostgreSQL 42.7.3

## Requisitos previos

- Java 11 instalado (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Tomcat 9 configurado
- PostgreSQL 16 corriendo en `localhost:5432`

## Instalación

### 1. Base de datos

```bash
psql -U postgres -c "CREATE DATABASE citas_academicas;"
psql -U postgres -d citas_academicas -f sql/01_schema.sql
psql -U postgres -d citas_academicas -f sql/02_datos_prueba.sql

### Nota sobre la prueba negativa de doble reserva

El dropdown de `/solicitar` solo muestra horarios DISPONIBLES (decisión de diseño).
Para verificar la validación del servidor, se modificó manualmente el DOM con
las DevTools del navegador para forzar el envío de un `idDisponibilidad` ya
ocupado. El servlet rechazó correctamente la operación sin alterar la base de
datos. Ver captura `docs/04_prueba_negativa_doble_reserva.png`.

Adicionalmente, la tabla `cita` cuenta con la restricción `uq_disponibilidad`
(UNIQUE en `id_disponibilidad`), lo que garantiza a nivel de base de datos que
jamás se pueda insertar dos citas para el mismo bloque, incluso si hubiera
una condición de carrera entre dos usuarios.