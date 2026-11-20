package controladores;



import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsuarioYValidacionTest {

    private static int idEstadoPrueba;
    private static int idAdminCreado;
    private static int idEncargadoCreado;

    @BeforeAll
    static void prepararBD() throws Exception {
        conexionBDM.setModoPruebas(true);
        GestorExcepciones.setModoSilenciosoTest(true);

        try (Connection con = conexionBDM.getConexion(); Statement st = con.createStatement()) {
            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            st.executeUpdate("TRUNCATE TABLE validaciones");
            st.executeUpdate("TRUNCATE TABLE usuarios");
            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }

        HospitalDAO hDao = new HospitalDAO();
        idEstadoPrueba = hDao.listarEstados().get(0).getIdEstado();
    }

    @Test
    @Order(1)
    @DisplayName("Validaciones: Rechazar campos obligatorios vacíos o contraseña corta")
    void testCamposObligatorios() {
        assertFalse(UsuarioDAO.registrarUsuario("", "correo@test.com", "12345678", "consulta", idEstadoPrueba),
                "Debe rechazar nombre vacío");
        assertFalse(UsuarioDAO.registrarUsuario("Juan Perez", "", "12345678", "consulta", idEstadoPrueba),
                "Debe rechazar correo vacío");
        assertFalse(UsuarioDAO.registrarUsuario("Juan Perez", "juan@test.com", "123", "consulta", idEstadoPrueba),
                "Debe rechazar contraseña menor a 8 caracteres");
        assertFalse(UsuarioDAO.registrarUsuario("Juan Perez", "juan@test.com", "12345678", null, idEstadoPrueba),
                "Debe rechazar rol nulo");
    }

    @Test
    @Order(2)
    @DisplayName("Restricción: Permitir solo un Administrador General en todo el sistema")
    void testUnicoAdministradorGeneral() {
        boolean primerAdmin = UsuarioDAO.registrarUsuario(
                "Admin Principal", "admin@redmedica.com", "AdminPass123", "administrador", null);
        assertTrue(primerAdmin, "Debe permitir registrar el primer Administrador General");

        boolean segundoAdmin = UsuarioDAO.registrarUsuario(
                "Admin Intruso", "admin2@redmedica.com", "AdminPass123", "administrador", null);
        assertFalse(segundoAdmin, "Debe rechazar un segundo Administrador General");

        idAdminCreado = UsuarioDAO.obtenerTodos().get(0).getIdUsuario();
    }

    @Test
    @Order(3)
    @DisplayName("Validación: No permitir registros con correo duplicado")
    void testNoRegistrosDuplicados() {
        boolean registrado = UsuarioDAO.registrarUsuario(
                "Encargado Jalisco", "encargado@redmedica.com", "ClaveSegura123", "encargado_region", idEstadoPrueba);
        assertTrue(registrado, "Debe registrar el encargado regional");

        boolean duplicado = UsuarioDAO.registrarUsuario(
                "Otro Nombre", "encargado@redmedica.com", "OtraClave123", "consulta", idEstadoPrueba);
        assertFalse(duplicado, "Debe rechazar el correo duplicado en la BD");

        idEncargadoCreado = UsuarioDAO.obtenerTodos().stream()
                .filter(u -> "encargado@redmedica.com".equals(u.getCorreo()))
                .findFirst().orElseThrow().getIdUsuario();
    }

    @Test
    @Order(4)
    @DisplayName("CRUD Usuarios: Consulta, login, edición y bloqueo de promoción a segundo Admin")
    void testLoginYActualizacion() {
        Usuario logueado = UsuarioDAO.iniciarSesion("encargado@redmedica.com", "ClaveSegura123");
        assertNotNull(logueado, "Debe iniciar sesión con credenciales válidas");
        assertEquals("encargado_region", logueado.getRol());


        assertFalse(UsuarioDAO.actualizarUsuario(idEncargadoCreado, "Encargado Editado", "administrador", "activo", idEstadoPrueba),
                "No debe permitir promover otro usuario a Administrador General");

     
        assertTrue(UsuarioDAO.actualizarUsuario(idEncargadoCreado, "Encargado Editado", "encargado_region", "activo", idEstadoPrueba));
    }

    @Test
    @Order(5)
    @DisplayName("Relaciones y Validaciones: Dictaminar solicitud y bloquear borrado de usuario con dependencias")
    void testValidacionYDependencias() {
        Sesion.idUsuario = idEncargadoCreado;
        HospitalDAO hDao = new HospitalDAO();
        Hospital h = new Hospital(0, "Hospital Validacion Test", "Calle 1", idEstadoPrueba, "", "inactivo", "3311112222", null);
        assertTrue(hDao.insertar(h, List.of()));


        assertFalse(UsuarioDAO.eliminarUsuario(idEncargadoCreado),
                "No debe eliminar un usuario que tiene solicitudes de validación vinculadas");


        Sesion.idUsuario = idAdminCreado;
        ValidacionDAO vDao = new ValidacionDAO();
        List<Validacion> pendientes = vDao.listarValidaciones();
        assertFalse(pendientes.isEmpty());

        assertTrue(vDao.dictaminar(pendientes.get(0), "aprobado", "Documentación completa"),
                "Debe aprobar la validación y activar el hospital en una misma transacción");
    }
}