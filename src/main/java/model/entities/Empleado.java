package model.entities;

import java.util.ArrayList;
import java.util.List;

public class Empleado extends Persona {
    private int idEmpleado;
    private String cargo;
    private List<String> autorizaciones;

    public Empleado(int idEmpleado, String nombre, String apellido, String dni, String cargo) {
        super(nombre, apellido, dni);
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.autorizaciones = new ArrayList<>();
    }

    public int getIdEmpleado() { return idEmpleado; }
    public String getCargo() { return cargo; }
    public void agregarAutorizacion(String auth) { autorizaciones.add(auth); }
    public boolean estaAutorizado(String auth) { return autorizaciones.contains(auth); }

    @Override
    public String toString() {
        return "Empleado #" + idEmpleado + " " + super.toString() + " - " + cargo;
    }
}
