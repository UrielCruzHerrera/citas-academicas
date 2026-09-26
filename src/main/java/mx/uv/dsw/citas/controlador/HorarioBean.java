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
import mx.uv.dsw.citas.dao.DisponibilidadDAO;
import mx.uv.dsw.citas.modelo.Disponibilidad;

/** Consulta y selección local a la vista; seleccionar no reserva ni crea una cita. */
@ManagedBean(name = "horarioBean")
@ViewScoped
public class HorarioBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(HorarioBean.class.getName());

    private List<Disponibilidad> disponibilidades = new ArrayList<>();
    private Disponibilidad horarioSeleccionado;
    private boolean errorCarga;

    public void cargar() {
        horarioSeleccionado = null;
        errorCarga = false;
        try {
            disponibilidades = new DisponibilidadDAO().listarDisponibles();
        } catch (SQLException e) {
            disponibilidades = new ArrayList<>();
            errorCarga = true;
            LOGGER.log(Level.SEVERE, "No se pudieron consultar los horarios disponibles", e);
        }
    }

    public void seleccionar(Disponibilidad horario) {
        if (horario != null && disponibilidades.contains(horario)) {
            horarioSeleccionado = horario;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Horario seleccionado", "La selección todavía no reserva una cita."));
        }
    }

    public List<Disponibilidad> getDisponibilidades() {
        return disponibilidades;
    }

    public Disponibilidad getHorarioSeleccionado() {
        return horarioSeleccionado;
    }

    public boolean isErrorCarga() {
        return errorCarga;
    }
}
