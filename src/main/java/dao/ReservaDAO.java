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
            {1, "2025-12-01", "2025-12-05", 101, "Juan", "Perez", "12345678", "juan@example.com", "555-1234", "pendiente"},
            {2, "2025-11-10", "2025-11-12", 103, "Maria", "Gonzalez", "87654321", "maria@example.com", "555-9876", "pendiente"}
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
