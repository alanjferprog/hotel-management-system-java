package model.entities;

import java.util.ArrayList;
import java.util.List;

public class Huesped extends Persona {
    private String email;
    private String telefono;
    private List<Reserva> reservas;

    public Huesped(String nombre, String apellido, String dni, String email, String telefono) {
        super(nombre, apellido, dni);
        this.email = email;
        this.telefono = telefono;
        this.reservas = new ArrayList<>();
    }

    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public List<Reserva> getReservas() { return reservas; }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public void addReserva(Reserva r) {
        reservas.add(r);
    }

    @Override
    public String toString() {
        return super.toString() + " - " + email;
    }
}