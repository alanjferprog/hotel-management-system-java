package model.entities;

import java.time.LocalDateTime;

public class CheckOut {
    private LocalDateTime fechaHora;
    private Empleado empleado;
    private Reserva reserva;
    private double totalConsumido;

    public CheckOut(Empleado empleado, Reserva reserva, double totalConsumido) {
        this.fechaHora = LocalDateTime.now();
        this.empleado = empleado;
        this.reserva = reserva;
        this.totalConsumido = totalConsumido;
        reserva.cancelar();
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public Reserva getReserva() { return reserva; }
    public double getTotalConsumido() { return totalConsumido; }
}
