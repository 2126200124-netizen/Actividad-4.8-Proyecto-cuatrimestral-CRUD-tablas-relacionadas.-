package controladores;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Inicio_sesionController {

    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    @FXML
    private void entrar() {
        lblMensaje.setText("");
        String correo = txtCorreo.getText().trim();
        String pass = txtContrasena.getText();

        if (correo.isEmpty() || pass.isEmpty()) {
            lblMensaje.setText("Ingresa correo y contraseña.");
            return;
        }

        Usuario usuario = UsuarioDAO.iniciarSesion(correo, pass);

        if (usuario == null) {
            lblMensaje.setText("Correo o contraseña incorrectos, o cuenta inactiva.");
            return;
        }

        
        Sesion.idUsuario = usuario.getIdUsuario();
        Sesion.nombre = usuario.getNombreResponsable();
        Sesion.rol = usuario.getRol();

        try {
            switch (usuario.getRol()) {
                case "administrador":
                    Navegador.cambiar(txtCorreo, "panel_admin.fxml");
                    break;
                case "encargado_region":
                    Navegador.cambiar(txtCorreo, "panel_encargado.fxml");
                    break;
                case "consulta":
                    Navegador.cambiar(txtCorreo, "consulta.fxml");
                    break;
                default:
                    lblMensaje.setText("El usuario no tiene un rol válido asignado.");
                    break;
            }
        } catch (IOException e) {
            lblMensaje.setText("No se pudo abrir el panel.");
            GestorExcepciones.manejarError("Login", "Abrir panel de rol: " + usuario.getRol(), 
                                           "No se pudo cargar la ventana correspondiente.", e);
        }
    }

    @FXML
    private void irRegistro() throws IOException {
        Navegador.cambiar(txtCorreo, "crearCuenta_Usuario.fxml");
    }

    @FXML
    private void irConsulta() throws IOException {
        Navegador.cambiar(txtCorreo, "consulta.fxml");
    }
}