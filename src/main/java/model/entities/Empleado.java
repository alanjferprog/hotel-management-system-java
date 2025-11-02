package model.entities;

import java.util.List;
import java.util.ArrayList;


public class Empleado extends Persona {
    private int idEmpleado;
    private String cargo;
    private List<Empleado> empleadosACargo = new ArrayList<>();


    public Empleado() {}


    public Empleado(String nombre, String apellido, String dni, int idEmpleado, String cargo) {
        super(nombre, apellido, dni);
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
    }


    public int getIdEmpleado() { return idEmpleado; }
    public String getCargo() { return cargo; }
    public void registarCheckIn(Reserva r);
    public void registarCheckOut(Reserva r);
    public void atenderRoomService(RoomService r);
    public void fichar();


    @Override
    public String toString() {
        return "Empleado #" + idEmpleado + " - " + super.toString();
    }
}
