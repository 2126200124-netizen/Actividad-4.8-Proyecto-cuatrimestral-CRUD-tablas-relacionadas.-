package controladores;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Validacion {
    private int idValidacion;
    private Integer idHospital;
    private Integer idMedico;
    private String tipo;
    private String nombre;
    private String detalle;
    private String estado;
    private Integer idUsuarioSolicita;
    private Integer idUsuarioValida;
    private String fechaSolicitud;
    private String fechaValidacion;
    private String observaciones;

    public Validacion() {
    }

    public int getIdValidacion() {
        return idValidacion;
    }

    public void setIdValidacion(int idValidacion) {
        this.idValidacion = idValidacion;
    }

    public Integer getIdHospital() {
        return idHospital;
    }

    public void setIdHospital(Integer idHospital) {
        this.idHospital = idHospital;
    }

    public Integer getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Integer idMedico) {
        this.idMedico = idMedico;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuarioSolicita() {
        return idUsuarioSolicita;
    }

    public void setIdUsuarioSolicita(Integer idUsuarioSolicita) {
        this.idUsuarioSolicita = idUsuarioSolicita;
    }

    public Integer getIdUsuarioValida() {
        return idUsuarioValida;
    }

    public void setIdUsuarioValida(Integer idUsuarioValida) {
        this.idUsuarioValida = idUsuarioValida;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(String fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public StringProperty tipoProperty() {
        return new SimpleStringProperty(tipo);
    }

    public StringProperty nombreProperty() {
        return new SimpleStringProperty(nombre);
    }

    public StringProperty detalleProperty() {
        return new SimpleStringProperty(detalle);
    }

    public StringProperty estadoProperty() {
        return new SimpleStringProperty(estado);
    }

    public StringProperty observacionesProperty() {
        return new SimpleStringProperty(observaciones != null ? observaciones : "");
    }
}