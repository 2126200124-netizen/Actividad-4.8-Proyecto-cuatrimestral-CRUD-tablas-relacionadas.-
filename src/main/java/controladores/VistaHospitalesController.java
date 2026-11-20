package controladores;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class VistaHospitalesController {

    private static class FilaServicioUI {
        final Servicio servicio;
        final CheckBox check;
        final TextField txtCaracteristicas;
        final TextField txtCosto;

        FilaServicioUI(Servicio servicio, CheckBox check, TextField txtCaracteristicas, TextField txtCosto) {
            this.servicio = servicio;
            this.check = check;
            this.txtCaracteristicas = txtCaracteristicas;
            this.txtCosto = txtCosto;
        }
    }

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDireccion;
    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cmbEstatus;
    @FXML private FlowPane panelServicios;
    @FXML private Label lblFoto;
    @FXML private ImageView imgFoto;
    @FXML private Label lblMensaje;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TableView<Hospital> tabla;
    @FXML private TableColumn<Hospital, String> colNombre;
    @FXML private TableColumn<Hospital, String> colEntidad;
    @FXML private TableColumn<Hospital, String> colTelefono;
    @FXML private TableColumn<Hospital, String> colEstatus;

    private final HospitalDAO hospitalDAO = new HospitalDAO();
    private final ObservableList<Hospital> listaHospitales = FXCollections.observableArrayList();
    private final List<FilaServicioUI> filasServicios = new ArrayList<>();

    private Hospital hospitalSeleccionado;
    private File archivoFotoSeleccionada;

    private static final Path CARPETA_IMAGENES = Paths.get(
            System.getProperty("user.home"), "Documents", "RedMedica", "hospitales");

    @FXML
    private void initialize() {
        crearDirectorioImagenes();
        cargarCombos();
        cargarCheckboxesServicios();
        configurarTabla();
        cargarTabla();
    }

    private void crearDirectorioImagenes() {
        try {
            if (!Files.exists(CARPETA_IMAGENES)) {
                Files.createDirectories(CARPETA_IMAGENES);
            }
        } catch (IOException e) {
            GestorExcepciones.manejarError("Hospitales", "Crear Carpeta de Imágenes",
                    "No se pudo crear el directorio de imágenes dentro de Documentos.", e);
        }
    }

    private void cargarCombos() {
        cmbEstado.setItems(FXCollections.observableArrayList(hospitalDAO.listarEstados()));
        cmbEstatus.setItems(FXCollections.observableArrayList("activo", "inactivo"));
        cmbEstatus.setValue("activo");
    }

    private void cargarCheckboxesServicios() {
        panelServicios.getChildren().clear();
        filasServicios.clear();

        VBox contenedorLista = new VBox(6);
        for (Servicio s : hospitalDAO.listarServiciosActivos()) {
            CheckBox chk = new CheckBox(s.getNombre());
            chk.setPrefWidth(145);

            TextField txtCar = new TextField();
            txtCar.setPromptText("Características del servicio...");
            txtCar.setPrefWidth(260);
            txtCar.setDisable(true);

            TextField txtCos = new TextField();
            txtCos.setPromptText("Costo ($)");
            txtCos.setPrefWidth(95);
            txtCos.setDisable(true);

            chk.selectedProperty().addListener((obs, antes, seleccionado) -> {
                txtCar.setDisable(!seleccionado);
                txtCos.setDisable(!seleccionado);
                if (!seleccionado) {
                    txtCar.clear();
                    txtCos.clear();
                }
            });

            HBox fila = new HBox(8, chk, txtCar, txtCos);
            fila.setAlignment(Pos.CENTER_LEFT);
            contenedorLista.getChildren().add(fila);

            filasServicios.add(new FilaServicioUI(s, chk, txtCar, txtCos));
        }

        ScrollPane scroll = new ScrollPane(contenedorLista);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(135);
        scroll.setPrefWidth(540);
        scroll.setStyle("-fx-background-color: transparent;");

        panelServicios.getChildren().add(scroll);
    }

    private List<ServicioDetalle> obtenerServiciosSeleccionados() {
        List<ServicioDetalle> lista = new ArrayList<>();
        for (FilaServicioUI fila : filasServicios) {
            if (fila.check.isSelected()) {
                String car = fila.txtCaracteristicas.getText().trim();
                String costoTxt = fila.txtCosto.getText().trim();
                double costo = costoTxt.isEmpty() ? 0.0 : Double.parseDouble(costoTxt);
                lista.add(new ServicioDetalle(fila.servicio.getIdServicio(), car, costo));
            }
        }
        return lista;
    }

    private void marcarServiciosDelHospital(int idHospital) {
        Map<Integer, ServicioDetalle> asignados = hospitalDAO.obtenerServiciosDetallePorHospital(idHospital);
        for (FilaServicioUI fila : filasServicios) {
            int idServ = fila.servicio.getIdServicio();
            if (asignados.containsKey(idServ)) {
                ServicioDetalle det = asignados.get(idServ);
                fila.check.setSelected(true);
                fila.txtCaracteristicas.setText(det.getCaracteristicas());
                fila.txtCosto.setText(String.valueOf(det.getCosto()));
            } else {
                fila.check.setSelected(false);
                fila.txtCaracteristicas.clear();
                fila.txtCosto.clear();
            }
        }
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colEntidad.setCellValueFactory(c -> c.getValue().nombreEstadoProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colEstatus.setCellValueFactory(c -> c.getValue().estatusProperty());

        tabla.setItems(listaHospitales);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarHospitalEnFormulario(actual);
            }
        });
    }

    private void cargarTabla() {
        listaHospitales.setAll(hospitalDAO.listarHospitales());
    }

    private void cargarHospitalEnFormulario(Hospital h) {
        hospitalSeleccionado = h;
        archivoFotoSeleccionada = null;

        txtNombre.setText(h.getNombre());
        txtDireccion.setText(h.getDireccion());
        txtTelefono.setText(h.getTelefono());
        cmbEstatus.setValue(h.getEstatus());

        for (Estado est : cmbEstado.getItems()) {
            if (est.getIdEstado() == h.getIdEstado()) {
                cmbEstado.setValue(est);
                break;
            }
        }

        marcarServiciosDelHospital(h.getIdHospital());

        if (h.getFoto() != null && !h.getFoto().isEmpty()) {
            Path rutaFoto = CARPETA_IMAGENES.resolve(h.getFoto());
            File archivo = rutaFoto.toFile();
            if (archivo.exists()) {
                lblFoto.setText(h.getFoto());
                imgFoto.setImage(new Image(archivo.toURI().toString()));
            } else {
                lblFoto.setText("Archivo no encontrado");
                imgFoto.setImage(null);
            }
        } else {
            lblFoto.setText("Sin foto");
            imgFoto.setImage(null);
        }

        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);
        mensaje("", true);
    }

    @FXML
    private void elegirFoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Elegir foto del hospital");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );
        File archivo = fc.showOpenDialog(txtNombre.getScene().getWindow());
        if (archivo != null) {
            archivoFotoSeleccionada = archivo;
            lblFoto.setText(archivo.getName());
            imgFoto.setImage(new Image(archivo.toURI().toString()));
        }
    }

    private String guardarImagenEnDocumentos(File archivoOrigen) {
        if (archivoOrigen == null) {
            return null;
        }
        try {
            crearDirectorioImagenes();
            String nombreLimpio = archivoOrigen.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
            String nombreFinal = System.currentTimeMillis() + "_" + nombreLimpio;
            Path destino = CARPETA_IMAGENES.resolve(nombreFinal);
            Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nombreFinal;
        } catch (IOException e) {
            GestorExcepciones.manejarError("Hospitales", "Copiar Fotografía",
                    "No se pudo guardar una copia de la imagen en Documentos.", e);
            return null;
        }
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) {
            return;
        }

        String nombreArchivoFoto = guardarImagenEnDocumentos(archivoFotoSeleccionada);

        Hospital nuevo = new Hospital();
        nuevo.setNombre(txtNombre.getText().trim());
        nuevo.setDireccion(txtDireccion.getText().trim());
        nuevo.setIdEstado(cmbEstado.getValue().getIdEstado());
        nuevo.setTelefono(txtTelefono.getText().trim());
        nuevo.setEstatus(cmbEstatus.getValue());
        nuevo.setFoto(nombreArchivoFoto);

        if (hospitalDAO.insertar(nuevo, obtenerServiciosSeleccionados())) {
            cargarTabla();
            limpiar();
            mensaje("Hospital y servicios registrados correctamente.", true);
        } else {
            mensaje("No se pudo registrar el hospital.", false);
        }
    }

    @FXML
    private void actualizar() {
        if (hospitalSeleccionado == null || !validarCampos()) {
            return;
        }

        String nombreArchivoFoto = hospitalSeleccionado.getFoto();
        if (archivoFotoSeleccionada != null) {
            String nuevaFoto = guardarImagenEnDocumentos(archivoFotoSeleccionada);
            if (nuevaFoto != null) {
                nombreArchivoFoto = nuevaFoto;
            }
        }

        hospitalSeleccionado.setNombre(txtNombre.getText().trim());
        hospitalSeleccionado.setDireccion(txtDireccion.getText().trim());
        hospitalSeleccionado.setIdEstado(cmbEstado.getValue().getIdEstado());
        hospitalSeleccionado.setTelefono(txtTelefono.getText().trim());
        hospitalSeleccionado.setEstatus(cmbEstatus.getValue());
        hospitalSeleccionado.setFoto(nombreArchivoFoto);

        if (hospitalDAO.actualizar(hospitalSeleccionado, obtenerServiciosSeleccionados())) {
            cargarTabla();
            limpiar();
            mensaje("Hospital y servicios actualizados correctamente.", true);
        } else {
            mensaje("No se pudo actualizar el hospital.", false);
        }
    }

    @FXML
    private void eliminar() {
        if (hospitalSeleccionado == null) {
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText("¿Eliminar el hospital seleccionado?");
        alerta.setContentText(hospitalSeleccionado.getNombre());

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (hospitalDAO.eliminar(hospitalSeleccionado.getIdHospital())) {
                cargarTabla();
                limpiar();
                mensaje("Hospital eliminado correctamente.", true);
            } else {
                mensaje("No se pudo eliminar el hospital.", false);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        limpiar();
        mensaje("", true);
    }

    private void limpiar() {
        hospitalSeleccionado = null;
        archivoFotoSeleccionada = null;
        tabla.getSelectionModel().clearSelection();

        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        cmbEstado.setValue(null);
        cmbEstatus.setValue("activo");

        for (FilaServicioUI fila : filasServicios) {
            fila.check.setSelected(false);
            fila.txtCaracteristicas.clear();
            fila.txtCosto.clear();
        }

        lblFoto.setText("Sin foto");
        imgFoto.setImage(null);

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private boolean validarCampos() {
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String telefono = txtTelefono.getText().trim();
        Estado estado = cmbEstado.getValue();
        String estatus = cmbEstatus.getValue();

        if (nombre.isEmpty() || direccion.isEmpty() || estado == null || estatus == null) {
            mensaje("Completa nombre, dirección, estado y estatus.", false);
            return false;
        }
        if (!telefono.matches("\\d{10}")) {
            mensaje("El teléfono debe tener exactamente 10 dígitos.", false);
            return false;
        }

        for (FilaServicioUI fila : filasServicios) {
            if (fila.check.isSelected()) {
                String costoTxt = fila.txtCosto.getText().trim();
                if (!costoTxt.isEmpty()) {
                    try {
                        double c = Double.parseDouble(costoTxt);
                        if (c < 0) {
                            mensaje("El costo de " + fila.servicio.getNombre() + " no puede ser negativo.", false);
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        mensaje("Ingresa un costo numérico válido para " + fila.servicio.getNombre() + ".", false);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void mensaje(String texto, boolean exito) {
        lblMensaje.setStyle(exito ? "-fx-text-fill: #0F766E;" : "-fx-text-fill: #DC2626;");
        lblMensaje.setText(texto);
    }
}