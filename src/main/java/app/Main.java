package app;

import model.core.Hotel;
import model.entities.Habitacion;
import view.ControladorGUI;
import view.MainFrame;
import javax.swing.SwingUtilities;
import controller.DatabaseInitializer;
import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import java.sql.Connection;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Inicializar la base de datos (tabla + datos ejemplo)
        DatabaseInitializer.initialize();

        // Arrancar la UI
        SwingUtilities.invokeLater(() -> {
            Hotel hotel = new Hotel("Hotel Ejemplo");

            // Intentar cargar habitaciones desde la DB
            try (Connection conn = ConexionSQLite.conectar()) {
                List<Habitacion> habitaciones = HabitacionDAO.findAll(conn);
                for (Habitacion h : habitaciones) hotel.agregarHabitacion(h);
            } catch (Exception ex) {
                System.err.println("No se pudo cargar habitaciones desde DB: " + ex.getMessage());
            }

            ControladorGUI controlador = new ControladorGUI(hotel);
            MainFrame frame = new MainFrame(controlador);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
