package controladores;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Hospital {
    private int idHospital;
    private String nombre;
    private String direccion;
    private int idEstado;
    private String nombreEstado;
    private String estatus;
    private String telefono;
    private String foto;

    public Hospital() {
    }

    public Hospital(int idHospital, String nombre, String direccion, int idEstado,
                    String nombreEstado, String estatus, String telefono, String foto) {
        this.idHospital = idHospital;
        this.nombre = nombre;
        this.direccion = direccion;
        this.idEstado = idEstado;
        this.nombreEstado = nombreEstado;
        this.estatus = estatus;
        this.telefono = telefono;
        this.foto = foto;
    }

    public int getIdHospital() {
        return idHospital;
    }

    public void setIdHospital(int idHospital) {
        this.idHospital = idHospital;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(int idEstado) {
        this.idEstado = idEstado;
    }

    public String getNombreEstado() {
        return nombreEstado;
    }

    public void setNombreEstado(String nombreEstado) {
        this.nombreEstado = nombreEstado;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }


    public StringProperty nombreProperty() {
        return new SimpleStringProperty(nombre);
    }

    public StringProperty nombreEstadoProperty() {
        return new SimpleStringProperty(nombreEstado);
    }

    public StringProperty telefonoProperty() {
        return new SimpleStringProperty(telefono);
    }

    public StringProperty estatusProperty() {
        return new SimpleStringProperty(estatus);
    }

  
    @Override
    public String toString() {
        return nombre + (nombreEstado != null ? " (" + nombreEstado + ")" : "");
    }
}