package controladores;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PanelAdminController {

    @FXML private StackPane contenido;
    @FXML private Label lblUsuario;

    @FXML
    private void initialize() {
        lblUsuario.setText(Sesion.nombre + " (administrador)");
        verUsuarios();
    }

    @FXML
    private void verUsuarios() {
        cargarVista("vista_usuarios.fxml");
    }

    @FXML
    private void verHospitales() {
        cargarVista("vista_hospitales.fxml");
    }

    @FXML
    private void verMedicos() {
        cargarVista("vista_medicos.fxml");
    }

    @FXML
    private void verValidaciones() {
        cargarVista("vista_validaciones.fxml");
    }

    private void cargarVista(String fxml) {
        try {
            Navegador.cargarEn(contenido, fxml);
        } catch (IOException e) {
            GestorExcepciones.manejarError("Panel Admin", "Cargar vista: " + fxml,
                    "No se pudo cargar la sección seleccionada.", e);
        }
    }

    @FXML
    private void cerrarSesion() throws IOException {
        Sesion.cerrar();
        Navegador.cambiar(contenido, "Inicio_sesion.fxml");
    }
}