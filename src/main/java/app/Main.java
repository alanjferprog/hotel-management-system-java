package app;

// File: src/main/java/view/Main.java

import model.core.Hotel;
import model.entities.Habitacion;
import view.ControladorGUI;
import view.MainFrame;

import javax.swing.SwingUtilities;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Hotel hotel = new Hotel("Hotel Ejemplo");

            ControladorGUI controlador = new ControladorGUI(hotel);

            // Intentar cargar desde CSV en data/habitaciones.csv
            try {
                int cargadas = controlador.cargarHabitacionesDesdeCSV("data/habitaciones.csv");
                System.out.println("Habitaciones cargadas desde CSV: " + cargadas);
            } catch (IOException e) {
                System.out.println("No se pudo cargar CSV (se usarán datos por defecto): " + e.getMessage());

                // Habitaciones de ejemplo (fallback)
                hotel.agregarHabitacion(new Habitacion(101, "Simple", 50.0));
                hotel.agregarHabitacion(new Habitacion(102, "Doble", 80.0));
                hotel.agregarHabitacion(new Habitacion(201, "Suite", 150.0));
                hotel.agregarHabitacion(new Habitacion(202, "Suite Deluxe", 220.0));
            }

            // Intentar cargar un CSV de reservas (opcional)
            try {
                int rc = controlador.cargarReservasDesdeCSV("data/reservas.csv");
                System.out.println("Reservas cargadas desde CSV: " + rc);
            } catch (IOException e) {
                // no es crítico
            }

            MainFrame frame = new MainFrame(controlador);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
