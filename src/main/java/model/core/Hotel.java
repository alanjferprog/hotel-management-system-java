package model.core;

import model.entities.*;
import model.exceptions.HabitacionNoDisponibleException;
import model.exceptions.ReservaInvalidaException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Hotel {
    private String nombre;
    private List<Habitacion> habitaciones;
    private List<Reserva> reservas;
    private List<Empleado> empleados;

    public Hotel(String nombre) {
        this.nombre = nombre;
        this.habitaciones = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.empleados = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public List<Habitacion> getHabitaciones() { return habitaciones; }
    public List<Reserva> getReservas() { return reservas; }
    public List<Empleado> getEmpleados() { return empleados; }

    public void agregarHabitacion(Habitacion h) { habitaciones.add(h); }
    public void agregarEmpleado(Empleado e) { empleados.add(e); }

    public Optional<Habitacion> buscarHabitacionPorNumero(int numero) {
        return habitaciones.stream().filter(h -> h.getNumero() == numero).findFirst();
    }

    public List<Habitacion> listarHabitacionesDisponibles() {
        List<Habitacion> disponibles = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            if (h.estaDisponible()) disponibles.add(h);
        }
        return disponibles;
    }

    public Reserva crearReserva(LocalDate inicio, LocalDate fin, int numeroHab, Huesped huesped, Empleado empleado)
            throws HabitacionNoDisponibleException, ReservaInvalidaException {
        if (inicio == null || fin == null || inicio.isAfter(fin)) {
            throw new ReservaInvalidaException("Fechas inválidas");
        }
        Habitacion hab = buscarHabitacionPorNumero(numeroHab)
                .orElseThrow(() -> new HabitacionNoDisponibleException("No existe la habitación " + numeroHab));
        if (!hab.estaDisponible()) throw new HabitacionNoDisponibleException("Habitación no disponible: " + numeroHab);

        Reserva r = new Reserva(inicio, fin, hab, huesped, empleado);
        reservas.add(r);
        huesped.addReserva(r);
        // Marcar habitación como ocupada en memoria para bloquear nuevas reservas
        hab.setEstado("ocupada");
        return r;
    }

    public void confirmarReserva(int idReserva) throws ReservaInvalidaException {
        Reserva r = reservas.stream().filter(x -> x.getIdReserva() == idReserva).findFirst()
                .orElseThrow(() -> new ReservaInvalidaException("Reserva no encontrada"));
        r.confirmar();
    }

    // NOTE: Removed CSV-loading helper methods. Data should be loaded via a database layer (SQLite) or through
    // explicit repository/DAO classes. Keep the in-memory lists above for runtime operations.
}
