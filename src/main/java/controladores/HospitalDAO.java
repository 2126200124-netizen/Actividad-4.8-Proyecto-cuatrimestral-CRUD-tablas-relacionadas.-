package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HospitalDAO {

    public List<Estado> listarEstados() {
        List<Estado> lista = new ArrayList<>();
        String sql = "SELECT id_estado, nombre FROM estados ORDER BY nombre ASC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Estado(rs.getInt("id_estado"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Hospitales", "Listar Estados",
                    "No se pudo cargar el catálogo de estados.", e);
        }
        return lista;
    }

    public List<Servicio> listarServiciosActivos() {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT id_servicio, nombre FROM servicios WHERE estatus = 'activo' ORDER BY nombre ASC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Servicio(rs.getInt("id_servicio"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Hospitales", "Listar Servicios",
                    "No se pudo cargar el catálogo de servicios.", e);
        }
        return lista;
    }

    public Map<Integer, ServicioDetalle> obtenerServiciosDetallePorHospital(int idHospital) {
        Map<Integer, ServicioDetalle> mapa = new HashMap<>();
        String sql = "SELECT id_servicio, IFNULL(caracteristicas, '') AS caracteristicas, "
                   + "IFNULL(costo, 0.00) AS costo FROM hospital_servicios WHERE id_hospital = ?";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idHospital);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idServ = rs.getInt("id_servicio");
                    mapa.put(idServ, new ServicioDetalle(
                            idServ,
                            rs.getString("caracteristicas"),
                            rs.getDouble("costo")
                    ));
                }
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Hospitales", "Consultar Servicios del Hospital",
                    "No se pudieron obtener los detalles de los servicios asignados.", e);
        }
        return mapa;
    }

    public List<Hospital> listarHospitales() {
        List<Hospital> lista = new ArrayList<>();
        String sql = "SELECT h.id_hospital, h.nombre, h.direccion, h.id_estado, "
                   + "e.nombre AS nombre_estado, h.estatus, h.telefono, h.foto "
                   + "FROM hospitales h "
                   + "INNER JOIN estados e ON h.id_estado = e.id_estado "
                   + "ORDER BY h.id_hospital DESC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Hospital(
                    rs.getInt("id_hospital"),
                    rs.getString("nombre"),
                    rs.getString("direccion"),
                    rs.getInt("id_estado"),
                    rs.getString("nombre_estado"),
                    rs.getString("estatus"),
                    rs.getString("telefono"),
                    rs.getString("foto")
                ));
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Hospitales", "Listar Hospitales",
                    "No se pudieron consultar los hospitales registrados.", e);
        }
        return lista;
    }

    public boolean insertar(Hospital hospital, List<ServicioDetalle> servicios) {
        String sqlHospital = "INSERT INTO hospitales (nombre, direccion, id_estado, estatus, telefono, foto) "
                           + "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlServicio = "INSERT INTO hospital_servicios (id_hospital, id_servicio, caracteristicas, costo) "
                           + "VALUES (?, ?, ?, ?)";
        String sqlValidacion = "INSERT INTO validaciones (id_hospital, estado, id_usuario_solicita, fecha_solicitud) "
                             + "VALUES (?, 'pendiente', ?, NOW())";
        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();
            int idGenerado = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlHospital, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, hospital.getNombre());
                ps.setString(2, hospital.getDireccion());
                ps.setInt(3, hospital.getIdEstado());
                ps.setString(4, hospital.getEstatus());
                ps.setString(5, hospital.getTelefono());
                ps.setString(6, hospital.getFoto());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }

            if (idGenerado > 0) {
                if (servicios != null && !servicios.isEmpty()) {
                    try (PreparedStatement psServ = conn.prepareStatement(sqlServicio)) {
                        for (ServicioDetalle s : servicios) {
                            psServ.setInt(1, idGenerado);
                            psServ.setInt(2, s.getIdServicio());
                            psServ.setString(3, s.getCaracteristicas());
                            psServ.setDouble(4, s.getCosto());
                            psServ.addBatch();
                        }
                        psServ.executeBatch();
                    }
                }

                try (PreparedStatement psVal = conn.prepareStatement(sqlValidacion)) {
                    psVal.setInt(1, idGenerado);
                    if (Sesion.idUsuario > 0) {
                        psVal.setInt(2, Sesion.idUsuario);
                    } else {
                        psVal.setNull(2, Types.INTEGER);
                    }
                    psVal.executeUpdate();
                }
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Hospitales", "Registrar Hospital",
                    "No se pudo guardar el hospital, sus servicios y su solicitud de validación.", e);
            return false;
        }
    }

    public boolean actualizar(Hospital hospital, List<ServicioDetalle> servicios) {
        String sqlHospital = "UPDATE hospitales SET nombre = ?, direccion = ?, id_estado = ?, "
                           + "estatus = ?, telefono = ?, foto = ? WHERE id_hospital = ?";
        String sqlBorrarServicios = "DELETE FROM hospital_servicios WHERE id_hospital = ?";
        String sqlInsertarServicio = "INSERT INTO hospital_servicios (id_hospital, id_servicio, caracteristicas, costo) "
                                   + "VALUES (?, ?, ?, ?)";
        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();

            try (PreparedStatement ps = conn.prepareStatement(sqlHospital)) {
                ps.setString(1, hospital.getNombre());
                ps.setString(2, hospital.getDireccion());
                ps.setInt(3, hospital.getIdEstado());
                ps.setString(4, hospital.getEstatus());
                ps.setString(5, hospital.getTelefono());
                ps.setString(6, hospital.getFoto());
                ps.setInt(7, hospital.getIdHospital());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlBorrarServicios)) {
                psDel.setInt(1, hospital.getIdHospital());
                psDel.executeUpdate();
            }

            if (servicios != null && !servicios.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertarServicio)) {
                    for (ServicioDetalle s : servicios) {
                        psIns.setInt(1, hospital.getIdHospital());
                        psIns.setInt(2, s.getIdServicio());
                        psIns.setString(3, s.getCaracteristicas());
                        psIns.setDouble(4, s.getCosto());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Hospitales", "Actualizar Hospital",
                    "No se pudo actualizar la información del hospital y sus servicios.", e);
            return false;
        }
    }

    public boolean eliminar(int idHospital) {
        String sqlContarMedicos = "SELECT COUNT(*) FROM medicos WHERE id_hospital = ?";
        String sqlBorrarServicios = "DELETE FROM hospital_servicios WHERE id_hospital = ?";
        String sqlBorrarValidaciones = "DELETE FROM validaciones WHERE id_hospital = ?";
        String sqlHospital = "DELETE FROM hospitales WHERE id_hospital = ?";
        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();

            // Validar que no existan médicos dependientes antes de eliminar
            try (PreparedStatement psCheck = conn.prepareStatement(sqlContarMedicos)) {
                psCheck.setInt(1, idHospital);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conexionBDM.revertirTransaccion();
                        GestorExcepciones.manejarError("Hospitales", "Eliminar Hospital",
                                "No se puede eliminar el hospital porque tiene médicos vinculados.",
                                new IllegalStateException("Relación dependiente activa en tabla medicos"));
                        return false;
                    }
                }
            }

            try (PreparedStatement psDelServ = conn.prepareStatement(sqlBorrarServicios)) {
                psDelServ.setInt(1, idHospital);
                psDelServ.executeUpdate();
            }

            try (PreparedStatement psDelVal = conn.prepareStatement(sqlBorrarValidaciones)) {
                psDelVal.setInt(1, idHospital);
                psDelVal.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlHospital)) {
                ps.setInt(1, idHospital);
                ps.executeUpdate();
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Hospitales", "Eliminar Hospital",
                    "No se pudo eliminar el hospital. Verifique que no tenga médicos vinculados.", e);
            return false;
        }
    }
    
}