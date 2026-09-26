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
            String id = req.getParameter("id");
            if (id != null) {
                int seleccionado = identificador(id);
                if (dispDAO.estaDisponible(seleccionado)) {
                    req.setAttribute("idDisponibilidadSeleccionada", seleccionado);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    req.setAttribute("error", "El horario seleccionado ya no está disponible. Elija otro.");
                }
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", "El identificador del horario debe ser un entero positivo.");
        } catch (SQLException e) {
            throw new ServletException("No se pudo consultar el horario.", e);
        }
        mostrarFormulario(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int idDisponibilidad = identificador(req.getParameter("idDisponibilidad"));
            req.setAttribute("idDisponibilidadSeleccionada", idDisponibilidad);
            int idMotivo = identificador(req.getParameter("idMotivo"));
            req.setAttribute("idMotivoSeleccionado", idMotivo);
            int idEstudiante = 1; // Usuario sintético de demostración, fijado en el servidor.
            int idCita = citaDAO.crear(idEstudiante, idDisponibilidad, idMotivo);
            resp.sendRedirect(req.getContextPath() + "/mis-citas?creada=" + idCita);
            return;
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("error", "Seleccione un horario y un motivo con identificadores enteros positivos.");
        } catch (SQLException e) {
            if ("P0001".equals(e.getSQLState())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                req.setAttribute("error", e.getMessage());
            } else if ("23505".equals(e.getSQLState())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                req.setAttribute("error", "Ese horario ya tiene una cita. Elija otro.");
            } else {
                throw new ServletException("No se pudo registrar la cita.", e);
            }
        }
        mostrarFormulario(req, resp);
    }

    private int identificador(String valor) {
        int id = Integer.parseInt(valor);
        if (id <= 0) {
            throw new NumberFormatException("Identificador no positivo");
        }
        return id;
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("disponibles", dispDAO.listarDisponibles());
            req.setAttribute("motivos", motivoDAO.listarActivos());
            req.getRequestDispatcher("/solicitar.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("No se pudo cargar el formulario.", e);
        }
    }
}
