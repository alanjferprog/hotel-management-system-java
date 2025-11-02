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

    @Override
    public String toString() {
        return "RoomService #" + idService + " " + tipo + " $" + costo;
    }
}
