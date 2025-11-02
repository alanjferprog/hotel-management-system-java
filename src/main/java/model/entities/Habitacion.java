package model.entities;


public class Habitacion {
    private int numero;
    private String tipo;
    private double precioPorNoche;
    private String estado;

    public Habitacion(int numero, String tipo, double precioPorNoche) {
        this.numero = numero;
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
        this.estado = "disponible";
    }

    public int getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public double getPrecioPorNoche() { return precioPorNoche; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    public boolean estaDisponible() {
        return "disponible".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return "Hab " + numero + " - " + tipo + " - $" + precioPorNoche + " - " + estado;
    }
}
