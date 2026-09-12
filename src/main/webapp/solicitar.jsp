<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>Solicitar cita</title></head>
<body>
    <h1>Solicitar cita</h1>
    <c:if test="${not empty error}">
        <p role="alert" style="color:#b00020">${error}</p>
    </c:if>
    <form method="post" action="solicitar">
        <label for="idDisponibilidad">Horario:</label>
        <select name="idDisponibilidad" id="idDisponibilidad" required>
            <c:forEach var="d" items="${disponibles}">
                <option value="${d.id}">
                    ${d.asesor} - ${d.fecha} ${d.horaInicio}-${d.horaFin}
                </option>
            </c:forEach>
        </select>
        <br><br>
        <label for="idMotivo">Motivo:</label>
        <select name="idMotivo" id="idMotivo" required>
            <c:forEach var="m" items="${motivos}">
                <option value="${m.id}">${m.nombre}</option>
            </c:forEach>
        </select>
        <br><br>
        <button type="submit">Solicitar cita</button>
    </form>
    <p><a href="horarios">Volver a horarios</a> | <a href="index.jsp">Inicio</a></p>
</body>
</html>