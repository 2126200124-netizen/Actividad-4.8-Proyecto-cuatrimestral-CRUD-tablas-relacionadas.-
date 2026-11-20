package controladores;

import java.io.IOException;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ConsultaController {

    private static final String TODOS = "Todos";

  
    @FXML private ComboBox<String> cmbEstadoHosp;
    @FXML private TextField txtBuscaHosp;
    @FXML private Label lblConteoHosp;
    @FXML private TableView<DatosConsulta.Hospital> tablaHosp;
    @FXML private TableColumn<DatosConsulta.Hospital, String> colHNombre;
    @FXML private TableColumn<DatosConsulta.Hospital, String> colHEstado;
    @FXML private TableColumn<DatosConsulta.Hospital, String> colHDireccion;
    @FXML private TableColumn<DatosConsulta.Hospital, String> colHTelefono;


    @FXML private ComboBox<String> cmbEstadoMed;
    @FXML private ComboBox<String> cmbEspecialidad;
    @FXML private TextField txtBuscaMed;
    @FXML private Label lblConteoMed;
    @FXML private TableView<DatosConsulta.Medico> tablaMed;
    @FXML private TableColumn<DatosConsulta.Medico, String> colMNombre;
    @FXML private TableColumn<DatosConsulta.Medico, String> colMEspecialidad;
    @FXML private TableColumn<DatosConsulta.Medico, String> colMHospital;
    @FXML private TableColumn<DatosConsulta.Medico, String> colMEstado;


    @FXML private ComboBox<String> cmbHospitalServ;
    @FXML private Label lblInfoHosp;
    @FXML private TableView<DatosConsulta.ServicioHospital> tablaServ;
    @FXML private TableColumn<DatosConsulta.ServicioHospital, String> colSNombre;
    @FXML private TableColumn<DatosConsulta.ServicioHospital, String> colSCaracteristicas;
    @FXML private TableColumn<DatosConsulta.ServicioHospital, String> colSCosto;

    private FilteredList<DatosConsulta.Hospital> hospFiltrados;
    private FilteredList<DatosConsulta.Medico> medFiltrados;
    private final Label avisoServicios = new Label();

    @FXML
    private void initialize() {
        DatosConsulta.cargarDesdeBD();
        iniciarHospitales();
        iniciarMedicos();
        iniciarServicios();
    }

    
    private void iniciarHospitales() {
        cmbEstadoHosp.getItems().add(TODOS);
        cmbEstadoHosp.getItems().addAll(DatosConsulta.estados());
        cmbEstadoHosp.setValue(TODOS);

        colHNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colHEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        colHDireccion.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion()));
        colHTelefono.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

        hospFiltrados = new FilteredList<>(FXCollections.observableArrayList(DatosConsulta.HOSPITALES));
        SortedList<DatosConsulta.Hospital> ordenados = new SortedList<>(hospFiltrados);
        ordenados.comparatorProperty().bind(tablaHosp.comparatorProperty());
        tablaHosp.setItems(ordenados);
        tablaHosp.setPlaceholder(new Label("No hay hospitales con esos filtros."));

        cmbEstadoHosp.valueProperty().addListener((o, a, b) -> filtrarHospitales());
        txtBuscaHosp.textProperty().addListener((o, a, b) -> filtrarHospitales());
        filtrarHospitales();
    }

    private void filtrarHospitales() {
        String estado = cmbEstadoHosp.getValue();
        String texto = txtBuscaHosp.getText().trim().toLowerCase();
        hospFiltrados.setPredicate(h ->
                (estado == null || TODOS.equals(estado) || h.getEstado().equals(estado))
                && h.getNombre().toLowerCase().contains(texto));
        lblConteoHosp.setText(hospFiltrados.size() + " hospital(es)");
    }


    private void iniciarMedicos() {
        cmbEstadoMed.getItems().add(TODOS);
        cmbEstadoMed.getItems().addAll(DatosConsulta.estados());
        cmbEstadoMed.setValue(TODOS);

        cmbEspecialidad.getItems().add(TODOS);
        cmbEspecialidad.getItems().addAll(DatosConsulta.especialidades());
        cmbEspecialidad.setValue(TODOS);

        colMNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        colMEspecialidad.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEspecialidadesTexto()));
        colMHospital.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHospital()));
        colMEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));

        medFiltrados = new FilteredList<>(FXCollections.observableArrayList(DatosConsulta.MEDICOS));
        SortedList<DatosConsulta.Medico> ordenados = new SortedList<>(medFiltrados);
        ordenados.comparatorProperty().bind(tablaMed.comparatorProperty());
        tablaMed.setItems(ordenados);
        tablaMed.setPlaceholder(new Label("No hay médicos con esos filtros."));

        cmbEstadoMed.valueProperty().addListener((o, a, b) -> filtrarMedicos());
        cmbEspecialidad.valueProperty().addListener((o, a, b) -> filtrarMedicos());
        txtBuscaMed.textProperty().addListener((o, a, b) -> filtrarMedicos());
        filtrarMedicos();
    }

    private void filtrarMedicos() {
        String estado = cmbEstadoMed.getValue();
        String especialidad = cmbEspecialidad.getValue();
        String texto = txtBuscaMed.getText().trim().toLowerCase();
        medFiltrados.setPredicate(m ->
                (estado == null || TODOS.equals(estado) || m.getEstado().equals(estado))
                && (especialidad == null || TODOS.equals(especialidad)
                        || m.getEspecialidades().contains(especialidad))
                && m.getNombre().toLowerCase().contains(texto));
        lblConteoMed.setText(medFiltrados.size() + " médico(s)");
    }

   
    private void iniciarServicios() {
        for (DatosConsulta.Hospital h : DatosConsulta.HOSPITALES) {
            cmbHospitalServ.getItems().add(h.getNombre());
        }

        colSNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getServicio()));
        colSCaracteristicas.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCaracteristicas()));
        colSCosto.setCellValueFactory(c -> new SimpleStringProperty(
                String.format(Locale.US, "$%,.2f", c.getValue().getCosto())));
        colSCosto.setStyle("-fx-alignment: CENTER-RIGHT;");
        tablaServ.setPlaceholder(avisoServicios);

        cmbHospitalServ.valueProperty().addListener((o, a, b) -> mostrarServicios(b));
        cmbHospitalServ.getSelectionModel().selectFirst();
    }

    private void mostrarServicios(String nombreHospital) {
        if (nombreHospital == null) {
            tablaServ.getItems().clear();
            lblInfoHosp.setText("");
            avisoServicios.setText("Elige un hospital para ver sus servicios.");
            return;
        }
        tablaServ.setItems(FXCollections.observableArrayList(DatosConsulta.serviciosDe(nombreHospital)));
        avisoServicios.setText("Este hospital no tiene servicios registrados.");
        DatosConsulta.Hospital h = DatosConsulta.buscarHospital(nombreHospital);
        if (h != null) {
            lblInfoHosp.setText(h.getEstado() + "  |  " + h.getDireccion() + "  |  Tel. " + h.getTelefono());
        }
    }

    @FXML
    private void volver() throws IOException {
        Navegador.cambiar(tablaHosp, "Inicio_sesion.fxml");
    }
}
