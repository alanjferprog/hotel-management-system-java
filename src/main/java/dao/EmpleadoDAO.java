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
        String sql = "INSERT OR IGNORE INTO empleado (id, nombre, apellido, dni, cargo, turno, estado) VALUES (?,?,?,?,?,?,?)";
        Object[][] datos = new Object[][]{
                {1, "Juan", "Perez", "12345678", "Recepcionista", "Mañana", "disponible"},
                {2, "Lucia", "Gomez", "23456789", "Recepcionista", "Mañana", "disponible"},
                {3, "Marina", "Suarez", "34567890", "Limpieza", "Mañana", "disponible"},
                {4, "Jorge", "Ramos", "45678901", "Limpieza", "Mañana", "disponible"},
                {5, "Carlos", "Benitez", "56789012", "Cocinero", "Mañana", "disponible"},
                {6, "Miguel", "Pereyra", "67890123", "Mantenimiento", "Mañana", "disponible"},

                {7, "Sofia", "Lopez", "78901234", "Recepcionista", "Tarde", "disponible"},
                {8, "Brenda", "Martinez", "89012345", "Recepcionista", "Tarde", "disponible"},
                {9, "Pablo", "Nuñez", "90123456", "Limpieza", "Tarde", "disponible"},
                {10, "Camila", "Ortiz", "11223344", "Limpieza", "Tarde", "disponible"},
                {11, "Esteban", "Molina", "22334455", "Cocinero", "Tarde", "disponible"},
                {12, "Ramon", "Acosta", "33445566", "Mantenimiento", "Tarde", "disponible"},

                {13, "Diego", "Silva", "44556677", "Recepcionista", "Noche", "disponible"},
                {14, "Valeria", "Castro", "55667788", "Recepcionista", "Noche", "disponible"},
                {15, "Natalia", "Ruiz", "66778899", "Limpieza", "Noche", "disponible"},
                {16, "Federico", "Herrera", "77889900", "Limpieza", "Noche", "disponible"},
                {17, "Gaston", "Quiroga", "88990011", "Cocinero", "Noche", "disponible"},
                {18, "Emilio", "Sosa", "99001122", "Mantenimiento", "Noche", "disponible"},

                };
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : datos) {
                ps.setInt(1, (Integer) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setString(3, (String) row[2]);
                ps.setString(4, (String) row[3]);
                ps.setString(5, (String) row[4]);
                ps.setString(6, (String) row[5]);
                ps.setString(7, (String) row[6]);
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
        String sql = "SELECT id, nombre, apellido, dni, cargo, turno, estado FROM empleado ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // Usar nombres de columna (o índices 1-based) para evitar "column 0 out of bounds"
                int idEmpleado = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String dni = rs.getString("dni");
                String cargo = rs.getString("cargo");
                String turno = rs.getString("turno");
                String estado = rs.getString("estado");
                Empleado e = new Empleado(idEmpleado, nombre, apellido, dni, cargo, turno);
                e.setEstado(estado);
                list.add(e);
            }
        } catch (SQLException ex) {
            // Propagar con mensaje más claro para la UI
            throw new SQLException("Error al leer empleados: " + ex.getMessage(), ex);
        }
        return list;
    }

    public static void insert(Connection conn, Empleado e) throws SQLException {
        // No incluir la columna id para que SQLite la genere automáticamente
        String sql = "INSERT INTO empleado (nombre, apellido, dni, cargo, turno) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getDni());
            ps.setString(4, e.getCargo());
            ps.setString(5, e.getTurno());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    // Si necesitas usar el id, podrías devolverlo o asignarlo al objeto si tu entidad lo permite
                    // e.setIdEmpleado(generatedId); // la clase Empleado no tiene setter, se deja así
                }
            }
        }
    }

    public static void updateByDni(Connection conn, Empleado e) throws SQLException {
        String sql = "UPDATE empleado SET nombre=?, apellido=?, cargo=?, turno=?, estado=? WHERE dni=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCargo());
            ps.setString(4, e.getTurno());
            // estado (usar dbValue del enum si está seteado)
            String estado = (e.getEstado() == null) ? "disponible" : e.getEstado().getDbValue();
            ps.setString(5, estado);
            ps.setString(6, e.getDni());
            ps.executeUpdate();
        }
    }

    public static void updateEstadoByDni(Connection conn, String dni, String estado) throws SQLException {
        String sql = "UPDATE empleado SET estado = ? WHERE dni = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setString(2, dni);
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
