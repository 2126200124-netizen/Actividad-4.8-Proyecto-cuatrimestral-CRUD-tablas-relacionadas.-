package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class DatosConsulta {

    public static class Hospital {
        private final String nombre;
        private final String estado;
        private final String direccion;
        private final String telefono;

        public Hospital(String nombre, String estado, String direccion, String telefono) {
            this.nombre = nombre;
            this.estado = estado;
            this.direccion = direccion;
            this.telefono = telefono;
        }
        public String getNombre() { return nombre; }
        public String getEstado() { return estado; }
        public String getDireccion() { return direccion; }
        public String getTelefono() { return telefono; }
    }

    public static class Medico {
        private final String nombre;
        private final List<String> especialidades;
        private final String hospital;
        private final String estado;

        public Medico(String nombre, List<String> especialidades, String hospital, String estado) {
            this.nombre = nombre;
            this.especialidades = especialidades;
            this.hospital = hospital;
            this.estado = estado;
        }
        public String getNombre() { return nombre; }
        public List<String> getEspecialidades() { return especialidades; }
        public String getEspecialidadesTexto() { return String.join(", ", especialidades); }
        public String getHospital() { return hospital; }
        public String getEstado() { return estado; }
    }

    public static class ServicioHospital {
        private final String hospital;
        private final String servicio;
        private final String caracteristicas;
        private final double costo;

        public ServicioHospital(String hospital, String servicio, String caracteristicas, double costo) {
            this.hospital = hospital;
            this.servicio = servicio;
            this.caracteristicas = caracteristicas;
            this.costo = costo;
        }
        public String getHospital() { return hospital; }
        public String getServicio() { return servicio; }
        public String getCaracteristicas() { return caracteristicas; }
        public double getCosto() { return costo; }
    }

    public static final List<Hospital> HOSPITALES = new ArrayList<>();
    public static final List<Medico> MEDICOS = new ArrayList<>();
    public static final List<ServicioHospital> SERVICIOS = new ArrayList<>();

    // Método para recargar los datos frescos desde MySQL cada vez que se abra la ventana
    public static void cargarDesdeBD() {
        HOSPITALES.clear();
        MEDICOS.clear();
        SERVICIOS.clear();

        cargarHospitales();
        cargarMedicos();
        cargarServicios();
    }

    private static void cargarHospitales() {
        String sql = "SELECT h.nombre, IFNULL(e.nombre, 'Sin estado') AS estado, " +
                     "IFNULL(h.direccion, '') AS direccion, IFNULL(h.telefono, '') AS telefono " +
                     "FROM hospitales h " +
                     "LEFT JOIN estados e ON h.id_estado = e.id_estado " +
                     "WHERE h.estatus = 'activo' " +
                     "ORDER BY h.nombre";

        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HOSPITALES.add(new Hospital(
                    rs.getString("nombre"),
                    rs.getString("estado"),
                    rs.getString("direccion"),
                    rs.getString("telefono")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar hospitales en consulta: " + ex.getMessage());
        }
    }

    private static void cargarMedicos() {
        String sql = "SELECT CONCAT(m.nombre, ' ', m.apellidos) AS nombre_completo, " +
                     "h.nombre AS hospital, " +
                     "IFNULL(e.nombre, 'Sin estado') AS estado, " +
                     "GROUP_CONCAT(esp.nombre SEPARATOR ', ') AS especialidades " +
                     "FROM medicos m " +
                     "INNER JOIN hospitales h ON m.id_hospital = h.id_hospital " +
                     "LEFT JOIN estados e ON h.id_estado = e.id_estado " +
                     "LEFT JOIN medico_especialidad me ON m.id_medico = me.id_medico " +
                     "LEFT JOIN especialidades esp ON me.id_especialidad = esp.id_especialidad AND esp.estatus = 'activo' " +
                     "WHERE m.estatus = 'activo' AND h.estatus = 'activo' " +
                     "GROUP BY m.id_medico, m.nombre, m.apellidos, h.nombre, e.nombre " +
                     "ORDER BY nombre_completo";

        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String espStr = rs.getString("especialidades");
                List<String> listaEsp = (espStr != null && !espStr.isEmpty())
                        ? Arrays.asList(espStr.split(", "))
                        : new ArrayList<>();

                MEDICOS.add(new Medico(
                    rs.getString("nombre_completo"),
                    listaEsp,
                    rs.getString("hospital"),
                    rs.getString("estado")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar médicos en consulta: " + ex.getMessage());
        }
    }

    private static void cargarServicios() {
        String sql = "SELECT h.nombre AS hospital, s.nombre AS servicio, " +
                     "IFNULL(hs.caracteristicas, '') AS caracteristicas, " +
                     "IFNULL(hs.costo, 0.0) AS costo " +
                     "FROM hospital_servicios hs " +
                     "INNER JOIN hospitales h ON hs.id_hospital = h.id_hospital " +
                     "INNER JOIN servicios s ON hs.id_servicio = s.id_servicio " +
                     "WHERE h.estatus = 'activo' AND s.estatus = 'activo' " +
                     "ORDER BY h.nombre, s.nombre";

        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SERVICIOS.add(new ServicioHospital(
                    rs.getString("hospital"),
                    rs.getString("servicio"),
                    rs.getString("caracteristicas"),
                    rs.getDouble("costo")
                ));
            }
        } catch (SQLException ex) {
            System.err.println("Error al cargar servicios en consulta: " + ex.getMessage());
        }
    }

    public static Hospital buscarHospital(String nombreHospital) {
        for (Hospital h : HOSPITALES) {
            if (h.getNombre().equals(nombreHospital)) return h;
        }
        return null;
    }

    public static List<String> estados() {
        TreeSet<String> set = new TreeSet<>();
        for (Hospital h : HOSPITALES) set.add(h.getEstado());
        return new ArrayList<>(set);
    }

    public static List<String> especialidades() {
        TreeSet<String> set = new TreeSet<>();
        for (Medico m : MEDICOS) set.addAll(m.getEspecialidades());
        return new ArrayList<>(set);
    }

    public static List<ServicioHospital> serviciosDe(String nombreHospital) {
        List<ServicioHospital> lista = new ArrayList<>();
        for (ServicioHospital s : SERVICIOS) {
            if (s.getHospital().equals(nombreHospital)) lista.add(s);
        }
        return lista;
    }
}
