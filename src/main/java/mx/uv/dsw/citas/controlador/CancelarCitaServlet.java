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
            int idUsuario = 1; // Demo
            boolean ok = citaDAO.cancelar(idCita, idUsuario, "Cancelada por el estudiante");

            if (ok) {
                resp.sendRedirect(req.getContextPath() + "/mis-citas?cancelada=1");
            } else {
                req.setAttribute("error",
                    "No se puede cancelar: la cita ya no está activa.");
                req.setAttribute("citas", citaDAO.listarPorEstudiante(idUsuario));
                req.getRequestDispatcher("/mis-citas.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}