package mx.uv.dsw.citas.controlador;

import mx.uv.dsw.citas.dao.CitaDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/mis-citas")
public class MisCitasServlet extends HttpServlet {
    private final CitaDAO citaDAO = new CitaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            int idEstudiante = 1; // Demo
            req.setAttribute("citas", citaDAO.listarPorEstudiante(idEstudiante));
            req.getRequestDispatcher("/mis-citas.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("error", "Error BD: " + e.getMessage());
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}