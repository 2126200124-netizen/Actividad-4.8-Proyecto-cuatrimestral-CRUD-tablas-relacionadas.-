package controladores;



import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GestionImagenesTest {

    private static final String SUBCARPETA_TEST = "hospitales_test";
    private static final Path RUTA_CARPETA = GestorImagenes.obtenerCarpetaModulo(SUBCARPETA_TEST);
    private static String nombreImagenGuardada;

    @BeforeAll
    static void prepararEntornoImagenes() throws Exception {
        GestorExcepciones.setModoSilenciosoTest(true);
        if (Files.exists(RUTA_CARPETA)) {
            Files.walk(RUTA_CARPETA)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    @AfterAll
    static void limpiarCarpetaTemporal() throws Exception {
        if (Files.exists(RUTA_CARPETA)) {
            Files.walk(RUTA_CARPETA)
                 .sorted(Comparator.reverseOrder())
                 .map(Path::toFile)
                 .forEach(File::delete);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Imágenes: Crear automáticamente la carpeta en Documentos cuando no existe")
    void testCreacionAutomaticaCarpetaDocumentos() throws Exception {
        assertFalse(Files.exists(RUTA_CARPETA), "La carpeta no debe existir antes de la prueba");

        File imgTemp = File.createTempFile("hospital_demo", ".png");
        Files.write(imgTemp.toPath(), new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        nombreImagenGuardada = GestorImagenes.guardarImagen(imgTemp, SUBCARPETA_TEST);

        assertTrue(Files.exists(RUTA_CARPETA) && Files.isDirectory(RUTA_CARPETA),
                "Debe crear automáticamente la carpeta dentro de Documentos/RedMedica");
        assertNotNull(nombreImagenGuardada, "Debe devolver el nombre generado de la imagen");

        imgTemp.deleteOnExit();
    }

    @Test
    @Order(2)
    @DisplayName("Imágenes: Recuperar correctamente una imagen almacenada")
    void testRecuperarImagen() {
        File recuperada = GestorImagenes.recuperarImagen(nombreImagenGuardada, SUBCARPETA_TEST);
        assertNotNull(recuperada, "Debe encontrar la imagen guardada previamente");
        assertTrue(recuperada.exists() && recuperada.length() > 0);
    }

    @Test
    @Order(3)
    @DisplayName("Imágenes: Controlar imagen inexistente, formato incorrecto e integridad previa")
    void testErroresEIntegridadImagenes() throws Exception {
   
        File inexistente = new File("C:/ruta/que/no/existe/foto.jpg");
        assertNull(GestorImagenes.guardarImagen(inexistente, SUBCARPETA_TEST),
                "Debe rechazar de forma controlada un archivo inexistente");

 
        File formatoInvalido = File.createTempFile("malicioso", ".txt");
        Files.writeString(formatoInvalido.toPath(), "esto no es una imagen");
        assertNull(GestorImagenes.guardarImagen(formatoInvalido, SUBCARPETA_TEST),
                "Debe rechazar archivos que no sean PNG, JPG o JPEG");


        File imagenIntacta = GestorImagenes.recuperarImagen(nombreImagenGuardada, SUBCARPETA_TEST);
        assertNotNull(imagenIntacta, "Los fallos de almacenamiento no deben dañar ni borrar imágenes previas");

        formatoInvalido.deleteOnExit();
    }
}
