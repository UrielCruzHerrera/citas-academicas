package mx.uv.dsw.citas.controlador;

import mx.uv.dsw.citas.dao.CitaDAO;
import mx.uv.dsw.citas.dao.DisponibilidadDAO;
import mx.uv.dsw.citas.dao.MotivoDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/solicitar")
public class SolicitarCitaServlet extends HttpServlet {

    private final CitaDAO citaDAO = new CitaDAO();
    private final DisponibilidadDAO dispDAO = new DisponibilidadDAO();
    private final MotivoDAO motivoDAO = new MotivoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("disponibles", dispDAO.listarDisponibles());
            req.setAttribute("motivos", motivoDAO.listarActivos());
            req.getRequestDispatcher("/solicitar.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("error", "Error BD: " + e.getMessage());
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int idDisponibilidad = Integer.parseInt(req.getParameter("idDisponibilidad"));
            int idMotivo         = Integer.parseInt(req.getParameter("idMotivo"));
            int idEstudiante     = 1; // Demo Web 1.0; en Web 3.0 vendrá del login

            // VALIDACIÓN NEGATIVA: doble reserva
            if (!dispDAO.estaDisponible(idDisponibilidad)) {
                req.setAttribute("error",
                    "El horario seleccionado ya fue reservado. Elija otro.");
                req.setAttribute("disponibles", dispDAO.listarDisponibles());
                req.setAttribute("motivos", motivoDAO.listarActivos());
                req.getRequestDispatcher("/solicitar.jsp").forward(req, resp);
                return;
            }

            int idCita = citaDAO.crear(idEstudiante, idDisponibilidad, idMotivo);
            resp.sendRedirect(req.getContextPath() + "/mis-citas?creada=" + idCita);

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("uq_disponibilidad")) {
                req.setAttribute("error", "Ese horario acaba de ser tomado.");
                try {
                    req.setAttribute("disponibles", dispDAO.listarDisponibles());
                    req.setAttribute("motivos", motivoDAO.listarActivos());
                } catch (SQLException ex) { /* log */ }
                req.getRequestDispatcher("/solicitar.jsp").forward(req, resp);
            } else {
                throw new ServletException(e);
            }
        }
    }
}