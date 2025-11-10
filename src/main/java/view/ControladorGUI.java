package view;

import model.core.*;
import model.entities.*;
import model.exceptions.*;

import java.time.LocalDate;
import java.util.Optional;

// Nuevos imports para BD
import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ControladorGUI {
    private Hotel hotel;

    public ControladorGUI(Hotel hotel) {
        this.hotel = hotel;
    }

    public Hotel getHotel() { return hotel; }

    public Reserva crearReserva(LocalDate inicio, LocalDate fin, int numeroHab, Huesped huesped, Empleado empleado)
            throws HabitacionNoDisponibleException, ReservaInvalidaException {
        return hotel.crearReserva(inicio, fin, numeroHab, huesped, empleado);
    }

    public void confirmarReserva(int idReserva) throws ReservaInvalidaException {
        hotel.confirmarReserva(idReserva);
    }

    /** Busca una reserva por su id en memoria. */
    public Optional<Reserva> buscarReservaPorId(int id) {
        return hotel.getReservas().stream().filter(r -> r.getIdReserva() == id).findFirst();
    }

    /**
     * Carga habitaciones desde la base de datos (SQLite) y las poblá en el Hotel en memoria.
     * Retorna la cantidad de habitaciones cargadas.
     */
    public int cargarHabitacionesDesdeDB() throws SQLException {
        try (Connection conn = ConexionSQLite.conectar()) {
            List<Habitacion> hs = HabitacionDAO.findAll(conn);
            // Reemplazar la lista en memoria
            hotel.getHabitaciones().clear();
            for (Habitacion h : hs) hotel.agregarHabitacion(h);
            return hs.size();
        }
    }

    /**
     * Carga reservas desde la BD y las poblá en el Hotel en memoria.
     */
    public int cargarReservasDesdeDB() throws SQLException {
        try (Connection conn = ConexionSQLite.conectar()) {
            // Necesitamos un DAO lector de reservas. Implementaré uno simple aquí si no existe.
            List<Reserva> reservas = new java.util.ArrayList<>();
            String sql = "SELECT id, fechaInicio, fechaFin, numeroHab, nombre, apellido, dni, email, telefono, estado FROM reserva ORDER BY id";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    java.time.LocalDate inicio = java.time.LocalDate.parse(rs.getString("fechaInicio"));
                    java.time.LocalDate fin = java.time.LocalDate.parse(rs.getString("fechaFin"));
                    int numeroHab = rs.getInt("numeroHab");
                    String nombre = rs.getString("nombre");
                    String apellido = rs.getString("apellido");
                    String dni = rs.getString("dni");
                    String email = rs.getString("email");
                    String telefono = rs.getString("telefono");
                    String estado = rs.getString("estado");

                    // Buscar habitacion en memoria (debe estar cargada previamente)
                    java.util.Optional<Habitacion> habOpt = hotel.buscarHabitacionPorNumero(numeroHab);
                    model.entities.Habitacion hab;
                    if (!habOpt.isPresent()) {
                        // Si la habitación no existe en memoria, crear una provisional para poder mostrar la reserva
                        hab = new model.entities.Habitacion(numeroHab, "Desconocida", 0.0);
                        // No persistimos automáticamente la habitación; la dejamos en memoria para mostrar la fila
                        hotel.agregarHabitacion(hab);
                    } else {
                        hab = habOpt.get();
                    }

                    Huesped h = new Huesped(nombre == null ? "" : nombre, apellido == null ? "" : apellido, dni == null ? "" : dni, email == null ? "" : email, telefono == null ? "" : telefono);
                    Reserva r = new Reserva(id, inicio, fin, hab, h, new Empleado(1, "DB", "Init", "00000000", "System", "All Day"));
                    // establecer estado si viene
                    if (estado != null) {
                        try {
                            java.lang.reflect.Field f = Reserva.class.getDeclaredField("estado");
                            f.setAccessible(true);
                            f.set(r, estado);
                        } catch (Exception ex) {
                            // ignorar
                        }
                    }
                    reservas.add(r);
                }
            }
            // Reemplazar reservas en memoria
            hotel.getReservas().clear();
            for (Reserva rr : reservas) {
                hotel.getReservas().add(rr);
                rr.getHuesped().addReserva(rr);
            }
            return reservas.size();
        }
    }

    /**
     * Persiste una reserva en la BD (INSERT OR REPLACE).
     */
    public void guardarReservaEnDB(Reserva r) throws SQLException {
        try (Connection conn = ConexionSQLite.conectar()) {
            dao.ReservaDAO.insertReserva(conn, r);
        }
    }

    /** Actualiza el estado de una habitación en la BD (por número) */
    public void actualizarEstadoHabitacionEnDB(int numero, String estado) throws SQLException {
        try (Connection conn = ConexionSQLite.conectar()) {
            dao.HabitacionDAO.updateEstado(conn, numero, estado);
        }
    }

    /** Carga los empleados desde la BD y devuelve la lista de Empelados */

    public java.util.List<model.entities.Empleado> cargarEmpleadoDesdeBD() throws java.sql.SQLException {
        try(java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            return dao.EmpleadoDAO.findAll(conn);
        }
    }

    public void insertarEmpleadoEnDB(model.entities.Empleado e) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.EmpleadoDAO.insert(conn, e);
        }
    }
    
    public void actualizarEmpleadoEnDB(model.entities.Empleado e) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.EmpleadoDAO.updateByDni(conn, e);
        }
    }

    public void eliminarEmpleadoEnDB(String dni) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.EmpleadoDAO.deleteByDni(conn, dni);
        }
    }

    /** Actualiza solamente el estado de un empleado tanto en BD como en memoria */
    public void actualizarEstadoEmpleadoEnDB(String dni, String estado) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.EmpleadoDAO.updateEstadoByDni(conn, dni, estado);
        }
        // actualizar en memoria si existe
        for (model.entities.Empleado e : hotel.getEmpleados()) {
            if (e.getDni() != null && e.getDni().equals(dni)) {
                try {
                    java.lang.reflect.Field f = e.getClass().getDeclaredField("estado");
                    f.setAccessible(true);
                    f.set(e, model.entities.EstadoEmpleado.fromString(estado));
                } catch (Exception ex) {
                    // si falla la reflexión, ignoramos (se actualizará cuando se recargue desde BD)
                }
                break;
            }
        }
    }


    /**
     * Carga huespedes desde la BD y devuelve la lista de Huesped
     */
    public java.util.List<model.entities.Huesped> cargarHuespedesDesdeDB() throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            return dao.HuespedDAO.findAll(conn);
        }
    }

    public void insertarHuespedEnDB(model.entities.Huesped h) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.HuespedDAO.insert(conn, h);
        }
    }

    public void actualizarHuespedEnDB(model.entities.Huesped h) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.HuespedDAO.updateByDni(conn, h);
        }
    }

    public void eliminarHuespedEnDB(String dni) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.HuespedDAO.deleteByDni(conn, dni);
        }
    }

    /** Asigna un empleado (dni) a una habitación: persiste la asignación y marca al empleado como ocupado. */
    public void asignarEmpleadoAHabitacion(int numero, String dni) throws java.sql.SQLException {
        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            // intentar actualizar columna empleadoAsignado (si existe)
            try {
                dao.HabitacionDAO.updateEmpleadoAsignado(conn, numero, dni);
            } catch (Exception ex) {
                // ignorar si la columna no existe
            }
            // marcar empleado como ocupado
            dao.EmpleadoDAO.updateEstadoByDni(conn, dni, "ocupado");
        }
        // actualizar memoria
        hotel.buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEmpleadoAsignado(dni));
        for (model.entities.Empleado e : hotel.getEmpleados()) {
            if (e.getDni() != null && e.getDni().equals(dni)) {
                try {
                    java.lang.reflect.Field f = e.getClass().getDeclaredField("estado");
                    f.setAccessible(true);
                    f.set(e, model.entities.EstadoEmpleado.fromString("ocupado"));
                } catch (Exception ex) { /* ignore */ }
                break;
            }
        }
    }

    /** Libera el empleado asignado a una habitación: marca al empleado como disponible y limpia la asignación. */
    public void liberarEmpleadoPorHabitacion(int numero) throws java.sql.SQLException {
        var habOpt = hotel.buscarHabitacionPorNumero(numero);
        if (habOpt.isEmpty()) return;
        var hab = habOpt.get();
        String dni = hab.getEmpleadoAsignado();
        if (dni == null || dni.isBlank()) return;

        try (java.sql.Connection conn = bdd.ConexionSQLite.conectar()) {
            dao.EmpleadoDAO.updateEstadoByDni(conn, dni, "disponible");
            try {
                dao.HabitacionDAO.updateEmpleadoAsignado(conn, numero, null);
            } catch (Exception ex) { /* columna puede no existir */ }
        }

        // actualizar en memoria
        hab.setEmpleadoAsignado(null);
        for (model.entities.Empleado e : hotel.getEmpleados()) {
            if (e.getDni() != null && e.getDni().equals(dni)) {
                try {
                    java.lang.reflect.Field f = e.getClass().getDeclaredField("estado");
                    f.setAccessible(true);
                    f.set(e, model.entities.EstadoEmpleado.fromString("disponible"));
                } catch (Exception ex) { /* ignore */ }
                break;
            }
        }
    }


    /**
     * Devuelve true si el empleado está actualmente dentro de su turno de trabajo.
     * Turnos soportados (basado en la cadena `turno` del empleado, insensible a mayúsculas y acentos):
     * - Mañana: 06:00 - 13:59
     * - Tarde: 14:00 - 21:59
     * - Noche: 22:00 - 05:59
     * Si el turno es nulo o no reconoce, retorna true (se considera disponible por turno).
     */
    public boolean estaEmpleadoEnTurno(model.entities.Empleado emp) {
        if (emp == null) return false;
        String turno = emp.getTurno();
        if (turno == null) return true;
        // Normalizar: quitar acentos y pasar a minúsculas
        String t = java.text.Normalizer.normalize(turno, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime mañanaInicio = java.time.LocalTime.of(6,0);
        java.time.LocalTime tardeInicio = java.time.LocalTime.of(14,0);
        java.time.LocalTime nocheInicio = java.time.LocalTime.of(22,0);
        // Mañana
        if (t.contains("manana") || t.contains("ma") || t.contains("mañ" ) || t.contains("man")) {
            return !now.isBefore(mañanaInicio) && now.isBefore(tardeInicio);
        }
        // Tarde
        if (t.contains("tarde") || t.contains("tar")) {
            return !now.isBefore(tardeInicio) && now.isBefore(nocheInicio);
        }
        // Noche
        if (t.contains("noche") || t.contains("noc")) {
            // noche cubre desde 22:00 hasta 06:00 (pasando medianoche)
            if (!now.isBefore(nocheInicio)) return true; // >=22:00
            return now.isBefore(mañanaInicio); // <06:00
        }
        // Si no reconoce el texto del turno, asumimos que está en turno
        return true;
    }
}
