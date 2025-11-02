package view;

import model.core.*;
import model.entities.*;
import model.exceptions.*;

import java.time.LocalDate;

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

    //public void cancelarReserva(int idReserva) throws ReservaInvalidaException {
    //    hotel.cancelarReserva(idReserva);
    //}
}
