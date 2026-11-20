package controladores;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RelacionesHospitalServicioTest {

    private static HospitalDAO hospitalDAO;

    @BeforeAll
    public static void configurarEntorno() {

        conexionBDM.setModoPruebas(true);
        GestorExcepciones.setModoSilenciosoTest(true);
        hospitalDAO = new HospitalDAO();
    }

    @AfterAll
    public static void restaurarEntorno() {
        conexionBDM.setModoPruebas(false);
        GestorExcepciones.setModoSilenciosoTest(false);
    }

    @BeforeEach
    public void limpiarTablasDePrueba() {
        try (Connection con = conexionBDM.getConexion(); Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM hospital_servicios");
            st.executeUpdate("DELETE FROM hospitales");
        } catch (SQLException e) {
            fail("Error al limpiar las tablas antes de la prueba: " + e.getMessage());
        }
    }

    @Test
    public void testInsertarHospitalConServicios_Exito() {

        Hospital nuevoHospital = new Hospital();
        nuevoHospital.setNombre("Hospital San Jorge");
        nuevoHospital.setDireccion("Av. Siempreviva 123");
        nuevoHospital.setIdEstado(1);
        nuevoHospital.setTelefono("1234567890");
        nuevoHospital.setEstatus("activo");

        
        List<ServicioDetalle> servicios = Arrays.asList(
            new ServicioDetalle(1, "Rayos X Tórax", 500.0),
            new ServicioDetalle(2, "Análisis de Sangre", 350.0)
        );

        boolean resultado = hospitalDAO.insertar(nuevoHospital, servicios);
        
        assertTrue(resultado, "El sistema debió guardar el hospital y sus servicios exitosamente.");
        assertFalse(hospitalDAO.listarHospitales().isEmpty(), "Debe existir al menos un hospital en la BD.");
    }

    @Test
    public void testToleranciaAFallos_RollbackActivo() {

        Hospital hospitalFallido = new Hospital();
        hospitalFallido.setNombre("Hospital Error");
        hospitalFallido.setDireccion("Calle Falsa");
        hospitalFallido.setIdEstado(1);
        hospitalFallido.setTelefono("0000000000");
        hospitalFallido.setEstatus("activo");


        List<ServicioDetalle> servicioInvalido = Arrays.asList(
            new ServicioDetalle(-999, "Servicio Fantasma", 100.0)
        );

        boolean resultado = hospitalDAO.insertar(hospitalFallido, servicioInvalido);

     
        assertFalse(resultado, "La inserción debió ser bloqueada por la base de datos.");
        
   
        assertTrue(hospitalDAO.listarHospitales().isEmpty(), "El Rollback no funcionó: El hospital se guardó a pesar de que el servicio falló.");
    }

    @Test
    public void testValidacion_DatosIncompletos() {

        Hospital hospitalInvalido = new Hospital();
        hospitalInvalido.setNombre(null); 
        hospitalInvalido.setDireccion("Falta Nombre");
        
        boolean resultado = hospitalDAO.insertar(hospitalInvalido, Arrays.asList());
        
        assertFalse(resultado, "El DAO debe retornar false y manejar la excepción al intentar guardar datos nulos.");
    }
}
