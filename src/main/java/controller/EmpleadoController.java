package controller;

import view.ControladorGUI;
import model.entities.Empleado;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

/** Controller para operaciones relacionadas con empleados desde la capa de vista. */
public class EmpleadoController {

    public EmpleadoController() {}

    /** Inicializa la BD y carga empleados en memoria (delegando a DatabaseInitializer y ControladorGUI). */
    public void initializeAndLoad(ControladorGUI controlador) throws Exception {
        DatabaseInitializer.initialize();
        // cargar empleados desde BD a memoria
        try {
            var empleados = controlador.cargarEmpleadoDesdeBD();
            controlador.getHotel().getEmpleados().clear();
            for (model.entities.Empleado e : empleados) controlador.getHotel().getEmpleados().add(e);
        } catch (SQLException ex) {
            throw ex;
        }
    }

    /** Retorna filas listas para la tabla en la vista (Id, Nombre, Apellido, DNI, Cargo, Turno, EstadoLabel, EnTurno). */
    public List<Object[]> getEmpleadoRows(ControladorGUI controlador) {
        List<Object[]> rows = new ArrayList<>();
        for (Empleado e : controlador.getHotel().getEmpleados()) {
            String estadoLabel = e.getEstado() == null ? "" : e.getEstado().getLabel();
            boolean enTurno = controlador.estaEmpleadoEnTurno(e);
            rows.add(new Object[]{e.getIdEmpleado(), e.getNombre(), e.getApellido(), e.getDni(), e.getCargo(), e.getTurno(), estadoLabel, enTurno ? "Sí" : "No"});
        }
        return rows;
    }

    // Operaciones CRUD expuestas para las vistas: delegan al ControladorGUI y mantienen validaciones simples
    public boolean existsByDni(ControladorGUI controlador, String dni) throws SQLException {
        List<Empleado> actuales = controlador.cargarEmpleadoDesdeBD();
        return actuales.stream().anyMatch(e -> e.getDni() != null && e.getDni().equalsIgnoreCase(dni));
    }

    public void insertEmpleado(ControladorGUI controlador, Empleado nuevo) throws SQLException {
        controlador.insertarEmpleadoEnDB(nuevo);
        // refrescar memoria
        var empleados = controlador.cargarEmpleadoDesdeBD();
        controlador.getHotel().getEmpleados().clear();
        for (var emp : empleados) controlador.getHotel().getEmpleados().add(emp);
    }

    public void updateEmpleado(ControladorGUI controlador, Empleado actualizado) throws SQLException {
        controlador.actualizarEmpleadoEnDB(actualizado);
        var empleados = controlador.cargarEmpleadoDesdeBD();
        controlador.getHotel().getEmpleados().clear();
        for (var emp : empleados) controlador.getHotel().getEmpleados().add(emp);
    }

    public void deleteEmpleado(ControladorGUI controlador, String dni) throws SQLException {
        controlador.eliminarEmpleadoEnDB(dni);
        var empleados = controlador.cargarEmpleadoDesdeBD();
        controlador.getHotel().getEmpleados().clear();
        for (var emp : empleados) controlador.getHotel().getEmpleados().add(emp);
    }
}
