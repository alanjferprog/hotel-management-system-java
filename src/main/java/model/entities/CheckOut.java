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

        // marcar reserva como cancelada (checkout) pero no poner la habitación disponible;
        // en su lugar marcamos la habitación como pendiente
        reserva.cancelar();
        if (reserva.getHabitacion() != null) {
            reserva.getHabitacion().setEstado(EstadoHabitacion.PENDIENTE_LIMPIEZA);
        }
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public Reserva getReserva() { return reserva; }
    public double getTotalConsumido() { return totalConsumido; }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public void setTotalConsumido(double totalConsumido) {
        this.totalConsumido = totalConsumido;
    }
}
