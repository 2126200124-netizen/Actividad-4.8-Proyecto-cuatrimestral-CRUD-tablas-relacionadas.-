package controladores;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;

public class VistaMedicosController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private ComboBox<Hospital> cmbHospital;
    @FXML private ComboBox<String> cmbEstatus;
    @FXML private FlowPane panelEspecialidades;
    @FXML private Label lblFoto;
    @FXML private ImageView imgFoto;
    @FXML private Label lblMensaje;

    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    @FXML private TableView<Medico> tabla;
    @FXML private TableColumn<Medico, String> colNombre;
    @FXML private TableColumn<Medico, String> colHospital;
    @FXML private TableColumn<Medico, String> colEstatus;

    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final ObservableList<Medico> listaMedicos = FXCollections.observableArrayList();

    private Medico medicoSeleccionado;
    private File archivoFotoSeleccionada;

    private static final Path CARPETA_IMAGENES = Paths.get(
            System.getProperty("user.home"), "Documents", "RedMedica", "medicos");

    @FXML
    private void initialize() {
        crearDirectorioImagenes();
        cargarCombos();
        cargarCheckboxesEspecialidades();
        configurarTabla();
        cargarTabla();
    }

    private void crearDirectorioImagenes() {
        try {
            if (!Files.exists(CARPETA_IMAGENES)) {
                Files.createDirectories(CARPETA_IMAGENES);
            }
        } catch (IOException e) {
            GestorExcepciones.manejarError("Médicos", "Crear Carpeta de Imágenes",
                    "No se pudo crear el directorio de imágenes dentro de Documentos.", e);
        }
    }

    private void cargarCombos() {
        cmbHospital.setItems(FXCollections.observableArrayList(medicoDAO.listarHospitalesActivos()));
        cmbEstatus.setItems(FXCollections.observableArrayList("activo", "inactivo"));
        cmbEstatus.setValue("activo");
    }

    private void cargarCheckboxesEspecialidades() {
        panelEspecialidades.getChildren().clear();
        for (Especialidad esp : medicoDAO.listarEspecialidadesActivas()) {
            CheckBox chk = new CheckBox(esp.getNombre());
            chk.setUserData(esp);
            panelEspecialidades.getChildren().add(chk);
        }
    }

    private List<Integer> obtenerEspecialidadesSeleccionadas() {
        List<Integer> ids = new ArrayList<>();
        for (Node nodo : panelEspecialidades.getChildren()) {
            if (nodo instanceof CheckBox) {
                CheckBox chk = (CheckBox) nodo;
                if (chk.isSelected() && chk.getUserData() instanceof Especialidad) {
                    ids.add(((Especialidad) chk.getUserData()).getIdEspecialidad());
                }
            }
        }
        return ids;
    }

    private void marcarEspecialidadesDelMedico(int idMedico) {
        List<Integer> idsAsignados = medicoDAO.obtenerIdsEspecialidadesPorMedico(idMedico);
        for (Node nodo : panelEspecialidades.getChildren()) {
            if (nodo instanceof CheckBox) {
                CheckBox chk = (CheckBox) nodo;
                if (chk.getUserData() instanceof Especialidad) {
                    int idEsp = ((Especialidad) chk.getUserData()).getIdEspecialidad();
                    chk.setSelected(idsAsignados.contains(idEsp));
                }
            }
        }
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(c -> c.getValue().nombreCompletoProperty());
        colHospital.setCellValueFactory(c -> c.getValue().nombreHospitalProperty());
        colEstatus.setCellValueFactory(c -> c.getValue().estatusProperty());

        tabla.setItems(listaMedicos);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                cargarMedicoEnFormulario(actual);
            }
        });
    }

    private void cargarTabla() {
        listaMedicos.setAll(medicoDAO.listarMedicos());
    }

    private void cargarMedicoEnFormulario(Medico m) {
        medicoSeleccionado = m;
        archivoFotoSeleccionada = null;

        txtNombre.setText(m.getNombre());
        txtApellidos.setText(m.getApellidos());
        cmbEstatus.setValue(m.getEstatus());

        for (Hospital hosp : cmbHospital.getItems()) {
            if (hosp.getIdHospital() == m.getIdHospital()) {
                cmbHospital.setValue(hosp);
                break;
            }
        }

        marcarEspecialidadesDelMedico(m.getIdMedico());

        if (m.getFoto() != null && !m.getFoto().isEmpty()) {
            Path rutaFoto = CARPETA_IMAGENES.resolve(m.getFoto());
            File archivo = rutaFoto.toFile();
            if (archivo.exists()) {
                lblFoto.setText(m.getFoto());
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
        fc.setTitle("Elegir foto del médico");
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
            GestorExcepciones.manejarError("Médicos", "Copiar Fotografía",
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

        Medico nuevo = new Medico();
        nuevo.setNombre(txtNombre.getText().trim());
        nuevo.setApellidos(txtApellidos.getText().trim());
        nuevo.setIdHospital(cmbHospital.getValue().getIdHospital());
        nuevo.setEstatus(cmbEstatus.getValue());
        nuevo.setFoto(nombreArchivoFoto);

        if (medicoDAO.insertar(nuevo, obtenerEspecialidadesSeleccionadas())) {
            cargarTabla();
            limpiar();
            mensaje("Médico y especialidades registrados correctamente.", true);
        } else {
            mensaje("No se pudo registrar el médico.", false);
        }
    }

    @FXML
    private void actualizar() {
        if (medicoSeleccionado == null || !validarCampos()) {
            return;
        }

        String nombreArchivoFoto = medicoSeleccionado.getFoto();
        if (archivoFotoSeleccionada != null) {
            String nuevaFoto = guardarImagenEnDocumentos(archivoFotoSeleccionada);
            if (nuevaFoto != null) {
                nombreArchivoFoto = nuevaFoto;
            }
        }

        medicoSeleccionado.setNombre(txtNombre.getText().trim());
        medicoSeleccionado.setApellidos(txtApellidos.getText().trim());
        medicoSeleccionado.setIdHospital(cmbHospital.getValue().getIdHospital());
        medicoSeleccionado.setEstatus(cmbEstatus.getValue());
        medicoSeleccionado.setFoto(nombreArchivoFoto);

        if (medicoDAO.actualizar(medicoSeleccionado, obtenerEspecialidadesSeleccionadas())) {
            cargarTabla();
            limpiar();
            mensaje("Médico y especialidades actualizados correctamente.", true);
        } else {
            mensaje("No se pudo actualizar el médico.", false);
        }
    }

    @FXML
    private void eliminar() {
        if (medicoSeleccionado == null) {
            return;
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText("¿Eliminar el médico seleccionado?");
        alerta.setContentText(medicoSeleccionado.getNombreCompleto());

        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (medicoDAO.eliminar(medicoSeleccionado.getIdMedico())) {
                cargarTabla();
                limpiar();
                mensaje("Médico eliminado correctamente.", true);
            } else {
                mensaje("No se pudo eliminar el médico.", false);
            }
        }
    }

    @FXML
    private void limpiarFormulario() {
        limpiar();
        mensaje("", true);
    }

    private void limpiar() {
        medicoSeleccionado = null;
        archivoFotoSeleccionada = null;
        tabla.getSelectionModel().clearSelection();

        txtNombre.clear();
        txtApellidos.clear();
        cmbHospital.setValue(null);
        cmbEstatus.setValue("activo");
        panelEspecialidades.getChildren().forEach(n -> ((CheckBox) n).setSelected(false));

        lblFoto.setText("Sin foto");
        imgFoto.setImage(null);

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private boolean validarCampos() {
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        Hospital hospital = cmbHospital.getValue();
        String estatus = cmbEstatus.getValue();

        if (nombre.isEmpty() || apellidos.isEmpty() || hospital == null || estatus == null) {
            mensaje("Completa nombre, apellidos, hospital y estatus.", false);
            return false;
        }
        return true;
    }

    private void mensaje(String texto, boolean exito) {
        lblMensaje.setStyle(exito ? "-fx-text-fill: #0F766E;" : "-fx-text-fill: #DC2626;");
        lblMensaje.setText(texto);
    }
}