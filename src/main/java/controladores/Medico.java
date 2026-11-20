package controladores;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Medico {
    private int idMedico;
    private String nombre;
    private String apellidos;
    private String foto;
    private int idHospital;
    private String nombreHospital;
    private String estatus;

    public Medico() {
    }

    public Medico(int idMedico, String nombre, String apellidos, String foto,
                  int idHospital, String nombreHospital, String estatus) {
        this.idMedico = idMedico;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.foto = foto;
        this.idHospital = idHospital;
        this.nombreHospital = nombreHospital;
        this.estatus = estatus;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public int getIdHospital() {
        return idHospital;
    }

    public void setIdHospital(int idHospital) {
        this.idHospital = idHospital;
    }

    public String getNombreHospital() {
        return nombreHospital;
    }

    public void setNombreHospital(String nombreHospital) {
        this.nombreHospital = nombreHospital;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }


    public StringProperty nombreCompletoProperty() {
        return new SimpleStringProperty(getNombreCompleto());
    }

    public StringProperty nombreHospitalProperty() {
        return new SimpleStringProperty(nombreHospital);
    }

    public StringProperty estatusProperty() {
        return new SimpleStringProperty(estatus);
    }
}
