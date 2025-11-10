package controller;

import controller.HotelController;
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
    public void initializeAndLoad(HotelController controlador) throws Exception {
        // Inicializar estructura de BD
        DatabaseInitializer.initialize();
        controlador.cargarHabitacionesDesdeDB();
        controlador.cargarReservasDesdeDB();
    }

    /**
     * Retorna una lista de filas (Object[]) para poblar la tabla con reservas.
     * Ahora priorizamos la BD como fuente de verdad: si la consulta a la BD funciona, devolvemos esas filas.
     * Si falla, usamos la memoria solo como fallback. Además procesamos fila a fila para evitar que
     * una fila con datos corruptos (fecha inválida) haga fallar toda la lectura.
     */
    public List<Object[]> getReservationRows(HotelController controlador) {
        List<Object[]> rows = new ArrayList<>();

        // Intentar leer desde BD primero (fuente de verdad)
        try (Connection conn = ConexionSQLite.conectar()) {
            String sql = "SELECT id, nombre, apellido, numeroHab, fechaInicio, fechaFin, estado FROM reserva ORDER BY id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        int id = rs.getInt("id");
                        String inicioS = rs.getString("fechaInicio");
                        String finS = rs.getString("fechaFin");
                        java.time.LocalDate inicio = null;
                        java.time.LocalDate fin = null;
                        try {
                            if (inicioS != null && !inicioS.isBlank()) inicio = java.time.LocalDate.parse(inicioS);
                        } catch (Exception pe) {
                            System.err.println("Fila reserva id="+id+": fechaInicio inválida ('"+inicioS+"'), se omitirá la fecha: " + pe.getMessage());
                        }
                        try {
                            if (finS != null && !finS.isBlank()) fin = java.time.LocalDate.parse(finS);
                        } catch (Exception pe) {
                            System.err.println("Fila reserva id="+id+": fechaFin inválida ('"+finS+"'), se omitirá la fecha: " + pe.getMessage());
                        }
                        int numeroHab = rs.getInt("numeroHab");
                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        String estado = rs.getString("estado");
                        rows.add(new Object[]{id, (nombre==null?"":nombre) + " " + (apellido==null?"":apellido), numeroHab, inicio, fin, estado});
                    } catch (Exception rowEx) {
                        // Si falla una fila concreta, continuar con la siguiente
                        System.err.println("Error procesando fila de reserva desde BD: " + rowEx.getMessage());
                    }
                }
            }
            if (!rows.isEmpty()) return rows;
        } catch (Exception ex) {
            // si la BD falla, seguiremos al fallback (memoria)
            System.err.println("No se pudo leer reservas desde BD: " + ex.getMessage());
        }

        // Fallback: usar las reservas en memoria
        try {
            if (controlador.getHotel().getReservas() != null && !controlador.getHotel().getReservas().isEmpty()) {
                for (Reserva r : controlador.getHotel().getReservas()) {
                    rows.add(new Object[]{r.getIdReserva(), r.getHuesped().getNombre() + " " + r.getHuesped().getApellido(), r.getHabitacion().getNumero(), r.getFechaInicio(), r.getFechaFin(), r.getEstado()});
                }
            }
        } catch (Exception ex) {
            // si todo falla, retornamos lista vacía
            System.err.println("No se pudo obtener reservas desde memoria: " + ex.getMessage());
        }
        return rows;
    }

    /**
     * Busca una reserva por id y retorna Optional<Reserva> (primero memoria, luego BD).
     */
    public Optional<Reserva> findReservaById(int id, HotelController controlador) {
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
                        String inicioS = rs.getString("fechaInicio");
                        String finS = rs.getString("fechaFin");
                        java.time.LocalDate inicio = null;
                        java.time.LocalDate fin = null;
                        try { if (inicioS != null && !inicioS.isBlank()) inicio = java.time.LocalDate.parse(inicioS); } catch (Exception pe) { /* ignore */ }
                        try { if (finS != null && !finS.isBlank()) fin = java.time.LocalDate.parse(finS); } catch (Exception pe) { /* ignore */ }

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
                        if (estado != null) {
                            try {
                                java.lang.reflect.Field f = Reserva.class.getDeclaredField("estado");
                                f.setAccessible(true);
                                f.set(r, estado);
                            } catch (Exception ignore) {}
                        }
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
    public Optional<Object[]> findReservationRowById(int id, HotelController controlador) {
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
