package controladores;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Solicitud {
    private final StringProperty tipo = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty detalle = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty("pendiente");
    private final StringProperty observaciones = new SimpleStringProperty("");

    public Solicitud(String tipo, String nombre, String detalle) {
        this.tipo.set(tipo);
        this.nombre.set(nombre);
        this.detalle.set(detalle);
    }

    public Solicitud(String tipo, String nombre, String detalle, String estado) {
        this(tipo, nombre, detalle);
        this.estado.set(estado);
    }

    public String getTipo() { return tipo.get(); }
    public String getNombre() { return nombre.get(); }
    public String getEstado() { return estado.get(); }
    public void setEstado(String valor) { estado.set(valor); }
    public void setObservaciones(String valor) { observaciones.set(valor); }

    public StringProperty tipoProperty() { return tipo; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty detalleProperty() { return detalle; }
    public StringProperty estadoProperty() { return estado; }
    public StringProperty observacionesProperty() { return observaciones; }
}
