package mx.uv.dsw.citas.modelo;

public class Motivo {
    private int id;
    private String nombre;
    private String descripcion;

    public Motivo() {}
    public Motivo(int id, String nombre, String descripcion) {
        this.id = id; this.nombre = nombre; this.descripcion = descripcion;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
}