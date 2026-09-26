package mx.uv.dsw.citas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.dsw.citas.modelo.Motivo;

public class MotivoDAO {
    public List<Motivo> listarActivos() throws SQLException {
        String sql = "SELECT id, nombre, descripcion FROM motivo WHERE activo = TRUE ORDER BY id";
        List<Motivo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Motivo(rs.getInt("id"), rs.getString("nombre"),
                                     rs.getString("descripcion")));
            }
        }
        return lista;
    }
}