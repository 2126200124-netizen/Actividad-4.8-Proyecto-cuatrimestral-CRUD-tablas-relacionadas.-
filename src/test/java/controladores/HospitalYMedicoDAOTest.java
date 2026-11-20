package controladores;

import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HospitalYMedicoDAOTest {

    private static HospitalDAO hospitalDAO;
    private static MedicoDAO medicoDAO;
    private static int idEstadoPrueba;
    private static int idHospitalCreado;
    private static int idMedicoCreado;

    @BeforeAll
    static void prepararEntorno() throws Exception {
        conexionBDM.setModoPruebas(true);
        GestorExcepciones.setModoSilenciosoTest(true);

        hospitalDAO = new HospitalDAO();
        medicoDAO = new MedicoDAO();

        try (Connection con = conexionBDM.getConexion(); Statement st = con.createStatement()) {
            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
            st.executeUpdate("TRUNCATE TABLE validaciones");
            st.executeUpdate("TRUNCATE TABLE medico_especialidad");
            st.executeUpdate("TRUNCATE TABLE medicos");
            st.executeUpdate("TRUNCATE TABLE hospital_servicios");
            st.executeUpdate("TRUNCATE TABLE hospitales");
            st.executeUpdate("TRUNCATE TABLE usuarios");
            st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
        }

        List<Estado> estados = hospitalDAO.listarEstados();
        assertFalse(estados.isEmpty(), "La tabla 'estados' en red_medica_test debe tener registros");
        idEstadoPrueba = estados.get(0).getIdEstado();

        assertTrue(UsuarioDAO.registrarUsuario("Solicitante Test", "solicitante@test.com", "Clave1234", "encargado_region", idEstadoPrueba));
        Sesion.idUsuario = UsuarioDAO.obtenerTodos().get(0).getIdUsuario();
    }

    @Test
    @Order(1)
    @DisplayName("CRUD Hospital: Registrar hospital con servicios y solicitud de validación")
    void testInsertarYListarHospital() {
        Hospital h = new Hospital(0, "Hospital Prueba Unitaria", "Av. Juarez 500",
                idEstadoPrueba, "", "activo", "3312345678", "foto_test.png");

        List<Servicio> catServicios = hospitalDAO.listarServiciosActivos();
        List<ServicioDetalle> detalles = catServicios.isEmpty()
                ? List.of()
                : List.of(new ServicioDetalle(catServicios.get(0).getIdServicio(), "Atención 24h", 500.0));

        boolean insertado = hospitalDAO.insertar(h, detalles);
        assertTrue(insertado, "Debe registrar el hospital y su validación pendiente");

        List<Hospital> hospitales = hospitalDAO.listarHospitales();
        assertFalse(hospitales.isEmpty(), "Debe listar al menos el hospital recién creado");
        idHospitalCreado = hospitales.get(0).getIdHospital();
        assertEquals("Hospital Prueba Unitaria", hospitales.get(0).getNombre());
    }

    @Test
    @Order(2)
    @DisplayName("CRUD Hospital: Actualizar datos y servicios del hospital")
    void testActualizarHospital() {
        Hospital hActualizado = new Hospital(idHospitalCreado, "Hospital Prueba Editado",
                "Calle Gigantes 120", idEstadoPrueba, "", "activo", "3398765432", "nueva_foto.png");

        boolean actualizado = hospitalDAO.actualizar(hActualizado, List.of());
        assertTrue(actualizado, "Debe actualizar el hospital sin errores");

        Hospital recuperado = hospitalDAO.listarHospitales().get(0);
        assertEquals("Hospital Prueba Editado", recuperado.getNombre());
        assertTrue(hospitalDAO.obtenerServiciosDetallePorHospital(idHospitalCreado).isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("CRUD Médico y Relaciones: Registrar médico vinculado al hospital")
    void testInsertarMedicoVinculado() {
        List<Especialidad> especialidades = medicoDAO.listarEspecialidadesActivas();
        List<Integer> idsEsp = especialidades.isEmpty()
                ? List.of()
                : List.of(especialidades.get(0).getIdEspecialidad());

        Medico m = new Medico(0, "Carlos", "Mendoza", "medico.png", idHospitalCreado, "", "activo");
        boolean guardado = medicoDAO.insertar(m, idsEsp);
        assertTrue(guardado, "Debe registrar el médico asociado al hospital");

        List<Medico> medicos = medicoDAO.listarMedicos();
        assertFalse(medicos.isEmpty());
        idMedicoCreado = medicos.get(0).getIdMedico();
    }

    @Test
    @Order(4)
    @DisplayName("Integridad Referencial: Bloquear eliminación de hospital con médicos asignados")
    void testNoEliminarHospitalConMedicos() {
        boolean eliminado = hospitalDAO.eliminar(idHospitalCreado);
        assertFalse(eliminado, "No debe permitir borrar un hospital mientras tenga médicos vinculados");
        assertFalse(hospitalDAO.listarHospitales().isEmpty(), "El hospital debe seguir existiendo en la BD");
    }

    @Test
    @Order(5)
    @DisplayName("Transacciones y Rollback: Revertir inserción si falla una llave foránea")
    void testRollbackEnInsercionFallida() {
        int totalAntes = hospitalDAO.listarHospitales().size();

        Hospital hFallido = new Hospital(0, "Hospital Rollback", "Sin Calle",
                idEstadoPrueba, "", "activo", "3300000000", null);
        List<ServicioDetalle> servicioInvalido = List.of(new ServicioDetalle(-999, "Fallo", 100.0));

        boolean resultado = hospitalDAO.insertar(hFallido, servicioInvalido);
        assertFalse(resultado, "La inserción debe fallar por clave foránea inválida");

        int totalDespues = hospitalDAO.listarHospitales().size();
        assertEquals(totalAntes, totalDespues, "El rollback debe evitar que el hospital quede guardado a medias");
    }

    @Test
    @Order(6)
    @DisplayName("CRUD Borrado en cascada controlada: Eliminar médico y luego hospital")
    void testEliminarMedicoYHospital() {
        assertTrue(medicoDAO.eliminar(idMedicoCreado), "Debe eliminar el médico y sus especialidades");
        assertTrue(hospitalDAO.eliminar(idHospitalCreado), "Ahora sin médicos sí debe permitir borrar el hospital");
    }
}