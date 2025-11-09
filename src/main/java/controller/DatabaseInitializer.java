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

            String sqlCreateEmp = """
                    CREATE TABLE IF NOT EXISTS empleado (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    apellido TEXT,
                    dni TEXT,
                    cargo TEXT,
                    turno TEXT
                    );
            """;
            stmt.execute(sqlCreateEmp);

            // 4) Insertar datos de ejemplo solo si la tabla está vacía
            try {
                if (tableIsEmpty(conn, "habitacion")) {
                    HabitacionDAO.insertSampleData(conn);
                    System.out.println("Datos de ejemplo (habitaciones) insertados en la BD.");
                } else {
                    System.out.println("Tabla 'habitacion' ya tiene datos, no insertar ejemplos.");
                }
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (habitaciones): " + ex.getMessage());
            }

            try {
                if (tableIsEmpty(conn, "reserva")) {
                    ReservaDAO.insertSampleData(conn);
                    System.out.println("Datos de ejemplo (reservas) insertados en la BD.");
                } else {
                    System.out.println("Tabla 'reserva' ya tiene datos, no insertar ejemplos.");
                }
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (reservas): " + ex.getMessage());
            }

            try {
                if (tableIsEmpty(conn, "empleado")) {
                    EmpleadoDAO.insertSampleData(conn);
                    System.out.println("Datos de ejemplo (empleados) insertados en la BD.");
                } else {
                    System.out.println("Tabla 'empleado' ya tiene datos, no insertar ejemplos.");
                }
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (empleados): " + ex.getMessage());
            }

            try {
                if (tableIsEmpty(conn, "cliente")) {
                    HuespedDAO.insertSampleData(conn);
                    System.out.println("Datos de ejemplo (huespedes) insertados en la BD.");
                } else {
                    System.out.println("Tabla 'cliente' ya tiene datos, no insertar ejemplos.");
                }
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (huespedes): " + ex.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error inicializando la BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean tableIsEmpty(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + tableName;
        try (var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cnt") == 0;
            }
        } catch (SQLException ex) {
            // si hay error (por ejemplo tabla no existe), consideramos que está vacía y dejamos que el caller cree y/o inserte
            System.err.println("No se pudo verificar si la tabla " + tableName + " está vacía: " + ex.getMessage());
            return true;
        }
        return true;
    }
}
