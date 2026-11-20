package controladores;

public class Sesion {
    public static int idUsuario;
    public static String nombre;
    public static String rol;

    public static void cerrar() {
        idUsuario = 0;
        nombre = null;
        rol = null;
    }
}
