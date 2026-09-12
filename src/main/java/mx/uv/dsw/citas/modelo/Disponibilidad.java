package mx.uv.dsw.citas.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Disponibilidad {
    private int id;
    private String asesor;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;

    public Disponibilidad() {}
    public Disponibilidad(int id, String asesor, LocalDate fecha,
                          LocalTime horaInicio, LocalTime horaFin) {
        this.id = id; this.asesor = asesor; this.fecha = fecha;
        this.horaInicio = horaInicio; this.horaFin = horaFin;
        this.estado = "DISPONIBLE";
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAsesor() { return asesor; }
    public void setAsesor(String a) { this.asesor = a; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime h) { this.horaInicio = h; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime h) { this.horaFin = h; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }
}