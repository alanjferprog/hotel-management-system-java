package model.core;

import model.entities.*;
import model.exceptions.HabitacionNoDisponibleException;
import model.exceptions.ReservaInvalidaException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class Hotel {
    private String nombre;
    private List<Habitacion> habitaciones;
    private List<Reserva> reservas;
    private List<Empleado> empleados;

    public Hotel(String nombre) {
        this.nombre = nombre;
        this.habitaciones = new ArrayList<>();
        this.reservas = new ArrayList<>();
        this.empleados = new ArrayList<>();
    }

    public String getNombre() { return nombre; }
    public List<Habitacion> getHabitaciones() { return habitaciones; }
    public List<Reserva> getReservas() { return reservas; }
    public List<Empleado> getEmpleados() { return empleados; }

    public void agregarHabitacion(Habitacion h) { habitaciones.add(h); }
    public void agregarEmpleado(Empleado e) { empleados.add(e); }

    public Optional<Habitacion> buscarHabitacionPorNumero(int numero) {
        return habitaciones.stream().filter(h -> h.getNumero() == numero).findFirst();
    }

    public List<Habitacion> listarHabitacionesDisponibles() {
        List<Habitacion> disponibles = new ArrayList<>();
        for (Habitacion h : habitaciones) {
            if (h.estaDisponible()) disponibles.add(h);
        }
        return disponibles;
    }

    public Reserva crearReserva(LocalDate inicio, LocalDate fin, int numeroHab, Huesped huesped, Empleado empleado)
            throws HabitacionNoDisponibleException, ReservaInvalidaException {
        if (inicio == null || fin == null || inicio.isAfter(fin)) {
            throw new ReservaInvalidaException("Fechas inválidas");
        }
        Habitacion hab = buscarHabitacionPorNumero(numeroHab)
                .orElseThrow(() -> new HabitacionNoDisponibleException("No existe la habitación " + numeroHab));
        if (!hab.estaDisponible()) throw new HabitacionNoDisponibleException("Habitación no disponible: " + numeroHab);

        Reserva r = new Reserva(inicio, fin, hab, huesped, empleado);
        reservas.add(r);
        huesped.addReserva(r);
        return r;
    }

    public void confirmarReserva(int idReserva) throws ReservaInvalidaException {
        Reserva r = reservas.stream().filter(x -> x.getIdReserva() == idReserva).findFirst()
                .orElseThrow(() -> new ReservaInvalidaException("Reserva no encontrada"));
        r.confirmar();
    }

    /**
     * Carga habitaciones desde un archivo CSV. Formato por línea:
     * numero,tipo,precio[,estado]
     * Las líneas vacías o que no tengan al menos 3 campos serán ignoradas.
     * Devuelve la cantidad de habitaciones cargadas.
     */
    public int cargarHabitacionesDesdeCSV(String rutaRelativa) throws IOException {
        Path ruta = Paths.get(rutaRelativa);
        if (!Files.exists(ruta)) {
            throw new IOException("Archivo no encontrado: " + ruta.toAbsolutePath());
        }
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        int cargadas = 0;
        for (String linea : lineas) {
            if (linea == null) continue;
            String l = linea.trim();
            if (l.isEmpty() || l.startsWith("#")) continue; // permite comentarios
            String[] parts = l.split(",");
            if (parts.length < 3) continue; // línea inválida
            try {
                int numero = Integer.parseInt(parts[0].trim());
                String tipo = parts[1].trim();
                double precio = Double.parseDouble(parts[2].trim());
                Habitacion h = new Habitacion(numero, tipo, precio);
                // si hay cuarto campo, actualizar estado
                if (parts.length >= 4) {
                    String estado = parts[3].trim();
                    h.setEstado(estado);
                }
                // evitar duplicados por número
                boolean existe = buscarHabitacionPorNumero(numero).isPresent();
                if (!existe) {
                    agregarHabitacion(h);
                    cargadas++;
                }
            } catch (NumberFormatException ex) {
                // ignorar línea con formato incorrecto
            }
        }
        return cargadas;
    }
}
