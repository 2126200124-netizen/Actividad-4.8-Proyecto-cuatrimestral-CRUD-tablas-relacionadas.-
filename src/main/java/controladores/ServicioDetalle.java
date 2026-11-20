package controladores;

public class ServicioDetalle {
    private int idServicio;
    private String caracteristicas;
    private double costo;

    public ServicioDetalle(int idServicio, String caracteristicas, double costo) {
        this.idServicio = idServicio;
        this.caracteristicas = caracteristicas;
        this.costo = costo;
    }

    public int getIdServicio() { return idServicio; }
    public String getCaracteristicas() { return caracteristicas; }
    public double getCosto() { return costo; }
}