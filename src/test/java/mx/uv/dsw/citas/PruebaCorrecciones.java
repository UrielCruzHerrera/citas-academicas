package mx.uv.dsw.citas;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.*;
import mx.uv.dsw.citas.controlador.*;
import mx.uv.dsw.citas.dao.*;

/** Regresión con PostgreSQL real. Ejecutar únicamente sobre una base temporal vacía. */
public class PruebaCorrecciones {
    private static final CitaDAO DAO = new CitaDAO();
    private static int comprobaciones;

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
        comprobaciones++;
    }

    private static void sql(String sentencia) throws SQLException {
        try (Connection cn = ConexionBD.obtener(); Statement st = cn.createStatement()) {
            st.execute(sentencia);
        }
    }

    private static int numero(String consulta) throws SQLException {
        try (Connection cn = ConexionBD.obtener(); Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(consulta)) {
            rs.next(); return rs.getInt(1);
        }
    }

    private static void rechazar(int estudiante, int horario, int motivo) throws SQLException {
        int citas = numero("SELECT count(*) FROM cita");
        int cambios = numero("SELECT count(*) FROM cambio_estado");
        try {
            DAO.crear(estudiante, horario, motivo);
            throw new AssertionError("Se aceptó una reserva inválida");
        } catch (SQLException ex) {
            verificar("P0001".equals(ex.getSQLState()) || "23505".equals(ex.getSQLState()),
                      "Debe rechazar por regla de negocio");
        }
        verificar(citas == numero("SELECT count(*) FROM cita"), "Rechazo sin citas nuevas");
        verificar(cambios == numero("SELECT count(*) FROM cambio_estado"), "Rechazo sin bitácoras nuevas");
    }

    private static class Solicitud extends SolicitarCitaServlet {
        void ejecutar(boolean post, Intercambio i) throws Exception {
            if (post) doPost(i.req, i.resp); else doGet(i.req, i.resp);
        }
    }
    private static class Cancelacion extends CancelarCitaServlet {
        void ejecutar(Intercambio i) throws Exception { doPost(i.req, i.resp); }
    }
    // Dobles HTTP mínimos; los Servlets y DAO utilizados son los reales.
    private static class Intercambio {
        final Map<String, String> parametros = new HashMap<>();
        final Map<String, Object> atributos = new HashMap<>();
        int estado = 200;
        String destino;
        final HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{HttpServletRequest.class}, (p, m, a) -> {
                switch (m.getName()) {
                    case "getParameter": return parametros.get(a[0]);
                    case "setAttribute": atributos.put((String) a[0], a[1]); return null;
                    case "getAttribute": return atributos.get(a[0]);
                    case "getContextPath": return "/citas-academicas";
                    case "getRequestDispatcher":
                        destino = (String) a[0];
                        return Proxy.newProxyInstance(getClass().getClassLoader(),
                            new Class<?>[]{RequestDispatcher.class}, (x, y, z) -> null);
                    default: return null;
                }
            });
        final HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[]{HttpServletResponse.class}, (p, m, a) -> {
                if ("setStatus".equals(m.getName())) estado = (int) a[0];
                if ("sendRedirect".equals(m.getName())) { estado = 302; destino = (String) a[0]; }
                return null;
            });
    }

    public static void main(String[] args) throws Exception {
        String url = System.getenv("CITAS_DB_URL");
        if (url == null || !url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/pr02_regresion")) {
            throw new IllegalStateException("Use la base temporal local pr02_regresion con puerto explícito.");
        }
        verificar(numero("SELECT count(*) FROM cita") == 0, "La base de prueba debe estar vacía");
        sql("INSERT INTO usuario(nombre,email,rol,password_hash) VALUES " +
            "('Otro estudiante','otro@example.invalid','ESTUDIANTE','demo')");
        sql("INSERT INTO motivo(nombre,activo) VALUES ('Inactivo',false)");
        sql("INSERT INTO disponibilidad(id_asesor,fecha,hora_inicio,hora_fin,estado) VALUES " +
            "(2,CURRENT_DATE-1,'08:00','08:30','DISPONIBLE')," +
            "(2,CURRENT_DATE+3,'08:00','08:30','BLOQUEADO')");

        Solicitud servlet = new Solicitud();
        Intercambio elegido = new Intercambio(); elegido.parametros.put("id", "2");
        servlet.ejecutar(false, elegido);
        verificar(Integer.valueOf(2).equals(elegido.atributos.get("idDisponibilidadSeleccionada")),
                  "Conservar la selección de un horario distinto del primero");
        verificar("/solicitar.jsp".equals(elegido.destino), "Mostrar formulario");
        for (String invalido : new String[]{null, "", "abc", "0", "-1", "2147483648"}) {
            Intercambio i = new Intercambio();
            i.parametros.put("idDisponibilidad", invalido); i.parametros.put("idMotivo", "1");
            servlet.ejecutar(true, i); verificar(i.estado == 400, "Validar horario mal formado");
            i = new Intercambio(); i.parametros.put("idDisponibilidad", "2");
            i.parametros.put("idMotivo", invalido); servlet.ejecutar(true, i);
            verificar(i.estado == 400, "Validar motivo mal formado");
            verificar(Integer.valueOf(2).equals(i.atributos.get("idDisponibilidadSeleccionada")),
                      "Conservar horario al corregir motivo");
            i = new Intercambio(); i.parametros.put("idCita", invalido);
            new Cancelacion().ejecutar(i); verificar(i.estado == 400, "Validar id de cancelación");
        }
        Intercambio malGet = new Intercambio(); malGet.parametros.put("id", "abc");
        servlet.ejecutar(false, malGet); verificar(malGet.estado == 400, "Validar GET manipulado");
        rechazar(1, 999999, 1); rechazar(1, 4, 1); rechazar(1, 5, 1);
        rechazar(1, 1, 999999); rechazar(1, 1, 4); rechazar(2, 1, 1);
        sql("UPDATE usuario SET activo=false WHERE id=1"); rechazar(1, 1, 1);
        sql("UPDATE usuario SET activo=true WHERE id=1");
        sql("UPDATE usuario SET activo=false WHERE id=2"); rechazar(1, 1, 1);
        sql("UPDATE usuario SET activo=true WHERE id=2");
        verificar(!new DisponibilidadDAO().estaDisponible(4), "No ofrecer fechas pasadas");

        int cita = DAO.crear(1, 1, 1);
        rechazar(4, 1, 1);
        verificar(!DAO.cancelar(cita, 4, "Ajena"), "Rechazar cancelación ajena");
        verificar(numero("SELECT count(*) FROM cambio_estado") == 1, "No registrar cancelación ajena");
        verificar(DAO.cancelar(cita, 1, "Propia"), "Cancelar cita propia");
        verificar(!DAO.cancelar(cita, 1, "Repetida"), "Rechazar segunda cancelación");
        verificar(!DAO.cancelar(999999, 1, "Inexistente"), "Rechazar cita inexistente");
        verificar(new DisponibilidadDAO().estaDisponible(1), "Liberar bloque");
        int nueva = DAO.crear(4, 1, 1);
        verificar(nueva != cita, "Crear nueva cita conservando historial");
        verificar(numero("SELECT count(*) FROM cita WHERE id_disponibilidad=1") == 2,
                  "Conservar ambas citas");
        Intercambio ajena = new Intercambio(); ajena.parametros.put("idCita", String.valueOf(nueva));
        new Cancelacion().ejecutar(ajena); verificar(ajena.estado == 400, "Servlet rechaza cita ajena");
        verificar(numero("SELECT count(*) FROM cita WHERE id="+nueva+" AND estado='SOLICITADA'") == 1,
                  "Cita ajena intacta");
        verificar(!new DisponibilidadDAO().estaDisponible(1), "Bloque ajeno sigue ocupado");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        Callable<Boolean> reservar = () -> {
            inicio.await();
            try { DAO.crear(1, 2, 1); return true; }
            catch (SQLException ex) {
                if (!"P0001".equals(ex.getSQLState()) && !"23505".equals(ex.getSQLState())) throw ex;
                return false;
            }
        };
        try {
            Future<Boolean> a = pool.submit(reservar), b = pool.submit(reservar); inicio.countDown();
            verificar(a.get(10, TimeUnit.SECONDS) ^ b.get(10, TimeUnit.SECONDS),
                      "Solo una reserva concurrente tiene éxito");
        } finally { pool.shutdownNow(); }
        verificar(numero("SELECT count(*) FROM cita WHERE id_disponibilidad=2") == 1,
                  "Sin doble reserva concurrente");
        // Provocar una falla posterior a tomar el bloque para comprobar rollback completo.
        sql("CREATE FUNCTION fallo_bitacora() RETURNS trigger LANGUAGE plpgsql AS $$ " +
            "BEGIN RAISE EXCEPTION 'Fallo de prueba'; END $$");
        sql("CREATE TRIGGER fallo_bitacora BEFORE INSERT ON cambio_estado " +
            "FOR EACH ROW EXECUTE PROCEDURE fallo_bitacora()");
        try {
            DAO.crear(1, 3, 1); throw new AssertionError("Debía fallar la bitácora");
        } catch (SQLException esperado) {
            verificar(new DisponibilidadDAO().estaDisponible(3), "Rollback libera el bloque");
            verificar(numero("SELECT count(*) FROM cita WHERE id_disponibilidad=3") == 0,
                      "Rollback elimina inserción parcial");
        } finally { sql("DROP TRIGGER fallo_bitacora ON cambio_estado"); sql("DROP FUNCTION fallo_bitacora()"); }
        Intercambio exito = new Intercambio(); exito.parametros.put("idDisponibilidad", "3");
        exito.parametros.put("idMotivo", "1"); servlet.ejecutar(true, exito);
        verificar(exito.estado == 302 && exito.destino.contains("/mis-citas?creada="),
                  "Solicitud válida redirige a mis citas");
        int propia = numero("SELECT id FROM cita WHERE id_disponibilidad=3");
        Intercambio cancelada = new Intercambio(); cancelada.parametros.put("idCita", String.valueOf(propia));
        new Cancelacion().ejecutar(cancelada);
        verificar(cancelada.estado == 302 && new DisponibilidadDAO().estaDisponible(3),
                  "Cancelación válida redirige y libera");
        System.out.println("VERIFICADO: " + comprobaciones + " comprobaciones de regresión.");
    }
}
