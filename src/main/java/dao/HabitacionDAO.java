package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.entities.Habitacion;

public class HabitacionDAO {

    public static List<Habitacion> findAll(Connection conn) throws SQLException {
        List<Habitacion> list = new ArrayList<>();
        String sql = "SELECT numero, tipo, precioPorNoche, estado, empleadoAsignado FROM habitacion ORDER BY numero";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int numero = rs.getInt("numero");
                String tipo = rs.getString("tipo");
                double precio = rs.getDouble("precioPorNoche");
                String estado = rs.getString("estado");
                String empleado = null;
                try { empleado = rs.getString("empleadoAsignado"); } catch (SQLException ex) { /* columna puede no existir en versiones antiguas */ }
                Habitacion h = new Habitacion(numero, tipo, precio);
                h.setEstado(estado);
                if (empleado != null) h.setEmpleadoAsignado(empleado);
                list.add(h);
            }
        }
        return list;
    }

    public static void insertSampleData(Connection conn) throws SQLException {
        // Usar INSERT OR IGNORE y ejecutar por fila para no fallar toda la operación si una fila existe
        String sql = "INSERT OR IGNORE INTO habitacion (numero, tipo, precioPorNoche, estado) VALUES (?,?,?,?)";

        Object[][] datos = new Object[][]{
            {101, "Simple", 45.000, "disponible"},
            {102, "Simple", 45.000, "no disponible"},
            {103, "Doble", 65.000, "disponible"},
            {104, "Doble", 65.000, "disponible"},
            {105, "Suite Junior", 85.000, "disponible"},
            {106, "Suite Junior", 85.000, "disponible"},
            {107, "Suite Ejecutiva", 110.000, "disponible"},
            {108, "Suite Ejecutiva", 110.000, "disponible"},
            {109, "Familiar", 95.000, "disponible"},
            {110, "Familiar", 95.000, "disponible"},
            {201, "Simple", 50.000, "disponible"},
            {202, "Doble", 70.000, "disponible"},
            {203, "Suite Presidencial", 180.000, "disponible"},
            {204, "Familiar", 100.000, "disponible"},
            {205, "Suite Ejecutiva", 115.000, "disponible"}
        };

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Object[] row : datos) {
                try {
                    ps.setInt(1, (Integer) row[0]);
                    ps.setString(2, (String) row[1]);
                    ps.setDouble(3, (Double) row[2]);
                    ps.setString(4, (String) row[3]);
                    int affected = ps.executeUpdate();
                    // affected==0 means ignored (ya existía)
                } catch (SQLException e) {
                    // Loguear y continuar con las demás filas
                    System.err.println("No se pudo insertar habitacion " + row[0] + ": " + e.getMessage());
                }
            }
        }
    }

    // Nuevo: actualizar el estado de una habitación por su número
    public static void updateEstado(Connection conn, int numero, String estado) throws SQLException {
        String sql = "UPDATE habitacion SET estado = ? WHERE numero = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, numero);
            ps.executeUpdate();
        }
    }

    // Nuevo: actualizar el empleado asignado para una habitacion
    public static void updateEmpleadoAsignado(Connection conn, int numero, String dniEmpleado) throws SQLException {
        // Intentar actualizar columna empleadoAsignado; si no existe, ignorar
        try {
            String sql = "UPDATE habitacion SET empleadoAsignado = ? WHERE numero = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dniEmpleado);
                ps.setInt(2, numero);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            // si la columna no existe, no hacemos nada (se puede migrar la DB si hace falta)
            System.err.println("No se pudo actualizar empleadoAsignado (columna ausente): " + ex.getMessage());
        }
    }

}
