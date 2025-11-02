package model.entities;

public class CheckOut {
    private LocalDateTime fechaHora;
    private Empleado empleadoResponsable;
    private Reserva reserva;
    private double totalConsumido;


    public CheckOut() {}


    public CheckOut(LocalDateTime fechaHora, Empleado empleado, Reserva reserva) {
        this.fechaHora = fechaHora;
        this.empleadoResponsable = empleado;
        this.reserva = reserva;
    }


    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public Reserva getReserva() { return reserva; }

    public void registarCheckOut();
}
