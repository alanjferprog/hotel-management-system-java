package app;

import model.core.Hotel;
import model.entities.Habitacion;
import view.ControladorGUI;
import view.MainFrame;
import javax.swing.SwingUtilities;
import controller.DatabaseInitializer;
import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import dao.EmpleadoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        System.out.println("[DEBUG] Main start");
        // 1) Inicializar la base de datos (tabla + datos ejemplo)
        DatabaseInitializer.initialize();
        System.out.println("[DEBUG] DB initialized");

        // Registrar handler global para excepciones no atrapadas (incluye EDT)
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("[FATAL] Uncaught exception in thread " + t.getName() + ": " + e.getMessage());
            e.printStackTrace();
        });

        // Si se ejecuta en modo diagnóstico, no arrancamos la UI
        for (String a : args) {
            if ("--diag".equalsIgnoreCase(a)) {
                runDiag();
                return;
            }
        }

        // 2) Arrancar la UI
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[DEBUG] Swing invokeLater start");
                Hotel hotel = new Hotel("Hotel Ejemplo");

                // Intentar cargar habitaciones desde la DB
                try (Connection conn = ConexionSQLite.conectar()) {
                    List<Habitacion> habitaciones = HabitacionDAO.findAll(conn);
                    for (Habitacion h : habitaciones) hotel.agregarHabitacion(h);
                    System.out.println("Habitaciones cargadas desde DB: " + habitaciones.size());
                } catch (Exception ex) {
                    System.err.println("No se pudo cargar habitaciones desde DB: " + ex.getMessage());
                    ex.printStackTrace();
                }

                ControladorGUI controlador = new ControladorGUI(hotel);

                System.out.println("[DEBUG] Antes de crear MainFrame");
                MainFrame frame = new MainFrame(controlador);
                System.out.println("[DEBUG] MainFrame creado");
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                System.out.println("[DEBUG] Frame visible");
            } catch (Throwable t) {
                System.err.println("[ERROR] Excepción durante inicialización UI: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private static void runDiag() {
        System.out.println("[DIAG] Modo diagnóstico: comprobando BD y carga de datos");
        try {
            try (Connection conn = ConexionSQLite.conectar()) {
                // contar habitaciones
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM habitacion"); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("[DIAG] Habitaciones en BD: " + rs.getInt("cnt"));
                }
                // contar reservas
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM reserva"); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("[DIAG] Reservas en BD: " + rs.getInt("cnt"));
                }
                // contar empleados
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM empleado"); ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) System.out.println("[DIAG] Empleados en BD: " + rs.getInt("cnt"));
                }

                // listar algunas habitaciones
                try (PreparedStatement ps = conn.prepareStatement("SELECT numero, tipo, precioPorNoche, estado FROM habitacion ORDER BY numero LIMIT 10"); ResultSet rs = ps.executeQuery()) {
                    System.out.println("[DIAG] Primeras habitaciones:");
                    while (rs.next()) {
                        System.out.println(" - #" + rs.getInt("numero") + " " + rs.getString("tipo") + " $" + rs.getDouble("precioPorNoche") + " estado=" + rs.getString("estado"));
                    }
                }

            }
            System.out.println("[DIAG] Fin diagnóstico");
        } catch (Exception ex) {
            System.err.println("[DIAG] Error durante diagnóstico: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
