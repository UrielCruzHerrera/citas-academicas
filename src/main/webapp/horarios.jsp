<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>Horarios disponibles</title></head>
<body>
    <h1>Horarios disponibles</h1>
    <c:if test="${not empty error}">
        <p role="alert" style="color:#b00020">${error}</p>
    </c:if>
    <table border="1" cellpadding="8">
        <thead>
            <tr><th>Asesor</th><th>Fecha</th><th>Inicio</th><th>Fin</th><th>Acción</th></tr>
        </thead>
        <tbody>
        <c:forEach var="d" items="${disponibles}">
            <tr>
                <td>${d.asesor}</td>
                <td>${d.fecha}</td>
                <td>${d.horaInicio}</td>
                <td>${d.horaFin}</td>
                <td><a href="solicitar?id=${d.id}">Solicitar</a></td>
            </tr>
        </c:forEach>
        <c:if test="${empty disponibles}">
            <tr><td colspan="5">No hay horarios disponibles.</td></tr>
        </c:if>
        </tbody>
    </table>
    <p><a href="index.jsp">Inicio</a></p>
</body>
</html>