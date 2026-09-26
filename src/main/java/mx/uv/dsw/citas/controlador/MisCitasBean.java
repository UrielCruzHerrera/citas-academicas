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
import mx.uv.dsw.citas.modelo.Cita;

/** Consulta y cancelación JSF para el estudiante sintético de esta etapa. */
@ManagedBean(name = "misCitasBean")
@ViewScoped
public class MisCitasBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MisCitasBean.class.getName());
    private static final int ID_ESTUDIANTE_ACTUAL = 1;

    private List<Cita> citas = new ArrayList<>();
    private boolean errorCarga;

    public void cargar() {
        errorCarga = false;
        try {
            citas = new CitaDAO().listarPorEstudiante(ID_ESTUDIANTE_ACTUAL);
        } catch (SQLException e) {
            citas = new ArrayList<>();
            errorCarga = true;
            LOGGER.log(Level.SEVERE, "No se pudieron consultar las citas del estudiante", e);
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "No pudimos cargar tus citas. Inténtalo nuevamente en unos momentos.");
        }
    }

    public void cancelar(Cita cita) {
        if (cita == null || cita.getId() <= 0 || !contiene(cita.getId())) {
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "La cita seleccionada no es válida. Actualiza la lista e inténtalo nuevamente.");
            return;
        }
        if (!puedeCancelar(cita)) {
            mensaje(FacesMessage.SEVERITY_WARN,
                    "La cita ya no puede cancelarse porque su estado cambió.");
            cargarSilenciosamente();
            return;
        }

        try {
            boolean cancelada = new CitaDAO().cancelar(
                    cita.getId(), ID_ESTUDIANTE_ACTUAL, "Cancelada por el estudiante");
            if (cancelada) {
                cargarSilenciosamente();
                mensaje(FacesMessage.SEVERITY_INFO,
                        "La cita fue cancelada y el horario volvió a estar disponible.");
            } else {
                cargarSilenciosamente();
                mensaje(FacesMessage.SEVERITY_WARN,
                        "No se pudo cancelar la cita. Puede que no te pertenezca o que su estado haya cambiado.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo cancelar la cita", e);
            mensaje(FacesMessage.SEVERITY_ERROR,
                    "No pudimos cancelar la cita en este momento. Inténtalo nuevamente.");
        }
    }

    public boolean puedeCancelar(Cita cita) {
        return cita != null && ("SOLICITADA".equals(cita.getEstado())
                || "CONFIRMADA".equals(cita.getEstado()));
    }

    private boolean contiene(int idCita) {
        return citas.stream().anyMatch(cita -> cita.getId() == idCita);
    }

    private void cargarSilenciosamente() {
        try {
            citas = new CitaDAO().listarPorEstudiante(ID_ESTUDIANTE_ACTUAL);
            errorCarga = false;
        } catch (SQLException e) {
            citas = new ArrayList<>();
            errorCarga = true;
            LOGGER.log(Level.SEVERE, "No se pudieron actualizar las citas", e);
        }
    }

    private void mensaje(FacesMessage.Severity severity, String texto) {
        FacesContext.getCurrentInstance().addMessage(
                null, new FacesMessage(severity, texto, null));
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public boolean isErrorCarga() {
        return errorCarga;
    }
}
