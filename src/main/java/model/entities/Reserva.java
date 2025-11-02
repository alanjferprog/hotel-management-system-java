package model.entities;

public class Reserva {
    private int idReserva;
    private Huesped huesped;
    private Habitacion habitacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;


    public Reserva() {}


    public Reserva(int idReserva, Huesped huesped, Habitacion habitacion, LocalDate fechaInicio, LocalDate fechaFin) {
        this.idReserva = idReserva;
        this.huesped = huesped;
        this.habitacion = habitacion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }


    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }
    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }
    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public double getCostoTotal() { return costoTotal; }


    public void confirmar();
    public void cancelar();


    public void calcularImporteTotal() {
        if (habitacion != null && fechaInicio != null && fechaFin != null) {
            costoTotal = noches() * habitacion.getPrecioPorNoche();
        } else {
            costoTotal = 0;
        }
    }


    @Override
    public String toString() {
        return "Reserva #" + idReserva + " - " + huesped + " - " + habitacion + " (" + fechaInicio + " -> " + fechaFin + ")";
    }
}
