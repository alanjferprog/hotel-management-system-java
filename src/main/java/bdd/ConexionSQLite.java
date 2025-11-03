package bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConexionSQLite {
    private static final String DB_DIR = "data";
    private static final String DB_FILE = "hotel.db";
    private static final String URL = "jdbc:sqlite:" + DB_DIR + "/" + DB_FILE;

    static {
        // Crear carpeta data si no existe
        try {
            Path dir = Paths.get(DB_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo crear carpeta data: " + ex.getMessage());
        }
    }

    public static Connection conectar() throws SQLException {
        try {
            // Intentar cargar explícitamente el driver SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver org.sqlite.JDBC no encontrado en classpath: " + e.getMessage());
        }
        // Mostrar URL para diagnóstico
        System.out.println("Conectando a BD: " + URL);
        return DriverManager.getConnection(URL);
    }
}
