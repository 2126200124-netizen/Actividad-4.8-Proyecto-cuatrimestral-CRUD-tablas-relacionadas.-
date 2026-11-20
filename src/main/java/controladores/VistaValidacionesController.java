package controladores;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class VistaValidacionesController {

    @FXML private TableView<Validacion> tabla;
    @FXML private TableColumn<Validacion, String> colTipo;
    @FXML private TableColumn<Validacion, String> colNombre;
    @FXML private TableColumn<Validacion, String> colDetalle;
    @FXML private TableColumn<Validacion, String> colEstado;
    @FXML private TableColumn<Validacion, String> colObs;

    @FXML private TextField txtObservaciones;
    @FXML private Label lblMensaje;

    private final ValidacionDAO validacionDAO = new ValidacionDAO();
    private final ObservableList<Validacion> listaValidaciones = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTabla();
        cargarTabla();
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(c -> c.getValue().tipoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colDetalle.setCellValueFactory(c -> c.getValue().detalleProperty());
        colEstado.setCellValueFactory(c -> c.getValue().estadoProperty());
        colObs.setCellValueFactory(c -> c.getValue().observacionesProperty());

        tabla.setItems(listaValidaciones);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                txtObservaciones.setText(actual.getObservaciones() != null ? actual.getObservaciones() : "");
                mensaje("", true);
            }
        });
    }

    private void cargarTabla() {
        listaValidaciones.setAll(validacionDAO.listarValidaciones());
    }

    @FXML
    private void aprobar() {
        Validacion seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mensaje("Selecciona un registro de la tabla para aprobar.", false);
            return;
        }

        String obs = txtObservaciones.getText().trim();
        if (validacionDAO.dictaminar(seleccionada, "aprobado", obs)) {
            cargarTabla();
            txtObservaciones.clear();
            mensaje("Registro aprobado y activado correctamente.", true);
        } else {
            mensaje("No se pudo aprobar el registro.", false);
        }
    }

    @FXML
    private void rechazar() {
        Validacion seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mensaje("Selecciona un registro de la tabla para rechazar.", false);
            return;
        }

        String obs = txtObservaciones.getText().trim();
        if (obs.isEmpty()) {
            mensaje("Debes escribir observaciones obligatorias para rechazar.", false);
            return;
        }

        if (validacionDAO.dictaminar(seleccionada, "rechazado", obs)) {
            cargarTabla();
            txtObservaciones.clear();
            mensaje("Registro rechazado correctamente.", true);
        } else {
            mensaje("No se pudo rechazar el registro.", false);
        }
    }

    private void mensaje(String texto, boolean exito) {
        lblMensaje.setStyle(exito ? "-fx-text-fill: #0F766E;" : "-fx-text-fill: #DC2626;");
        lblMensaje.setText(texto);
    }
}