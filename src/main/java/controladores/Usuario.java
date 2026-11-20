package controladores;

public class Usuario {
    private int idUsuario;
    private String nombreResponsable;
    private String correo;
    private String rol;
    private String estatus;
    private int idEstado;
    private String nombreEstado;

    public Usuario(int idUsuario, String nombreResponsable, String correo, String rol, int idEstado) {
        this(idUsuario, nombreResponsable, correo, rol, "activo", idEstado, "Sin asignar");
    }

    public Usuario(int idUsuario, String nombreResponsable, String correo, String rol,
                   String estatus, int idEstado, String nombreEstado) {
        this.idUsuario = idUsuario;
        this.nombreResponsable = nombreResponsable;
        this.correo = correo;
        this.rol = rol;
        this.estatus = estatus;
        this.idEstado = idEstado;
        this.nombreEstado = nombreEstado != null ? nombreEstado : "Sin asignar";
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombreResponsable() { return nombreResponsable; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public String getEstatus() { return estatus; }
    public int getIdEstado() { return idEstado; }
    public String getNombreEstado() { return nombreEstado; }
}
