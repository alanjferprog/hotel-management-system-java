package model.entities;


public class Habitacion {
    private int numero;
    private String tipo; // e.g., Single, Double
    private double precioPorNoche;
    private boolean disponible = true;


    public Habitacion() {}


    public Habitacion(int numero, String tipo, double precioPorNoche) {
        this.numero = numero;
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
    }


    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public double getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(double precioPorNoche) { this.precioPorNoche = precioPorNoche; }
    public boolean esDisponible(Date fechaInicio, Date fechaFin) { return disponible; }
    public void actualizarEstado(boolean disponible) { this.disponible = disponible; }


    @Override
    public String toString() {
        return "Hab " + numero + " (" + tipo + ") - " + (disponible?"Disponible":"Ocupada");
    }
}
