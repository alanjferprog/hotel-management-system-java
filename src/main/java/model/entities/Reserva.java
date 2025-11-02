package model.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Reserva {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private int idReserva;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Habitacion habitacion;
    private Huesped huesped;
    private Empleado empleadoResponsable;
    private String estado;

    public Reserva(LocalDate fechaInicio, LocalDate fechaFin, Habitacion habitacion, Huesped huesped, Empleado empleadoResponsable) {
        this.idReserva = COUNTER.getAndIncrement();
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.habitacion = habitacion;
        this.huesped = huesped;
        this.empleadoResponsable = empleadoResponsable;
        this.estado = "pendiente";
    }

    public int getIdReserva() { return idReserva; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Habitacion getHabitacion() { return habitacion; }
    public Huesped getHuesped() { return huesped; }
    public Empleado getEmpleadoResponsable() { return empleadoResponsable; }
    public String getEstado() { return estado; }

    public void confirmar() { this.estado = "confirmada"; habitacion.setEstado("ocupada"); }
    public void cancelar() { this.estado = "cancelada"; habitacion.setEstado("disponible"); }

    public long calcularImporteTotal() {
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (noches <= 0) noches = 1;
        return Math.round(noches * habitacion.getPrecioPorNoche());
    }

    @Override
    public String toString() {
        return "Reserva #" + idReserva + " - " + huesped + " / Hab: " + habitacion.getNumero()
                + " - " + fechaInicio + " a " + fechaFin + " (" + estado + ")";
    }
}
