package model.core;

import java.util.*;

public class Hotel {
    private String nombre;
    private List<Habitacion> habitaciones = new ArrayList<>();
    private List<Reserva> reservas = new ArrayList<>();


    public Hotel() {}


    public Hotel(String nombre) { this.nombre = nombre; }


    public void agregarHabitacion(Habitacion h) { habitaciones.add(h); }
    public void agregarReserva(Reserva r) throws ReservaInvalidaException, HabitacionNoDisponibleException {
        if (r.getHabitacion() == null) throw new ReservaInvalidaException("Reserva sin habitacion");
        if (!r.getHabitacion().isDisponible()) throw new HabitacionNoDisponibleException("Habitacion no disponible");
        r.getHabitacion().setDisponible(false);
        r.calcularCosto();
        reservas.add(r);
    }


    public void realizarCheckOut(Reserva r, Empleado empleado) throws EmpleadoNoAutorizadoException {
        if (!empleado.tienePermiso("CHECKOUT")) throw new EmpleadoNoAutorizadoException("Empleado no autorizado");
        r.getHabitacion().setDisponible(true);
        reservas.remove(r);
    }


    public Optional<Habitacion> buscarHabitacionLibrePorTipo(String tipo) {
        return habitaciones.stream().filter(h -> h.isDisponible() && h.getTipo().equalsIgnoreCase(tipo)).findFirst();
    }
