package controladores;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexionBDM {

    private static final String URL_PROD = "jdbc:mysql://localhost:3307/red_medica";
    private static final String URL_TEST = "jdbc:mysql://localhost:3307/red_medica_test";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    private static boolean modoPruebas = false;
    private static String urlForzada = null;
    private static Connection conexion = null;

    public static void setModoPruebas(boolean activar) {
        cerrarConexion();
        modoPruebas = activar;
        urlForzada = null;
    }

    public static void forzarUrl(String nuevaUrl) {
        cerrarConexion();
        urlForzada = nuevaUrl;
    }

    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
            conexion = null;
        } catch (SQLException ignored) {}
    }

    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            String urlActiva = (urlForzada != null) ? urlForzada : (modoPruebas ? URL_TEST : URL_PROD);
            conexion = DriverManager.getConnection(urlActiva, USUARIO, PASSWORD);
        }
        return conexion;
    }

    public static void iniciarTransaccion() throws SQLException {
        getConexion().setAutoCommit(false);
    }

    public static void confirmarTransaccion() throws SQLException {
        getConexion().commit();
        getConexion().setAutoCommit(true);
    }

    public static void revertirTransaccion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.rollback();
                conexion.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            System.err.println("Error grave al intentar revertir la transacción: " + ex.getMessage());
        }
    }
}