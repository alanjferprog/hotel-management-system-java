package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.entities.Empleado;

public class EmpleadoDAO {

    public static void insertSampleData(Connection conn) throws SQLException {
        String sql = "INSERT OR IGNORE INTO empleado (id, nombre, apellido, dni, cargo, turno) VALUES (?,?,?,?,?,?)";
        Object[][] datos = new Object[][]{
            {1, "Alan", "Fernandez", "12345678", "Recepcionista", "Mañana"},
            {2, "Kevin", "Mediña", "87654321", "Recepcionista", "Tarde"},
            {3, "Juan", "Saavedra", "95678912", "Recepcionista", "Noche"},
            {4, "Nahuel", "Kryc", "23438912", "Limpieza", "Mañana"},
            {5, "Pedro", "Gonzales", "45658212", "Cocinero", "Mañana"},
            {6, "Alberto", "Maurice", "45654391", "Limpieza", "Noche"},
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
                    System.err.println("No se pudo insertar empleado id=" + row[0] + ": " + ex.getMessage());
                }
            }
        }
    }

    public static List<Empleado> findAll(Connection conn) throws SQLException {
        List<Empleado> list = new ArrayList<>();
        String sql = "SELECT id, nombre, apellido, dni, cargo, turno FROM empleado ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int idEmpleado = rs.getInt(0);
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String dni = rs.getString("dni");
                String cargo = rs.getString("cargo");
                String turno = rs.getString("turno");
                Empleado e = new Empleado(idEmpleado, nombre, apellido, dni, cargo, turno);
                list.add(e);
            }
        }
        return list;
    }

    public static void insert(Connection conn, Empleado e) throws SQLException {
        String sql = "INSERT INTO empleado (id, nombre, apellido, dni, cargo, turno) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, e.getIdEmpleado());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellido());
            ps.setString(4, e.getDni());
            ps.setString(5, e.getCargo());
            ps.setString(6, e.getTurno());
            ps.executeUpdate();
        }
    }

    public static void updateByDni(Connection conn, Empleado e) throws SQLException {
        String sql = "UPDATE empleado SET nombre=?, apellido=?, cargo=?, turno=? WHERE dni=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCargo());
            ps.setString(4, e.getTurno());
            ps.setString(5, e.getDni());
            ps.executeUpdate();
        }
    }

    public static void deleteByDni(Connection conn, String dni) throws SQLException {
        String sql = "DELETE FROM empleado WHERE dni = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.executeUpdate();
        }
    }
}

