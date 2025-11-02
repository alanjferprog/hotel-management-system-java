package model.entities;

import java.util.ArrayList;
import java.util.List;

public class Huesped extends Persona {
    private String email;
    private String telefono;
    private List<Reserva> historialDeReservas = new ArrayList<Reserva>();


    public Huesped() {}


    public Huesped(String nombre, String apellido, String dni, String telefono, String email) {
        super(nombre, apellido, dni);
        this.telefono = telefono;
        this.email = email;
    }


    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return email; }
    public void setTelefono(String telefono) { this.email = email; }
    public void realizarReserva(Habitiacion h, Date fechaInicio, Date fechaFin);
    public void solicitarRoomService(String tipo, String descripcion);
    public void confirmarRoomService(RoomService servicio);

    @Override
    public String toString() {
        return "Huesped: " + super.toString() + " <" + email + ">" + " <" + telefono + ">";
    }
}