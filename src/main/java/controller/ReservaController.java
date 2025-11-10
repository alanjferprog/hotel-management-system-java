package controller;

import view.ControladorGUI;
import model.entities.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import bdd.ConexionSQLite;

/**
 * Clase que encapsula la lógica para cargar y buscar reservas en BD o memoria.
 */
public class ReservaController {

    public ReservaController() {}

    /**
     * Inicializa la BD y carga habitaciones y reservas en memoria usando el ControladorGUI.
     */
    public void initializeAndLoad(ControladorGUI controlador) throws Exception {
        // Inicializar estructura de BD
        DatabaseInitializer.initialize();
        controlador.cargarHabitacionesDesdeDB();
        controlador.cargarReservasDesdeDB();
    }

    /**
     * Retorna una lista de filas (Object[]) para poblar la tabla con reservas.
     */
    public List<Object[]> getReservationRows(ControladorGUI controlador) {
        List<Object[]> rows = new ArrayList<>();
        try {
            if (!controlador.getHotel().getReservas().isEmpty()) {
                for (Reserva r : controlador.getHotel().getReservas()) {
                    rows.add(new Object[]{r.getIdReserva(), r.getHuesped().getNombre() + " " + r.getHuesped().getApellido(), r.getHabitacion().getNumero(), r.getFechaInicio(), r.getFechaFin(), r.getEstado()});
                }
                return rows;
            }
        } catch (Exception ex) {
            // seguir a fallback
        }

        // Fallback: leer directamente desde la BD
        try (Connection conn = ConexionSQLite.conectar()) {
            String sql = "SELECT id, nombre, apellido, numeroHab, fechaInicio, fechaFin, estado FROM reserva ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    java.time.LocalDate inicio = java.time.LocalDate.parse(rs.getString("fechaInicio"));
                    java.time.LocalDate fin = java.time.LocalDate.parse(rs.getString("fechaFin"));
                    int numeroHab = rs.getInt("numeroHab");
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    String estado = rs.getString("estado");
                    rows.add(new Object[]{id, (nombre==null?"":nombre) + " " + (apellido==null?"":apellido), numeroHab, inicio, fin, estado});
                }
            }
        } catch (Exception ex) {
            // si falla, retornar lista posiblemente vacía
        }
        return rows;
    }

    /**
     * Busca una reserva por id y retorna Optional<Reserva> (primero memoria, luego BD).
     */
    public Optional<Reserva> findReservaById(int id, ControladorGUI controlador) {
        try {
            Optional<Reserva> opt = controlador.buscarReservaPorId(id);
            if (opt.isPresent()) return opt;
        } catch (Exception ex) {
            // fallback
        }

        // Fallback a BD: construir un objeto Reserva mínimo si es posible (necesita habitacion y huesped)
        try (Connection conn = ConexionSQLite.conectar()) {
            String sql = "SELECT id, fechaInicio, fechaFin, numeroHab, nombre, apellido, dni, email, telefono, estado FROM reserva WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int rid = rs.getInt("id");
                        java.time.LocalDate inicio = java.time.LocalDate.parse(rs.getString("fechaInicio"));
                        java.time.LocalDate fin = java.time.LocalDate.parse(rs.getString("fechaFin"));
                        int numeroHab = rs.getInt("numeroHab");
                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        String dni = rs.getString("dni");
                        String email = rs.getString("email");
                        String tel = rs.getString("telefono");
                        String estado = rs.getString("estado");

                        // Buscar habitacion en memoria
                        var habOpt = controlador.getHotel().buscarHabitacionPorNumero(numeroHab);
                        if (habOpt.isEmpty()) return Optional.empty();
                        var hab = habOpt.get();
                        model.entities.Huesped hues = new model.entities.Huesped(nombre==null?"":nombre, apellido==null?"":apellido, dni==null?"":dni, email==null?"":email, tel==null?"":tel);
                        model.entities.Empleado emp = controlador.getHotel().getEmpleados().isEmpty() ? new model.entities.Empleado(0, "DB", "Init", "00000000", "System", "All Day") : controlador.getHotel().getEmpleados().get(0);
                        Reserva r = new Reserva(rid, inicio, fin, hab, hues, emp);
                        // intentar setear estado si existe
                        try {
                            java.lang.reflect.Field f = Reserva.class.getDeclaredField("estado");
                            f.setAccessible(true);
                            f.set(r, estado);
                        } catch (Exception ignore) {}
                        return Optional.of(r);
                    }
                }
            }
        } catch (Exception ex) {
            // ignore
        }
        return Optional.empty();
    }

    /**
     * Busca una reserva por id y retorna una fila preparada (Object[]) para la tabla.
     */
    public Optional<Object[]> findReservationRowById(int id, ControladorGUI controlador) {
        try {
            Optional<Reserva> opt = findReservaById(id, controlador);
            if (opt.isPresent()) {
                Reserva r = opt.get();
                return Optional.of(new Object[]{r.getIdReserva(), r.getHuesped().getNombre() + " " + r.getHuesped().getApellido(), r.getHabitacion().getNumero(), r.getFechaInicio(), r.getFechaFin(), r.getEstado()});
            }
        } catch (Exception ex) {
            // fallback
        }
        return Optional.empty();
    }
}
