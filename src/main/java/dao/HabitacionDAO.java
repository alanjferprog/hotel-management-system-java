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
        String sql = "SELECT numero, tipo, precioPorNoche, estado FROM habitacion ORDER BY numero";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int numero = rs.getInt("numero");
                String tipo = rs.getString("tipo");
                double precio = rs.getDouble("precioPorNoche");
                String estado = rs.getString("estado");
                Habitacion h = new Habitacion(numero, tipo, precio);
                h.setEstado(estado);
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
            {102, "Simple", 45.000, "ocupada"},
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
}
