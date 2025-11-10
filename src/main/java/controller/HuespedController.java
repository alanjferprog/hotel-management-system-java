package controller;

import controller.HotelController;
import model.entities.Huesped;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class HuespedController {
    public HuespedController() {}

    public void initializeAndLoad(HotelController controlador) throws Exception {
        DatabaseInitializer.initialize();
        try {
            // cargar y no mantener en memoria si el Hotel no tiene lista; pero recargar BD
            controlador.cargarHuespedesDesdeDB();
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public List<Object[]> getHuespedRows(HotelController controlador) {
        List<Object[]> rows = new ArrayList<>();
        try {
            List<Huesped> huespedes = controlador.cargarHuespedesDesdeDB();
            for (Huesped h : huespedes) {
                rows.add(new Object[]{h.getNombre(), h.getApellido(), h.getDni(), h.getEmail(), h.getTelefono()});
            }
        } catch (Exception ex) {
            // ignore
        }
        return rows;
    }

    public void insertHuesped(HotelController controlador, Huesped h) throws SQLException {
        controlador.insertarHuespedEnDB(h);
    }

    public void updateHuesped(HotelController controlador, Huesped h) throws SQLException {
        controlador.actualizarHuespedEnDB(h);
    }

    public void deleteHuesped(HotelController controlador, String dni) throws SQLException {
        controlador.eliminarHuespedEnDB(dni);
    }
}
