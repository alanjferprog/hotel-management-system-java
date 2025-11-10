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

    /**
     * Constructor que permite especificar un id (por ejemplo al cargar desde CSV).
     * Asegura que el contador está por encima del id para evitar colisiones posteriores.
     */
    public Reserva(int idReserva, LocalDate fechaInicio, LocalDate fechaFin, Habitacion habitacion, Huesped huesped, Empleado empleadoResponsable) {
        this.idReserva = idReserva;
        // Asegurar que COUNTER avance por encima del id especificado
        COUNTER.updateAndGet(curr -> Math.max(curr, idReserva + 1));
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

    public void confirmar() { this.estado = "confirmada"; habitacion.setEstado(EstadoHabitacion.OCUPADA); }
    public void cancelar() { this.estado = "cancelada"; /* ya no tocamos el estado de la habitación aquí */ }

    public long calcularImporteTotal() {
        long noches = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
        if (noches <= 0) noches = 1;
        return Math.round(noches * habitacion.getPrecioPorNoche());
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }

    public void setHuesped(Huesped huesped) {
        this.huesped = huesped;
    }

    public void setEmpleadoResponsable(Empleado empleadoResponsable) {
        this.empleadoResponsable = empleadoResponsable;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva #" + idReserva + " - " + huesped + " / Hab: " + habitacion.getNumero()
                + " - " + fechaInicio + " a " + fechaFin + " (" + estado + ")";
    }
}
