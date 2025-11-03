package app;

import model.core.Hotel;
import model.entities.Reserva;

import java.io.IOException;

public class TestLoader {
    public static void main(String[] args) {
        Hotel hotel = new Hotel("TestHotel");
        try {
            int h = hotel.cargarHabitacionesDesdeCSV("data/habitaciones.csv");
            System.out.println("Habitaciones cargadas: " + h);
        } catch (IOException e) {
            System.out.println("No se cargaron habitaciones: " + e.getMessage());
        }

        try {
            int r = hotel.cargarReservasDesdeCSV("data/reservas.csv");
            System.out.println("Reservas cargadas: " + r);
        } catch (IOException e) {
            System.out.println("No se cargaron reservas: " + e.getMessage());
        }

        System.out.println("Listado de reservas (size=" + hotel.getReservas().size() + "): ");
        for (Reserva res : hotel.getReservas()) {
            System.out.println(System.identityHashCode(res) + " -> " + res);
        }
    }
}
