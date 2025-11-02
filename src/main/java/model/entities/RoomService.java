package model.entities;

public class RoomService {
    private int idService;
    private String tipo;
    private String descripcion;
    private double costo;
    private String estado;


    public RoomService() {}


    public RoomService(int idService, String descripcion, double costo) {
        this.idService = idService;
        this.descripcion = descripcion;
        this.costo = costo;
    }

    public int getIdService() {return idService;}
    public void setIdService(int idService) {this.idService = idService;}
    public String getTipo() {return tipo;}
    public void setTipo(String tipo) {this.tipo = tipo;}
    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}
    public double getCosto() {return costo;}
    public void setCosto(double costo) {this.costo = costo;}
    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}

    public void solicitarServicio();
    public void marcarComoCompletado();
    public void confirmarPorHuesped(Huesped h);
}
