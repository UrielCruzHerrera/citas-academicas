package mx.uv.dsw.citas.modelo;

public class Usuario {
    private int id;
    private String nombre;
    private String email;
    private String rol;

    public Usuario() {}
    public Usuario(int id, String nombre, String email, String rol) {
        this.id = id; this.nombre = nombre;
        this.email = email; this.rol = rol;
    }
    // getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getRol() { return rol; }
    public void setRol(String r) { this.rol = r; }
}
