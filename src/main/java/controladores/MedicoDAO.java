package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MedicoDAO {

    public List<Hospital> listarHospitalesActivos() {
        List<Hospital> lista = new ArrayList<>();
        String sql = "SELECT h.id_hospital, h.nombre, h.direccion, h.id_estado, "
                   + "e.nombre AS nombre_estado, h.estatus, h.telefono, h.foto "
                   + "FROM hospitales h "
                   + "INNER JOIN estados e ON h.id_estado = e.id_estado "
                   + "WHERE h.estatus = 'activo' "
                   + "ORDER BY h.nombre ASC";

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
            GestorExcepciones.manejarError("Médicos", "Listar Hospitales",
                    "No se pudo cargar la lista de hospitales disponibles.", e);
        }
        return lista;
    }

    public List<Especialidad> listarEspecialidadesActivas() {
        List<Especialidad> lista = new ArrayList<>();
        String sql = "SELECT id_especialidad, nombre FROM especialidades WHERE estatus = 'activo' ORDER BY nombre ASC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Especialidad(rs.getInt("id_especialidad"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Médicos", "Listar Especialidades",
                    "No se pudo cargar el catálogo de especialidades.", e);
        }
        return lista;
    }

    public List<Integer> obtenerIdsEspecialidadesPorMedico(int idMedico) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_especialidad FROM medico_especialidad WHERE id_medico = ?";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idMedico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_especialidad"));
                }
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Médicos", "Consultar Especialidades del Médico",
                    "No se pudieron obtener las especialidades asignadas al médico.", e);
        }
        return ids;
    }

    public List<Medico> listarMedicos() {
        List<Medico> lista = new ArrayList<>();
        String sql = "SELECT m.id_medico, m.nombre, m.apellidos, m.foto, m.id_hospital, "
                   + "h.nombre AS nombre_hospital, m.estatus "
                   + "FROM medicos m "
                   + "INNER JOIN hospitales h ON m.id_hospital = h.id_hospital "
                   + "ORDER BY m.id_medico DESC";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Medico(
                    rs.getInt("id_medico"),
                    rs.getString("nombre"),
                    rs.getString("apellidos"),
                    rs.getString("foto"),
                    rs.getInt("id_hospital"),
                    rs.getString("nombre_hospital"),
                    rs.getString("estatus")
                ));
            }
        } catch (SQLException e) {
            GestorExcepciones.manejarError("Médicos", "Listar Médicos",
                    "No se pudieron consultar los médicos registrados.", e);
        }
        return lista;
    }

    public boolean insertar(Medico medico, List<Integer> idsEspecialidades) {
        String sqlMedico = "INSERT INTO medicos (nombre, apellidos, foto, id_hospital, estatus) "
                         + "VALUES (?, ?, ?, ?, ?)";
        String sqlEsp = "INSERT INTO medico_especialidad (id_medico, id_especialidad) VALUES (?, ?)";
        String sqlValidacion = "INSERT INTO validaciones (id_medico, estado, id_usuario_solicita, fecha_solicitud) "
                             + "VALUES (?, 'pendiente', ?, NOW())";

        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();
            int idGenerado = 0;

            try (PreparedStatement ps = conn.prepareStatement(sqlMedico, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, medico.getNombre());
                ps.setString(2, medico.getApellidos());
                ps.setString(3, medico.getFoto());
                ps.setInt(4, medico.getIdHospital());
                ps.setString(5, medico.getEstatus());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    }
                }
            }

            if (idGenerado > 0) {
                if (idsEspecialidades != null && !idsEspecialidades.isEmpty()) {
                    try (PreparedStatement psEsp = conn.prepareStatement(sqlEsp)) {
                        for (Integer idEspecialidad : idsEspecialidades) {
                            psEsp.setInt(1, idGenerado);
                            psEsp.setInt(2, idEspecialidad);
                            psEsp.addBatch();
                        }
                        psEsp.executeBatch();
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
            GestorExcepciones.manejarError("Médicos", "Registrar Médico",
                    "No se pudo guardar el médico, sus especialidades y su solicitud de validación.", e);
            return false;
        }
    }

    public boolean actualizar(Medico medico, List<Integer> idsEspecialidades) {
        String sqlMedico = "UPDATE medicos SET nombre = ?, apellidos = ?, foto = ?, "
                         + "id_hospital = ?, estatus = ? WHERE id_medico = ?";
        String sqlBorrarEsp = "DELETE FROM medico_especialidad WHERE id_medico = ?";
        String sqlInsertarEsp = "INSERT INTO medico_especialidad (id_medico, id_especialidad) VALUES (?, ?)";

        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();

            try (PreparedStatement ps = conn.prepareStatement(sqlMedico)) {
                ps.setString(1, medico.getNombre());
                ps.setString(2, medico.getApellidos());
                ps.setString(3, medico.getFoto());
                ps.setInt(4, medico.getIdHospital());
                ps.setString(5, medico.getEstatus());
                ps.setInt(6, medico.getIdMedico());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlBorrarEsp)) {
                psDel.setInt(1, medico.getIdMedico());
                psDel.executeUpdate();
            }

            if (idsEspecialidades != null && !idsEspecialidades.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsertarEsp)) {
                    for (Integer idEspecialidad : idsEspecialidades) {
                        psIns.setInt(1, medico.getIdMedico());
                        psIns.setInt(2, idEspecialidad);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Médicos", "Actualizar Médico",
                    "No se pudo actualizar la información del médico y sus especialidades.", e);
            return false;
        }
    }

    public boolean eliminar(int idMedico) {
        String sqlBorrarEsp = "DELETE FROM medico_especialidad WHERE id_medico = ?";
        String sqlBorrarValidaciones = "DELETE FROM validaciones WHERE id_medico = ?";
        String sqlMedico = "DELETE FROM medicos WHERE id_medico = ?";

        try {
            conexionBDM.iniciarTransaccion();
            Connection conn = conexionBDM.getConexion();

            try (PreparedStatement psDelEsp = conn.prepareStatement(sqlBorrarEsp)) {
                psDelEsp.setInt(1, idMedico);
                psDelEsp.executeUpdate();
            }

            try (PreparedStatement psDelVal = conn.prepareStatement(sqlBorrarValidaciones)) {
                psDelVal.setInt(1, idMedico);
                psDelVal.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlMedico)) {
                ps.setInt(1, idMedico);
                ps.executeUpdate();
            }

            conexionBDM.confirmarTransaccion();
            return true;
        } catch (SQLException e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Médicos", "Eliminar Médico",
                    "No se pudo eliminar el médico seleccionado.", e);
            return false;
        }
    }
}