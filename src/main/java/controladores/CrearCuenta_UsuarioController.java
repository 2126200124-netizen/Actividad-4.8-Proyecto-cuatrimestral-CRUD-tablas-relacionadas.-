package controladores;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class CrearCuenta_UsuarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtConfirmar;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<String> cmbEstado;
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        cmbRol.setItems(FXCollections.observableArrayList("administrador", "encargado_region", "consulta"));
        cargarEstados();
    }

    private void cargarEstados() {
        ObservableList<String> listaEstados = FXCollections.observableArrayList();
        String query = "SELECT nombre FROM estados";
        
        try (Connection con = conexionBDM.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
             
            while (rs.next()) {
                listaEstados.add(rs.getString("nombre"));
            }
            cmbEstado.setItems(listaEstados);
            
        } catch (Exception e) {
            GestorExcepciones.manejarError("Usuarios", "Cargar Estados", "No se pudieron cargar los estados en el ComboBox.", e);
        }
    }

    @FXML
    private void registrar() {
        lblMensaje.setStyle("-fx-text-fill: #B42318;");
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String pass = txtContrasena.getText();
        String confirmar = txtConfirmar.getText();
        String rol = cmbRol.getValue();
        String estadoNombre = cmbEstado.getValue();

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty() || rol == null) {
            lblMensaje.setText("Completa todos los campos obligatorios.");
            return;
        }
        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) {
            lblMensaje.setText("Correo no válido.");
            return;
        }
        if (pass.length() < 8) {
            lblMensaje.setText("La contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!pass.equals(confirmar)) {
            lblMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        try {
            conexionBDM.iniciarTransaccion();

            int idEstado = 0;
            if (estadoNombre != null) {
                String queryEstado = "SELECT id_estado FROM estados WHERE nombre = ?";
                try (PreparedStatement psEstado = conexionBDM.getConexion().prepareStatement(queryEstado)) {
                    psEstado.setString(1, estadoNombre);
                    try (ResultSet rs = psEstado.executeQuery()) {
                        if (rs.next()) {
                            idEstado = rs.getInt("id_estado");
                        }
                    }
                }
            }

            String queryInsert = "INSERT INTO usuarios (nombre_responsable, correo, contrasena, rol, estatus, id_estado) VALUES (?, ?, ?, ?, 'activo', ?)";
            try (PreparedStatement ps = conexionBDM.getConexion().prepareStatement(queryInsert)) {
                ps.setString(1, nombre);
                ps.setString(2, correo);
                ps.setString(3, pass);
                ps.setString(4, rol);
                
                if (idEstado > 0) {
                    ps.setInt(5, idEstado);
                } else {
                    ps.setNull(5, java.sql.Types.INTEGER);
                }
                ps.executeUpdate();
            }

            conexionBDM.confirmarTransaccion();

            lblMensaje.setStyle("-fx-text-fill: #0F766E;");
            lblMensaje.setText("Cuenta creada exitosamente. Regresando...");

            PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
            pausa.setOnFinished(e -> {
                try { volver(); } catch (IOException ex) { ex.printStackTrace(); }
            });
            pausa.play();

        } catch (Exception e) {
            conexionBDM.revertirTransaccion();
            GestorExcepciones.manejarError("Usuarios", "Registrar Nuevo Usuario", "El correo ya existe o hubo un problema de conexión.", e);
        }
    }

    @FXML
    private void volver() throws IOException {
        Navegador.cambiar(txtCorreo, "Inicio_sesion.fxml");
    }
}
