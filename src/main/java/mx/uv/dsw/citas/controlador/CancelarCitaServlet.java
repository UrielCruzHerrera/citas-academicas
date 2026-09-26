package mx.uv.dsw.citas.controlador;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.uv.dsw.citas.dao.CitaDAO;

@WebServlet("/cancelar")
public class CancelarCitaServlet extends HttpServlet {
    private final CitaDAO citaDAO = new CitaDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int idCita = Integer.parseInt(req.getParameter("idCita"));
            if (idCita <= 0) {
                throw new NumberFormatException("Identificador no positivo");
            }
            int idUsuario = 1; // Usuario sintético fijado en el servidor.
            boolean ok = citaDAO.cancelar(idCita, idUsuario, "Cancelada por el estudiante");

            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/mis-citas?cancelada=1");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                req.setAttribute("error",
                    "No se puede cancelar: la cita no existe, no te pertenece o ya no está activa.");
                req.setAttribute("citas", citaDAO.listarPorEstudiante(idUsuario));
                req.getRequestDispatcher("/mis-citas.jsp").forward(req, resp);
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", "El identificador de la cita debe ser un entero positivo.");
            try {
                req.setAttribute("citas", citaDAO.listarPorEstudiante(1));
            } catch (SQLException ex) {
                throw new ServletException("No se pudieron consultar las citas.", ex);
            }
            req.getRequestDispatcher("/mis-citas.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}