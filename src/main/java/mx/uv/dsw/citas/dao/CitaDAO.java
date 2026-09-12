package mx.uv.dsw.citas.dao;

import mx.uv.dsw.citas.modelo.Cita;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CitaDAO {

    /** Crea cita + bitácora + marca disponibilidad OCUPADO en UNA transacción. */
    public int crear(int idEstudiante, int idDisponibilidad, int idMotivo)
            throws SQLException {
        String insertCita = "INSERT INTO cita (id_estudiante, id_disponibilidad, id_motivo, estado) " +
                            "VALUES (?, ?, ?, 'SOLICITADA') RETURNING id";
        String insertBitacora = "INSERT INTO cambio_estado (id_cita, estado_anterior, estado_nuevo, id_usuario, observacion) " +
                                "VALUES (?, NULL, 'SOLICITADA', ?, 'Cita creada por el estudiante')";

        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idCita;
                try (PreparedStatement ps = cn.prepareStatement(insertCita)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, idDisponibilidad);
                    ps.setInt(3, idMotivo);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        idCita = rs.getInt("id");
                    }
                }
                try (PreparedStatement ps = cn.prepareStatement(insertBitacora)) {
                    ps.setInt(1, idCita);
                    ps.setInt(2, idEstudiante);
                    ps.executeUpdate();
                }
                new DisponibilidadDAO().marcarOcupado(idDisponibilidad, cn);
                cn.commit();
                return idCita;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public List<Cita> listarPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT c.id, e.nombre AS estudiante, a.nombre AS asesor, " +
                     "d.fecha, d.hora_inicio, d.hora_fin, m.nombre AS motivo, c.estado " +
                     "FROM cita c " +
                     "JOIN usuario e ON e.id = c.id_estudiante " +
                     "JOIN disponibilidad d ON d.id = c.id_disponibilidad " +
                     "JOIN usuario a ON a.id = d.id_asesor " +
                     "JOIN motivo m ON m.id = c.id_motivo " +
                     "WHERE c.id_estudiante = ? " +
                     "ORDER BY d.fecha DESC, d.hora_inicio DESC";
        List<Cita> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cita c = new Cita();
                    c.setId(rs.getInt("id"));
                    c.setEstudiante(rs.getString("estudiante"));
                    c.setAsesor(rs.getString("asesor"));
                    c.setFechaHora(rs.getDate("fecha") + " " +
                                   rs.getTime("hora_inicio") + "-" +
                                   rs.getTime("hora_fin"));
                    c.setMotivo(rs.getString("motivo"));
                    c.setEstado(rs.getString("estado"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    /** Cancela una cita: valida estado, cambia a CANCELADA, registra bitácora y libera bloque. */
    public boolean cancelar(int idCita, int idUsuario, String observacion)
            throws SQLException {
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try {
                // 1. Leer cita con bloqueo
                int idDisponibilidad;
                String estadoActual;
                try (PreparedStatement ps = cn.prepareStatement(
                        "SELECT estado, id_disponibilidad FROM cita WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, idCita);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { cn.rollback(); return false; }
                        estadoActual = rs.getString("estado");
                        idDisponibilidad = rs.getInt("id_disponibilidad");
                    }
                }
                // 2. Validar transición
                if (!"SOLICITADA".equals(estadoActual) && !"CONFIRMADA".equals(estadoActual)) {
                    cn.rollback();
                    return false;
                }
                // 3. Actualizar cita
                try (PreparedStatement ps = cn.prepareStatement(
                        "UPDATE cita SET estado = 'CANCELADA' WHERE id = ?")) {
                    ps.setInt(1, idCita);
                    ps.executeUpdate();
                }
                // 4. Bitácora
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO cambio_estado (id_cita, estado_anterior, estado_nuevo, id_usuario, observacion) " +
                        "VALUES (?, ?, 'CANCELADA', ?, ?)")) {
                    ps.setInt(1, idCita);
                    ps.setString(2, estadoActual);
                    ps.setInt(3, idUsuario);
                    ps.setString(4, observacion);
                    ps.executeUpdate();
                }
                // 5. Liberar disponibilidad
                new DisponibilidadDAO().marcarDisponible(idDisponibilidad, cn);
                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
}