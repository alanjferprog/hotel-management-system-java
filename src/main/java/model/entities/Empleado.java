package model.entities;

import java.util.ArrayList;
import java.util.List;

public class Empleado extends Persona {
    private int idEmpleado;
    private String cargo;
    private String turno;
    private List<String> autorizaciones;
    private EstadoEmpleado estado;

    public Empleado(int idEmpleado, String nombre, String apellido, String dni, String cargo, String turno) {
        super(nombre, apellido, dni);
        this.idEmpleado = idEmpleado;
        this.cargo = cargo;
        this.turno = turno;
        this.autorizaciones = new ArrayList<>();
        this.estado = EstadoEmpleado.DISPONIBLE;
    }

    public int getIdEmpleado() { return idEmpleado; }
    public String getCargo() { return cargo; }
    public String getTurno() { return turno; }
    public void agregarAutorizacion(String auth) { autorizaciones.add(auth); }
    public boolean estaAutorizado(String auth) { return autorizaciones.contains(auth); }

    public EstadoEmpleado getEstado() { return estado; }
    public void setEstado(EstadoEmpleado estado) { this.estado = estado; }
    public void setEstado(String estadoStr) { this.estado = EstadoEmpleado.fromString(estadoStr); }

    public void setIdEmpleado(int idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public void setAutorizaciones(List<String> autorizaciones) {
        this.autorizaciones = autorizaciones;
    }

    @Override
    public String toString() {
        return "Empleado #" + idEmpleado + " " + super.toString() + " - " + cargo + " - " + turno + " (" + (estado == null ? "" : estado.getLabel()) + ")";
    }
}
