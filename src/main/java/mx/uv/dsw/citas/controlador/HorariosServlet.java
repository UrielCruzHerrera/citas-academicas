package mx.uv.dsw.citas.controlador;

import mx.uv.dsw.citas.dao.DisponibilidadDAO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/horarios")
public class HorariosServlet extends HttpServlet {
    private final DisponibilidadDAO dao = new DisponibilidadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("disponibles", dao.listarDisponibles());
            req.getRequestDispatcher("/horarios.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("error", "Error BD: " + e.getMessage());
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}