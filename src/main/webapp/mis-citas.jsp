<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head><meta charset="UTF-8"><title>Mis citas</title></head>
<body>
    <h1>Mis citas</h1>

    <c:if test="${param.creada != null}">
        <p style="color:green">Cita creada correctamente (ID: ${param.creada}).</p>
    </c:if>
    <c:if test="${param.cancelada != null}">
        <p style="color:green">Cita cancelada.</p>
    </c:if>
    <c:if test="${not empty error}">
        <p role="alert" style="color:#b00020">${error}</p>
    </c:if>

    <table border="1" cellpadding="8">
        <thead>
            <tr><th>ID</th><th>Asesor</th><th>Fecha/Hora</th><th>Motivo</th><th>Estado</th><th>Acción</th></tr>
        </thead>
        <tbody>
        <c:forEach var="c" items="${citas}">
            <tr>
                <td>${c.id}</td>
                <td>${c.asesor}</td>
                <td>${c.fechaHora}</td>
                <td>${c.motivo}</td>
                <td>${c.estado}</td>
                <td>
                    <c:if test="${c.estado == 'SOLICITADA' || c.estado == 'CONFIRMADA'}">
                        <form method="post" action="cancelar" style="display:inline">
                            <input type="hidden" name="idCita" value="${c.id}">
                            <button type="submit">Cancelar</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty citas}">
            <tr><td colspan="6">No tienes citas registradas.</td></tr>
        </c:if>
        </tbody>
    </table>
    <p><a href="horarios">Horarios</a> | <a href="index.jsp">Inicio</a></p>
</body>
</html>