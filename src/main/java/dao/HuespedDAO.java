package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.entities.Huesped;

public class HuespedDAO {

    public static void insertSampleData(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO cliente (id, nombre, apellido, dni, email, telefono) VALUES (?,?,?,?,?,?)";
        Object[][] datos = new Object[][]{
            {1, "Juan", "Pérez", "12345678", "juan.perez@example.com", "555-1111"},
            {2, "María", "González", "87654321", "maria.g@example.com", "555-2222"},
            {3, "Carlos", "López", "45678912", "carlos.l@example.com", "555-3333"}
        };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : datos) {
                ps.setInt(1, (Integer) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setString(6, (String) row[5]);
                try {
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    System.err.println("No se pudo insertar huesped id=" + row[0] + ": " + ex.getMessage());
                }
            }
        }
    }

    public static List<Huesped> findAll(Connection conn) throws SQLException {
        List<Huesped> list = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, dni, email, telefono FROM cliente ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String dni = rs.getString("dni");
                String email = rs.getString("email");
                String telefono = rs.getString("telefono");
                Huesped h = new Huesped(nombre, apellido, dni, email, telefono);
                list.add(h);
            }
        }
        return list;
    }

    public static void insert(Connection conn, Huesped h) throws SQLException {
        String sql = "INSERT INTO cliente (nombre, apellido, dni, email, telefono) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getNombre());
            ps.setString(2, h.getApellido());
            ps.setString(3, h.getDni());
            ps.setString(4, h.getEmail());
            ps.setString(5, h.getTelefono());
            ps.executeUpdate();
        }
    }

    public static void updateByDni(Connection conn, Huesped h) throws SQLException {
        String sql = "UPDATE cliente SET nombre=?, apellido=?, email=?, telefono=? WHERE dni=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getNombre());
            ps.setString(2, h.getApellido());
            ps.setString(3, h.getEmail());
            ps.setString(4, h.getTelefono());
            ps.setString(5, h.getDni());
            ps.executeUpdate();
        }
    }

    public static void deleteByDni(Connection conn, String dni) throws SQLException {
        String sql = "DELETE FROM cliente WHERE dni = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.executeUpdate();
        }
    }
}

