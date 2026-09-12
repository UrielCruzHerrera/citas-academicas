package mx.uv.dsw.citas.modelo;

import java.time.LocalDateTime;

public class Cita {
    private int id;
    private String estudiante;
    private String asesor;
    private String fechaHora;
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
    public String getMotivo() { return motivo; }
    public void setMotivo(String m) { this.motivo = m; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime f) { this.fechaSolicitud = f; }
}