package controladores;

import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class ExcepcionesYLogsTest {

    private final Path rutaLog = Path.of(System.getProperty("user.home"), "Documents", "RedMedica", "registro_errores.log");

    @BeforeEach
    void configurarSilencioso() {
        GestorExcepciones.setModoSilenciosoTest(true);
        conexionBDM.setModoPruebas(true);
    }

    @Test
    @DisplayName("Logs: Registrar fecha, hora, módulo, operación, tipo y ocultar contraseñas")
    void testRegistroLogSinSensibles() throws Exception {
        if (Files.exists(rutaLog)) {
            Files.delete(rutaLog);
        }

        String claveSecreta = "PassUltraSecreta999";
        SQLException errorSimulado = new SQLException("Error de acceso para usuario admin password=" + claveSecreta);

        GestorExcepciones.manejarError("Login / Usuarios", "Autenticación de usuario",
                "Credenciales inválidas.", errorSimulado);

        assertTrue(Files.exists(rutaLog), "Debe crearse el archivo registro_errores.log");
        String contenido = Files.readString(rutaLog);

        assertTrue(contenido.contains("MÓDULO: Login / Usuarios"), "Debe incluir el módulo");
        assertTrue(contenido.contains("OPERACIÓN: Autenticación de usuario"), "Debe incluir la operación");
        assertTrue(contenido.contains("TIPO: SQLException"), "Debe incluir el tipo de excepción");
        assertTrue(contenido.matches("(?s).*\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\].*"),
                "Debe incluir marca de fecha y hora");
        assertFalse(contenido.contains(claveSecreta), "CRÍTICO: No debe registrar contraseñas en el log");
        assertTrue(contenido.contains("password=***"), "Debe enmascarar la contraseña");
    }

    @Test
    @DisplayName("Tolerancia a fallos: Continuar operando después de un error recuperable de BD")
    void testRecuperacionTrasErrorConexion() {
        conexionBDM.forzarUrl("jdbc:mysql://localhost:3307/bd_inexistente_error");
        HospitalDAO dao = new HospitalDAO();

        assertDoesNotThrow(() -> {
            var resultadoFallido = dao.listarEstados();
            assertTrue(resultadoFallido.isEmpty());
        });

        conexionBDM.setModoPruebas(true);
        var resultadoRecuperado = dao.listarEstados();
        assertFalse(resultadoRecuperado.isEmpty(), "La aplicación debe seguir operando tras recuperarse del error");
    }
}