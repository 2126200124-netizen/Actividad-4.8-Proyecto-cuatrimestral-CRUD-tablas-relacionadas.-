package controladores;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Almacen {

    public static final ObservableList<Solicitud> solicitudes =
            FXCollections.<Solicitud>observableArrayList(s -> new Observable[]{ s.estadoProperty() });

    static {
        solicitudes.add(new Solicitud("Hospital", "Hospital Civil de Guadalajara", "Jalisco", "aprobado"));
    }
}
