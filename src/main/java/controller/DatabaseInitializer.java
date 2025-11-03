package controller;

import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import dao.ReservaDAO;
import dao.HuespedDAO;
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

            // 4) Insertar datos de ejemplo siempre (INSERT OR IGNORE dentro de los DAOs evitará duplicados)
            try {
                HabitacionDAO.insertSampleData(conn);
                System.out.println("Datos de ejemplo (habitaciones) insertados/actualizados en la BD.");
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (habitaciones): " + ex.getMessage());
            }

            try {
                ReservaDAO.insertSampleData(conn);
                System.out.println("Datos de ejemplo (reservas) insertados/actualizados en la BD.");
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (reservas): " + ex.getMessage());
            }

            try {
                HuespedDAO.insertSampleData(conn);
                System.out.println("Datos de ejemplo (huespedes) insertados/actualizados en la BD.");
            } catch (SQLException ex) {
                System.err.println("No se pudieron insertar datos de ejemplo (huespedes): " + ex.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error inicializando la BD: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
