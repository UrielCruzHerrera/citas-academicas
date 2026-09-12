package mx.uv.dsw.citas.modelo;

import java.time.LocalDateTime;

public class CambioEstado {
    private int id;
    private int idCita;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuario;
    private LocalDateTime fechaCambio;
    private String observacion;

    public CambioEstado() {}
    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCita() { return idCita; }
    public void setIdCita(int i) { this.idCita = i; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public void setEstadoAnterior(String e) { this.estadoAnterior = e; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public void setEstadoNuevo(String e) { this.estadoNuevo = e; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String u) { this.usuario = u; }
    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime f) { this.fechaCambio = f; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String o) { this.observacion = o; }
}