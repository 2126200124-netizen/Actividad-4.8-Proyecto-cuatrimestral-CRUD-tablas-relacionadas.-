package controladores;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.Alert;

public class GestorExcepciones {

    private static boolean modoSilenciosoTest = false;

    public static void setModoSilenciosoTest(boolean silencioso) {
        modoSilenciosoTest = silencioso;
    }

    public static void manejarError(String modulo, String operacion, String mensajeAmigable, Exception ex) {
        registrarLogSilencioso(modulo, operacion, ex);

        if (!modoSilenciosoTest) {
            try {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Aviso del Sistema");
                alerta.setHeaderText("Inconveniente en módulo: " + modulo);
                alerta.setContentText(mensajeAmigable + "\n\nPor favor, verifique su información o intente más tarde.");
                alerta.showAndWait();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    public static void registrarLogSilencioso(String modulo, String operacion, Exception ex) {
        try {
            String rutaDocs = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "RedMedica";
            File directorio = new File(rutaDocs);
            if (!directorio.exists()) {
                directorio.mkdirs();
            }

            File archivoLog = new File(directorio, "registro_errores.log");
            try (FileWriter fw = new FileWriter(archivoLog, true)) {
                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String tipoExcepcion = (ex != null) ? ex.getClass().getSimpleName() : "Excepcion Desconocida";
                String detalleTecnico = (ex != null && ex.getMessage() != null) ? sanitizarMensaje(ex.getMessage()) : "Sin detalle";

                String lineaLog = String.format("[%s] MÓDULO: %s | OPERACIÓN: %s | TIPO: %s | DETALLE: %s%n",
                        fechaHora, modulo, operacion, tipoExcepcion, detalleTecnico);

                fw.write(lineaLog);
            }
        } catch (IOException e) {
            System.err.println("Fallo crítico al intentar escribir en el Log: " + e.getMessage());
        }
    }

    private static String sanitizarMensaje(String mensaje) {
        return mensaje.replaceAll("(?i)(password|pass|pwd|contrasena|contraseña)\\s*[:=]\\s*[^\\s,;)]+", "$1=***");
    }
}
