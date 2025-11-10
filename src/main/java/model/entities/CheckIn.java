package model.entities;

import java.time.LocalDateTime;

public class CheckIn {
    private LocalDateTime fechaHora;
    private Empleado empleado;
    private Reserva reserva;

    public CheckIn(Empleado empleado, Reserva reserva) {
        this.fechaHora = LocalDateTime.now();
        this.empleado = empleado;
        this.reserva = reserva;
        reserva.confirmar();
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public Reserva getReserva() { return reserva; }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }
}
