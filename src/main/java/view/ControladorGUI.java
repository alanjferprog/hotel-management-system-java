package view;

import model.core.*;
import model.entities.*;
import model.exceptions.*;

import java.time.LocalDate;
import java.util.Optional;

// Nuevos imports para BD
import bdd.ConexionSQLite;
import dao.HabitacionDAO;
import dao.ReservaDAO; // nota: existe ReservaDAO (insert), pero necesitaremos un reader - crearé uno si hace falta
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
                    if (!habOpt.isPresent()) continue; // ignorar si no existe
                    Habitacion hab = habOpt.get();

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
}
