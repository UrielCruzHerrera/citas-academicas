package mx.uv.dsw.citas.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Cita implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String estudiante;
    private String asesor;
    private String fechaHora;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private String estado;
    private LocalDateTime fechaSolicitud;

    public Cita() {}
    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String e) { this.estudiante = e; }
    public String getAsesor() { return asesor; }
    public void setAsesor(String a) { this.asesor = a; }
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String f) { this.fechaHora = f; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String m) { this.motivo = m; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime f) { this.fechaSolicitud = f; }
}
