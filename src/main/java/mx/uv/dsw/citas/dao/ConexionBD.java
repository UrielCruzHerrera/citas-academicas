package mx.uv.dsw.citas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL  = System.getenv().getOrDefault(
            "CITAS_DB_URL", "jdbc:postgresql://localhost:5432/citas_academicas");
    private static final String USER = System.getenv().getOrDefault(
            "CITAS_DB_USER", "postgres");
    private static final String PASS = System.getenv().getOrDefault(
            "CITAS_DB_PASS", "postgres");

    public static Connection obtener() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL no encontrado", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}