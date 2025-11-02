package model.entities;

public class CheckIn {
    private LocalDateTime fechaHora;
    private Empleado empleadoResponsable;
    private Reserva reserva;


    public CheckIn() {}


    public CheckIn(LocalDateTime fechaHora, Empleado empleado, Reserva reserva) {
        this.fechaHora = fechaHora;
        this.empleadoResponsable = empleado;
        this.reserva = reserva;
    }


    public LocalDateTime getFechaHora() { return fechaHora; }
    public Empleado getEmpleado() { return empleado; }
    public Reserva getReserva() { return reserva; }

    public void registrarCheckIn();
}
