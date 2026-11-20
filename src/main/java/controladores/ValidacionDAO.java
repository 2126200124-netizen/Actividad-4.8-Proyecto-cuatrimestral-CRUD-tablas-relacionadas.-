package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ValidacionDAO {

    public List<Validacion> listarValidaciones() {
        List<Validacion> lista = new ArrayList<>();
        String sql = "SELECT v.id_validacion, v.id_hospital, v.id_medico, v.estado, "
                   + "v.id_usuario_solicita, v.id_usuario_valida, "
                   + "v.fecha_solicitud, v.fecha_validacion, v.observaciones, "
                   + "CASE WHEN v.id_hospital IS NOT NULL THEN 'Hospital' ELSE 'Médico' END AS tipo, "
                   + "COALESCE(h.nombre, CONCAT(m.nombre, ' ', m.apellidos)) AS nombre, "
                   + "COALESCE(e.nombre, hm.nombre) AS detalle "
                   + "FROM validaciones v "
                   + "LEFT JOIN hospitales h ON v.id_hospital = h.id_hospital "
                   + "LEFT JOIN estados e ON h.id_estado = e.id_estado "
                   + "LEFT JOIN medicos m ON v.id_medico = m.id_medico "
                   + "LEFT JOIN hospitales hm ON m.id_hospital = hm.id_hospital "
                   + "ORDER BY FIELD(v.estado, 'pendiente', 'aprobado', 'rechazado'), v.id_validacion DESC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Validacion v = new Validacion();
                v.setIdValidacion(rs.getInt("id_validacion"));

                int idHosp = rs.getInt("id_hospital");
                v.setIdHospital(rs.wasNull() ? null : idHosp);

                int idMed = rs.getInt("id_medico");
                v.setIdMedico(rs.wasNull() ? null : idMed);

                v.setTipo(rs.getString("tipo"));
                v.setNombre(rs.getString("nombre"));
                v.setDetalle(rs.getString("detalle"));
                v.setEstado(rs.getString("estado"));
                v.setFechaSolicitud(rs.getString("fecha_solicitud"));
                v.setFechaValidacion(rs.getString("fecha_validacion"));
                v.setObservaciones(rs.getString("observaciones"));
                lista.add(v);
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError(
                "Validaciones",
                "Listar Validaciones",
                "No se pudieron consultar las solicitudes de validación.",
                e
            );
        }
        return lista;
    }

    public boolean dictaminar(Validacion validacion, String nuevoEstado, String observaciones) {
        String sqlValidacion = "UPDATE validaciones SET estado = ?, id_usuario_valida = ?, "
                             + "fecha_validacion = NOW(), observaciones = ? WHERE id_validacion = ?";
        String sqlHospital = "UPDATE hospitales SET estatus = ? WHERE id_hospital = ?";
        String sqlMedico = "UPDATE medicos SET estatus = ? WHERE id_medico = ?";

        String estatusEntidad = "aprobado".equals(nuevoEstado) ? "activo" : "inactivo";

        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();

          
            try (PreparedStatement psVal = conn.prepareStatement(sqlValidacion)) {
                psVal.setString(1, nuevoEstado);
                if (Sesion.idUsuario > 0) {
                    psVal.setInt(2, Sesion.idUsuario);
                } else {
                    psVal.setNull(2, Types.INTEGER);
                }
                psVal.setString(3, observaciones.isEmpty() ? null : observaciones);
                psVal.setInt(4, validacion.getIdValidacion());
                psVal.executeUpdate();
            }

        
            if (validacion.getIdHospital() != null) {
                try (PreparedStatement psHosp = conn.prepareStatement(sqlHospital)) {
                    psHosp.setString(1, estatusEntidad);
                    psHosp.setInt(2, validacion.getIdHospital());
                    psHosp.executeUpdate();
                }
            } else if (validacion.getIdMedico() != null) {
                try (PreparedStatement psMed = conn.prepareStatement(sqlMedico)) {
                    psMed.setString(1, estatusEntidad);
                    psMed.setInt(2, validacion.getIdMedico());
                    psMed.executeUpdate();
                }
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError(
                "Validaciones",
                "Dictaminar Solicitud",
                "No se pudo procesar la validación seleccionada.",
                e
            );
            return false;
        }
    }
}