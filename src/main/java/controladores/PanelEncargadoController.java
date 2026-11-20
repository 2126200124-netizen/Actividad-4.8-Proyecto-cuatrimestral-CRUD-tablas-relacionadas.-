package controladores;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PanelEncargadoController {

    @FXML private StackPane contenido;
    @FXML private Label lblUsuario;

    @FXML
    private void initialize() {
        lblUsuario.setText(Sesion.nombre + " (encargado de región)");
        verHospitales();
    }

    @FXML private void verHospitales() { mostrar("vista_hospitales.fxml"); }

    @FXML private void verMedicos() { mostrar("vista_medicos.fxml"); }

    @FXML
    private void cerrarSesion() throws IOException {
        Sesion.cerrar();
        Navegador.cambiar(contenido, "Inicio_sesion.fxml");
    }

    private void mostrar(String fxml) {
        try {
            Navegador.cargarEn(contenido, fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
