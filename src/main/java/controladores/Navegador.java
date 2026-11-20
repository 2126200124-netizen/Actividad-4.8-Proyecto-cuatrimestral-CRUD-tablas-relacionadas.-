package controladores;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class Navegador {

 
    public static void cambiar(Node origen, String fxml) throws IOException {
        Parent root = FXMLLoader.load(Navegador.class.getResource("/fxml/" + fxml));
        origen.getScene().setRoot(root);
    }

  
    public static void cargarEn(Pane contenedor, String fxml) throws IOException {
        Parent vista = FXMLLoader.load(Navegador.class.getResource("/fxml/" + fxml));
        contenedor.getChildren().setAll(vista);
    }
}
