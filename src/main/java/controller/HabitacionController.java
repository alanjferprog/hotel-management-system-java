package controller;

import view.ControladorGUI;
import model.entities.Habitacion;
import model.entities.EstadoHabitacion;
import model.entities.Empleado;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller que encapsula operaciones de habitaciones para la capa de vista.
 */
public class HabitacionController {

    public HabitacionController() {}

    /** Devuelve filas listas para la tabla de habitaciones. */
    public List<Object[]> getHabitacionRows(ControladorGUI controlador) {
        List<Object[]> rows = new ArrayList<>();
        List<Habitacion> habitaciones = controlador.getHotel().getHabitaciones();
        for (Habitacion h : habitaciones) {
            EstadoHabitacion estado = h.getEstado();
            String accion = "Reservar";
            if (EstadoHabitacion.PENDIENTE_LIMPIEZA.equals(estado)) {
                accion = "Pedir limpieza";
            } else if (EstadoHabitacion.LIMPIEZA_PEDIDA.equals(estado)) {
                accion = "Limpieza pedida";
            } else if (EstadoHabitacion.EN_REPARACION.equals(estado)) {
                accion = "No disponible";
            } else if (!EstadoHabitacion.DISPONIBLE.equals(estado)) {
                accion = "No disponible";
            }
            String alta = (EstadoHabitacion.DISPONIBLE.equals(estado)) ? "" : "Dar de alta";

            // buscar nombre del empleado asignado
            String empleadoNombre = "";
            String dniAsignado = h.getEmpleadoAsignado();
            if (dniAsignado != null && !dniAsignado.isBlank()) {
                for (Empleado emp : controlador.getHotel().getEmpleados()) {
                    if (emp.getDni() != null && emp.getDni().equals(dniAsignado)) {
                        empleadoNombre = emp.getNombre() + " " + emp.getApellido();
                        break;
                    }
                }
            }
            rows.add(new Object[] { h.getNumero(), h.getTipo(), h.getPrecioPorNoche(), estado.getLabel(), empleadoNombre, accion, alta });
        }
        return rows;
    }

    /** Retorna lista de empleados filtrados por cargo y por si están en turno. */
    public List<Empleado> listarEmpleadosPorCargoYTurno(ControladorGUI controlador, String cargoContains) {
        List<Empleado> result = new ArrayList<>();
        for (Empleado emp : controlador.getHotel().getEmpleados()) {
            if (emp.getCargo() != null && emp.getCargo().toLowerCase().contains(cargoContains.toLowerCase()) && controlador.estaEmpleadoEnTurno(emp)) {
                result.add(emp);
            }
        }
        return result;
    }

    /** Asigna un empleado (dni) a una habitación y marca empleado ocupado (persistencia via ControladorGUI). */
    public void asignarEmpleado(ControladorGUI controlador, int numero, String dni) throws SQLException {
        controlador.asignarEmpleadoAHabitacion(numero, dni);
    }

    /** Marcar habitación como en_reparacion y persistir. */
    public void darDeBaja(ControladorGUI controlador, int numero) throws SQLException {
        controlador.getHotel().buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEstado(EstadoHabitacion.EN_REPARACION));
        controlador.actualizarEstadoHabitacionEnDB(numero, EstadoHabitacion.EN_REPARACION.getDbValue());
    }

    /** Marcar habitación como disponible y liberar empleado asignado. */
    public void darDeAlta(ControladorGUI controlador, int numero) throws SQLException {
        controlador.getHotel().buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEstado(EstadoHabitacion.DISPONIBLE));
        controlador.actualizarEstadoHabitacionEnDB(numero, EstadoHabitacion.DISPONIBLE.getDbValue());
        controlador.liberarEmpleadoPorHabitacion(numero);
    }

    /** Reservar: delega a ControladorGUI.crearReserva y persistir via guardarReservaEnDB */
    public model.entities.Reserva crearYGuardarReserva(ControladorGUI controlador, java.time.LocalDate inicio, java.time.LocalDate fin, int numeroHab, model.entities.Huesped huesped, model.entities.Empleado empleado) throws Exception {
        model.entities.Reserva r = controlador.crearReserva(inicio, fin, numeroHab, huesped, empleado);
        controlador.guardarReservaEnDB(r);
        return r;
    }
}

