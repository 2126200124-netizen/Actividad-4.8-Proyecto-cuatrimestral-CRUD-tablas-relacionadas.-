package controladores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class Vista_usuariosController {

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private TableColumn<Usuario, String> colEstatus;

    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private ComboBox<String> cmbEstatus;
    @FXML private Label lblMensaje;

    private Usuario usuarioSeleccionado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getIdUsuario()).asObject());
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreResponsable()));
        colCorreo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCorreo()));
        colRol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRol()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreEstado()));
        colEstatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstatus()));

        cmbRol.setItems(FXCollections.observableArrayList("administrador", "encargado_region", "consulta"));
        cmbEstatus.setItems(FXCollections.observableArrayList("activo", "inactivo"));
        cargarEstados();
        cargarTabla();

        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual != null) {
                usuarioSeleccionado = actual;
                txtNombre.setText(actual.getNombreResponsable());
                txtCorreo.setText(actual.getCorreo());
                cmbRol.setValue(actual.getRol());
                cmbEstatus.setValue(actual.getEstatus());
                cmbEstado.setValue("Sin asignar".equals(actual.getNombreEstado()) ? null : actual.getNombreEstado());
                lblMensaje.setText("");
            }
        });
    }

    private void cargarEstados() {
        ObservableList<String> estados = FXCollections.observableArrayList();
        try (Connection con = conexionBDM.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT nombre FROM estados ORDER BY nombre")) {
            while (rs.next()) {
                estados.add(rs.getString("nombre"));
            }
            cmbEstado.setItems(estados);
        } catch (Exception e) {
            GestorExcepciones.manejarError("Usuarios", "Cargar estados",
                    "No se pudieron cargar los estados.", e);
        }
    }

    private void cargarTabla() {
        tblUsuarios.setItems(UsuarioDAO.obtenerTodos());
    }

    @FXML
    private void actualizarUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarMensaje("Selecciona un usuario de la tabla.", true);
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty() || cmbRol.getValue() == null || cmbEstatus.getValue() == null) {
            mostrarMensaje("Completa nombre, rol y estatus.", true);
            return;
        }

        Integer idEstado = obtenerIdEstado(cmbEstado.getValue());
        boolean ok = UsuarioDAO.actualizarUsuario(
                usuarioSeleccionado.getIdUsuario(),
                nombre,
                cmbRol.getValue(),
                cmbEstatus.getValue(),
                idEstado
        );

        if (ok) {
            mostrarMensaje("Usuario actualizado correctamente.", false);
            cargarTabla();
        }
    }

    @FXML
    private void eliminarUsuario() {
        if (usuarioSeleccionado == null) {
            mostrarMensaje("Selecciona un usuario para eliminar.", true);
            return;
        }
        if (usuarioSeleccionado.getIdUsuario() == Sesion.idUsuario) {
            mostrarMensaje("No puedes eliminar tu propia cuenta activa.", true);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que deseas eliminar a " + usuarioSeleccionado.getNombreResponsable() + "?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            if (UsuarioDAO.eliminarUsuario(usuarioSeleccionado.getIdUsuario())) {
                usuarioSeleccionado = null;
                txtNombre.clear();
                txtCorreo.clear();
                mostrarMensaje("Usuario eliminado.", false);
                cargarTabla();
            }
        }
    }

    private Integer obtenerIdEstado(String nombreEstado) {
        if (nombreEstado == null || nombreEstado.isEmpty()) return null;
        try (Connection con = conexionBDM.getConexion();
             PreparedStatement ps = con.prepareStatement("SELECT id_estado FROM estados WHERE nombre = ?")) {
            ps.setString(1, nombreEstado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id_estado");
            }
        } catch (Exception e) {
            GestorExcepciones.manejarError("Usuarios", "Buscar ID de estado",
                    "Error al consultar el estado seleccionado.", e);
        }
        return null;
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setStyle(esError ? "-fx-text-fill: #B42318;" : "-fx-text-fill: #0F766E;");
        lblMensaje.setText(texto);
    }
}