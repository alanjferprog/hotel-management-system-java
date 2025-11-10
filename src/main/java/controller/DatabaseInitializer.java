package controller;

import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import dao.ReservaDAO;

import dao.HuespedDAO;
import dao.EmpleadoDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseInitializer {

    public static void initialize() {
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {

            // 1) Crear tabla si no existe - habitacion
            String sqlCreateHab = """
                CREATE TABLE IF NOT EXISTS habitacion (
                    numero INTEGER PRIMARY KEY,
                    tipo TEXT NOT NULL,
                    precioPorNoche REAL NOT NULL,
                    estado TEXT NOT NULL
                );
            """;
            stmt.execute(sqlCreateHab);

            // 2) Crear tabla reserva si no existe
            String sqlCreateRes = """
                CREATE TABLE IF NOT EXISTS reserva (
                    id INTEGER PRIMARY KEY,
                    fechaInicio TEXT NOT NULL,
                    fechaFin TEXT NOT NULL,
                    numeroHab INTEGER NOT NULL,
                    nombre TEXT,
                    apellido TEXT,
                    dni TEXT,
                    email TEXT,
                    telefono TEXT,
                    estado TEXT,
                    FOREIGN KEY(numeroHab) REFERENCES habitacion(numero)
                );
            """;
            stmt.execute(sqlCreateRes);

            // 3) Crear tabla cliente si no existe
            String sqlCreateCli = """
                CREATE TABLE IF NOT EXISTS cliente (
                    id INTEGER PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    apellido TEXT,
                    dni TEXT,
                    email TEXT,
                    telefono TEXT
                );
            """;
            stmt.execute(sqlCreateCli);

            // 4) Crear tabla empleado con columna estado (default 'disponible')
            String sqlCreateEmp = """
                    CREATE TABLE IF NOT EXISTS empleado (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    apellido TEXT,
                    dni TEXT,
                    cargo TEXT,
                    turno TEXT,
                    estado TEXT NOT NULL DEFAULT 'disponible'
                    );
            """;
            stmt.execute(sqlCreateEmp);

            // 5) Insertar datos de ejemplo solo si la tabla está vacía
            try {
                if (tableIsEmpty(conn, "habitacion")) {
                    HabitacionDAO.insertSampleData(conn);
                }
            } catch (SQLException ex) {
                System.err.println("Error insertando datos de ejemplo en 'habitacion': " + ex.getMessage());
                ex.printStackTrace();
            }

            try {
                if (tableIsEmpty(conn, "reserva")) {
                    ReservaDAO.insertSampleData(conn);
                }
            } catch (SQLException ex) {
                System.err.println("Error insertando datos de ejemplo en 'reserva': " + ex.getMessage());
                ex.printStackTrace();
            }

            try {
                if (tableIsEmpty(conn, "empleado")) {
                    EmpleadoDAO.insertSampleData(conn);
                }
            } catch (SQLException ex) {
                System.err.println("Error insertando datos de ejemplo en 'empleado': " + ex.getMessage());
                ex.printStackTrace();
            }

            try {
                if (tableIsEmpty(conn, "cliente")) {
                    HuespedDAO.insertSampleData(conn);
                }
            } catch (SQLException ex) {
                System.err.println("Error insertando datos de ejemplo en 'cliente': " + ex.getMessage());
                ex.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Verifica si una tabla está vacía, se usa para decidir si insertar datos de ejemplo
    private static boolean tableIsEmpty(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + tableName;
        try (var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cnt") == 0;
            }
        } catch (SQLException ex) {
            System.err.println("No se pudo verificar si la tabla '" + tableName + "' está vacía: " + ex.getMessage());
            ex.printStackTrace();
            return true;
        }
        return true;
    }
}
