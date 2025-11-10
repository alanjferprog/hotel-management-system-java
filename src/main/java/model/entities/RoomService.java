package model.entities;

public class RoomService {
    private int idService;
    private String tipo;
    private String descripcion;
    private double costo;

    public RoomService(int idService, String tipo, String descripcion, double costo) {
        this.idService = idService;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.costo = costo;
    }

    public int getIdService() { return idService; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public double getCosto() { return costo; }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }

    @Override
    public String toString() {
        return "RoomService #" + idService + " " + tipo + " $" + costo;
    }
}
