package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UsuarioDAO {

    public static boolean existeAdministradorGeneral(Integer excluirIdUsuario) throws SQLException {
        String sql = (excluirIdUsuario == null)
                ? "SELECT COUNT(*) FROM usuarios WHERE rol = 'administrador'"
                : "SELECT COUNT(*) FROM usuarios WHERE rol = 'administrador' AND id_usuario <> ?";

        try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(sql)) {
            if (excluirIdUsuario != null) {
                ps.setInt(1, excluirIdUsuario);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public static boolean registrarUsuario(String nombre, String correo, String contrasena, String rol, Integer idEstado) {
        if (nombre == null || nombre.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()
                || contrasena == null || contrasena.length() < 8
                || rol == null || rol.trim().isEmpty()) {
            GestorExcepciones.manejarError("Usuarios", "Validar Campos Obligatorios",
                    "Todos los campos obligatorios deben estar completos y la contraseña debe tener al menos 8 caracteres.",
                    new IllegalArgumentException("Campos obligatorios inválidos en registro de usuario"));
            return false;
        }

        try {
            if ("administrador".equalsIgnoreCase(rol.trim()) && existeAdministradorGeneral(null)) {
                GestorExcepciones.manejarError("Usuarios", "Validar Único Administrador",
                        "Ya existe un Administrador General registrado en el sistema.",
                        new IllegalStateException("Restricción violada: solo se permite un Administrador General"));
                return false;
            }

            conexionBDM.iniciarTransaccion();
            String queryInsert = "INSERT INTO usuarios (nombre_responsable, correo, contrasena, rol, estatus, id_estado) "
                               + "VALUES (?, ?, ?, ?, 'activo', ?)";
            try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(queryInsert)) {
                ps.setString(1, nombre.trim());
                ps.setString(2, correo.trim());
                ps.setString(3, contrasena);
                ps.setString(4, rol.trim());
                if (idEstado != null && idEstado > 0) {
                    ps.setInt(5, idEstado);
                } else {
                    ps.setNull(5, Types.INTEGER);
                }
                ps.executeUpdate();
            }
            conexionBDM.confirmarTransaccion();
            return true;
        } catch (Exception e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Usuarios", "Registrar Usuario",
                    "No se pudo registrar el usuario (el correo puede estar duplicado).", e);
            return false;
        }
    }

    public static Usuario iniciarSesion(String correo, String contrasena) {
        Usuario usuarioLogueado = null;
        String query = "SELECT id_usuario, nombre_responsable, correo, rol, id_estado "
                     + "FROM usuarios WHERE correo = ? AND contrasena = ? AND estatus = 'activo'";

        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, correo);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioLogueado = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_responsable"),
                        rs.getString("correo"),
                        rs.getString("rol"),
                        rs.getInt("id_estado")
                    );
                }
            }
        } catch (Exception e) {
            GestorExcepciones.manejarError("Login / Usuarios", "Autenticación de usuario",
                    "No se pudo conectar para verificar las credenciales.", e);
        }
        return usuarioLogueado;
    }

    public static ObservableList<Usuario> obtenerTodos() {
        ObservableList<Usuario> lista = FXCollections.observableArrayList();
        String query = "SELECT u.id_usuario, u.nombre_responsable, u.correo, u.rol, "
                     + "u.estatus, u.id_estado, e.nombre AS nombre_estado "
                     + "FROM usuarios u "
                     + "LEFT JOIN estados e ON u.id_estado = e.id_estado "
                     + "ORDER BY u.id_usuario ASC";

        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_responsable"),
                    rs.getString("correo"),
                    rs.getString("rol"),
                    rs.getString("estatus"),
                    rs.getInt("id_estado"),
                    rs.getString("nombre_estado")
                ));
            }
        } catch (Exception e) {
            GestorExcepciones.manejarError("Usuarios", "Listar usuarios",
                    "No se pudo cargar la lista de usuarios.", e);
        }
        return lista;
    }

    public static boolean actualizarUsuario(int idUsuario, String nombre, String rol, String estatus, Integer idEstado) {
        if (nombre == null || nombre.trim().isEmpty() || rol == null || estatus == null) {
            return false;
        }
        try {
            if ("administrador".equalsIgnoreCase(rol.trim()) && existeAdministradorGeneral(idUsuario)) {
                GestorExcepciones.manejarError("Usuarios", "Actualizar Rol Administrador",
                        "No se puede asignar el rol de Administrador General porque ya existe uno.",
                        new IllegalStateException("Restricción violada: único Administrador General"));
                return false;
            }

            String query = "UPDATE usuarios SET nombre_responsable = ?, rol = ?, estatus = ?, id_estado = ? "
                         + "WHERE id_usuario = ?";
            conexionBDM.iniciarTransaccion();
            try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(query)) {
                ps.setString(1, nombre.trim());
                ps.setString(2, rol.trim());
                ps.setString(3, estatus.trim());
                if (idEstado != null && idEstado > 0) {
                    ps.setInt(4, idEstado);
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setInt(5, idUsuario);
                ps.executeUpdate();
            }
            conexionBDM.confirmarTransaccion();
            return true;
        } catch (Exception e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Usuarios", "Actualizar usuario ID: " + idUsuario,
                    "No se pudieron guardar los cambios del usuario.", e);
            return false;
        }
    }

    public static boolean eliminarUsuario(int idUsuario) {
        String query = "DELETE FROM usuarios WHERE id_usuario = ?";
        try {
            conexionBDM.iniciarTransaccion();
            try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(query)) {
                ps.setInt(1, idUsuario);
                ps.executeUpdate();
            }
            conexionBDM.confirmarTransaccion();
            return true;
        } catch (Exception e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Usuarios", "Eliminar usuario ID: " + idUsuario,
                    "No se pudo eliminar el usuario (puede tener registros vinculados).", e);
            return false;
        }
    }
}