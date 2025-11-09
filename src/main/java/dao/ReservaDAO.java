package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import model.entities.Reserva;
import model.entities.Huesped;

public class ReservaDAO {
    public static void insertSampleData(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO reserva (id, fechaInicio, fechaFin, numeroHab, nombre, apellido, dni, email, telefono, estado) VALUES (?,?,?,?,?,?,?,?,?,?)";
        Object[][] datos = new Object[][]{
            {1, "2025-11-08", "2025-11-10", 101, "Juan", "Perez", "12345678", "juan.perez@example.com", "555-1111", "confirmada"},
            {2, "2025-11-15", "2025-11-18", 103, "Maria", "Gonzalez", "87654321", "maria.g@example.com", "555-2222", "pendiente"},
            {3, "2025-11-05", "2025-11-07", 102, "Carlos", "Lopez", "45678912", "carlos.l@example.com", "555-3333", "cancelada"},
            {4, "2025-11-20", "2025-11-22", 105, "Lucia", "Fernandez", "33445566", "lucia.f@example.com", "555-4444", "pendiente"},
            {5, "2025-12-01", "2025-12-05", 201, "Miguel", "Rodriguez", "44556677", "miguel.r@example.com", "555-5555", "pendiente"},
            {6, "2025-11-09", "2025-11-10", 103, "Ana", "Martinez", "66778899", "ana.m@example.com", "555-6666", "confirmada"},
            {7, "2025-11-25", "2025-11-27", 203, "Sofia", "Ramirez", "77889900", "sofia.r@example.com", "555-7777", "pendiente"},
            {8, "2025-11-10", "2025-11-12", 105, "Pablo", "Gomez", "88990011", "pablo.g@example.com", "555-8888", "pendiente"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : datos) {
                ps.setInt(1, (Integer) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setInt(4, (Integer) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setString(6, (String) row[5]);
                ps.setString(7, (String) row[6]);
                ps.setString(8, (String) row[7]);
                ps.setString(9, (String) row[8]);
                ps.setString(10, (String) row[9]);
                try {
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("No se pudo insertar reserva id=" + row[0] + ": " + ex.getMessage());
                }
            }
        }
    }

    /** Inserta o reemplaza una reserva en la BD usando el id de la reserva */
    public static void insertReserva(Connection conn, Reserva r) throws SQLException {
        String sql = "INSERT OR REPLACE INTO reserva (id, fechaInicio, fechaFin, numeroHab, nombre, apellido, dni, email, telefono, estado) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            Huesped h = r.getHuesped();
            ps.setInt(1, r.getIdReserva());
            ps.setString(2, r.getFechaInicio().toString());
            ps.setString(3, r.getFechaFin().toString());
            ps.setInt(4, r.getHabitacion().getNumero());
            ps.setString(5, h.getNombre());
            ps.setString(6, h.getApellido());
            ps.setString(7, h.getDni());
            ps.setString(8, h.getEmail());
            ps.setString(9, h.getTelefono());
            ps.setString(10, r.getEstado());
            ps.executeUpdate();
        }
    }
}
