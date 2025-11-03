package view;

import model.core.*;
import model.entities.*;
import model.exceptions.*;

import java.time.LocalDate;
import java.io.IOException;
import java.util.Optional;

public class ControladorGUI {
    private Hotel hotel;

    public ControladorGUI(Hotel hotel) {
        this.hotel = hotel;
    }

    public Hotel getHotel() { return hotel; }

    public Reserva crearReserva(LocalDate inicio, LocalDate fin, int numeroHab, Huesped huesped, Empleado empleado)
            throws HabitacionNoDisponibleException, ReservaInvalidaException {
        return hotel.crearReserva(inicio, fin, numeroHab, huesped, empleado);
    }

    public void confirmarReserva(int idReserva) throws ReservaInvalidaException {
        hotel.confirmarReserva(idReserva);
    }

    /**
     * Carga las habitaciones desde un CSV relativo a la raiz del proyecto.
     * Retorna la cantidad de habitaciones cargadas o lanza IOException si falla la lectura.
     */
    public int cargarHabitacionesDesdeCSV(String rutaRelativa) throws IOException {
        return hotel.cargarHabitacionesDesdeCSV(rutaRelativa);
    }

    /** Busca una reserva por su id en memoria. */
    public Optional<Reserva> buscarReservaPorId(int id) {
        return hotel.getReservas().stream().filter(r -> r.getIdReserva() == id).findFirst();
    }

    /**
     * Carga reservas desde un CSV simple. Devuelve la cantidad de reservas añadidas.
     * Formato esperado por línea (csv): fechaInicio,fechaFin,numeroHab,nombre,apellido,dni,email,telefono
     */
    public int cargarReservasDesdeCSV(String rutaRelativa) throws IOException {
        return hotel.cargarReservasDesdeCSV(rutaRelativa);
    }

    //public void cancelarReserva(int idReserva) throws ReservaInvalidaException {
    //    hotel.cancelarReserva(idReserva);
    //}
}
