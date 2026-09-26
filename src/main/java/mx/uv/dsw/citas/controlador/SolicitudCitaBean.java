package mx.uv.dsw.citas.controlador;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import mx.uv.dsw.citas.dao.CitaDAO;
import mx.uv.dsw.citas.dao.DisponibilidadDAO;
import mx.uv.dsw.citas.dao.MotivoDAO;
import mx.uv.dsw.citas.modelo.Disponibilidad;
import mx.uv.dsw.citas.modelo.Motivo;

/** Formulario JSF; toda escritura y su validación transaccional permanecen en CitaDAO. */
@ManagedBean(name = "solicitudCitaBean")
@ViewScoped
public class SolicitudCitaBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SolicitudCitaBean.class.getName());
    private static final int ID_ESTUDIANTE_DEMO = 1;

    private int idDisponibilidad;
    private int idMotivo;
    private int idCitaCreada;
    private Disponibilidad horario;
    private List<Motivo> motivos = new ArrayList<>();
    private boolean horarioValido;

    // Se invoca una sola vez al abrir la vista; el id no se vuelve a tomar del POST.
    public void inicializar() {
        String id = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
        try {
            idDisponibilidad = Integer.parseInt(id);
            if (idDisponibilidad <= 0) {
                throw new NumberFormatException("Identificador no positivo");
            }
        } catch (NumberFormatException e) {
            error("Selecciona un horario válido desde la consulta de horarios.");
            return;
        }
        try {
            DisponibilidadDAO dao = new DisponibilidadDAO();
            horario = dao.buscarPorId(idDisponibilidad);
            if (horario == null) {
                error("El horario seleccionado no existe. Elige otro horario.");
                return;
            }
            // Incluye fecha/hora futura, estado DISPONIBLE y asesor habilitado.
            horarioValido = dao.estaDisponible(idDisponibilidad);
            if (!horarioValido) {
                error("El horario ya pasó o dejó de estar disponible. Elige otro horario.");
                return;
            }
            motivos = new MotivoDAO().listarActivos();
            if (motivos.isEmpty()) {
                error("No hay motivos disponibles por el momento. Inténtalo más tarde.");
            }
        } catch (SQLException e) {
            horarioValido = false;
            LOGGER.log(Level.SEVERE, "No se pudo cargar el formulario de solicitud", e);
            error("No pudimos cargar la solicitud. Vuelve a horarios e inténtalo nuevamente.");
        }
    }

    public void solicitar() {
        if (idCitaCreada > 0) {
            mensaje(FacesMessage.SEVERITY_INFO, "Esta cita ya fue solicitada correctamente.");
            return;
        }
        if (!horarioValido || idDisponibilidad <= 0 || horario == null) {
            error("Selecciona un horario válido desde la consulta de horarios.");
            return;
        }
        if (idMotivo <= 0) {
            error("Selecciona un motivo para la cita.");
            return;
        }
        try {
            if (!new DisponibilidadDAO().estaDisponible(idDisponibilidad)) {
                horarioValido = false;
                error("El horario ya pasó o dejó de estar disponible. Elige otro horario.");
                return;
            }
            // Releer el catálogo: un motivo pudo desactivarse con el formulario abierto.
            motivos = new MotivoDAO().listarActivos();
            boolean motivoValido = motivos.stream().anyMatch(m -> m.getId() == idMotivo);
            if (!motivoValido) {
                idMotivo = 0;
                error("El motivo ya no está disponible. Selecciona uno de la lista actual.");
                return;
            }
            // El DAO vuelve a validar dentro de la transacción y evita reservas simultáneas.
            idCitaCreada = new CitaDAO().crear(ID_ESTUDIANTE_DEMO, idDisponibilidad, idMotivo);
            horarioValido = false;
            mensaje(FacesMessage.SEVERITY_INFO, "Cita solicitada correctamente. Folio: " + idCitaCreada + ".");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo registrar la solicitud de cita", e);
            if ("23505".equals(e.getSQLState())) {
                horarioValido = false;
                error("Ese horario acaba de ser reservado. Elige otro horario.");
            } else if ("P0001".equals(e.getSQLState()) || "23503".equals(e.getSQLState())) {
                error("No se pudo solicitar la cita. Verifica que el horario, el motivo y el estudiante sigan habilitados.");
            } else {
                error("No pudimos registrar la cita en este momento. Inténtalo nuevamente.");
            }
        }
    }

    private void error(String texto) {
        FacesContext.getCurrentInstance().validationFailed();
        mensaje(FacesMessage.SEVERITY_ERROR, texto);
    }

    private void mensaje(FacesMessage.Severity gravedad, String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(gravedad, texto, null));
    }

    public Disponibilidad getHorario() { return horario; }
    public List<Motivo> getMotivos() { return motivos; }
    public int getIdMotivo() { return idMotivo; }
    public void setIdMotivo(int idMotivo) { this.idMotivo = idMotivo; }
    public int getIdCitaCreada() { return idCitaCreada; }
    public boolean isPuedeSolicitar() {
        return horarioValido && !motivos.isEmpty() && idCitaCreada == 0;
    }
}
