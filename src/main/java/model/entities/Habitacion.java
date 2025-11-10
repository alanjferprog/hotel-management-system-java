package model.entities;

public class Habitacion {
    private int numero;
    private String tipo;
    private double precioPorNoche;
    private EstadoHabitacion estado;
    private String empleadoAsignado; // DNI del empleado asignado (opcional)

    public Habitacion(int numero, String tipo, double precioPorNoche) {
        this.numero = numero;
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
        this.estado = EstadoHabitacion.DISPONIBLE;
        this.empleadoAsignado = null;
    }

    public int getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public double getPrecioPorNoche() { return precioPorNoche; }
    public EstadoHabitacion getEstado() { return estado; }
    public String getEmpleadoAsignado() { return empleadoAsignado; }

    // compatibilidad: setEstado por enum
    public void setEstado(EstadoHabitacion estado) { this.estado = estado; }
    // compatibilidad: setEstado por String (útil para DAO que lee DB)
    public void setEstado(String estadoStr) { this.estado = EstadoHabitacion.fromString(estadoStr); }

    public void setEmpleadoAsignado(String dni) { this.empleadoAsignado = dni; }

    public boolean estaDisponible() {
        return EstadoHabitacion.DISPONIBLE.equals(this.estado);
    }

    @Override
    public String toString() {
        return "Hab " + numero + " - " + tipo + " - $" + precioPorNoche + " - " + (estado == null ? "" : estado.getLabel()) + (empleadoAsignado == null ? "" : " (emp:"+empleadoAsignado+")");
    }
}
