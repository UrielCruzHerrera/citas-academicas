package mx.uv.dsw.citas.dao;

import mx.uv.dsw.citas.modelo.Disponibilidad;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadDAO {

    public List<Disponibilidad> listarDisponibles() throws SQLException {
        String sql = "SELECT d.id, u.nombre AS asesor, d.fecha, d.hora_inicio, d.hora_fin " +
                     "FROM disponibilidad d " +
                     "JOIN usuario u ON u.id = d.id_asesor " +
                     "WHERE d.estado = 'DISPONIBLE' AND d.fecha >= CURRENT_DATE " +
                     "ORDER BY d.fecha, d.hora_inicio";
        List<Disponibilidad> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Disponibilidad(
                    rs.getInt("id"),
                    rs.getString("asesor"),
                    rs.getDate("fecha").toLocalDate(),
                    rs.getTime("hora_inicio").toLocalTime(),
                    rs.getTime("hora_fin").toLocalTime()
                ));
            }
        }
        return lista;
    }

    public boolean estaDisponible(int idDisponibilidad) throws SQLException {
        String sql = "SELECT estado FROM disponibilidad WHERE id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idDisponibilidad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "DISPONIBLE".equals(rs.getString("estado"));
            }
        }
    }

    public void marcarOcupado(int idDisponibilidad, Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE disponibilidad SET estado = 'OCUPADO' WHERE id = ?")) {
            ps.setInt(1, idDisponibilidad);
            ps.executeUpdate();
        }
    }

    public void marcarDisponible(int idDisponibilidad, Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "UPDATE disponibilidad SET estado = 'DISPONIBLE' WHERE id = ?")) {
            ps.setInt(1, idDisponibilidad);
            ps.executeUpdate();
        }
    }

    public Disponibilidad buscarPorId(int id) throws SQLException {
        String sql = "SELECT d.id, u.nombre AS asesor, d.fecha, d.hora_inicio, d.hora_fin " +
                     "FROM disponibilidad d JOIN usuario u ON u.id = d.id_asesor WHERE d.id = ?";
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Disponibilidad(
                        rs.getInt("id"), rs.getString("asesor"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getTime("hora_inicio").toLocalTime(),
                        rs.getTime("hora_fin").toLocalTime()
                    );
                }
            }
        }
        return null;
    }
}