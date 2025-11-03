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

    /**
     * Carga reservas desde CSV. Formato por línea:
     * fechaInicio,fechaFin,numeroHab,nombre,apellido,dni,email,telefono
     * Las fechas deben ser yyyy-mm-dd.
     * Devuelve la cantidad de reservas añadidas correctamente.
     */
    public int cargarReservasDesdeCSV(String rutaRelativa) throws IOException {
        Path ruta = Paths.get(rutaRelativa);
        if (!Files.exists(ruta)) {
            throw new IOException("Archivo no encontrado: " + ruta.toAbsolutePath());
        }
        List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
        int cargadas = 0;
        int lineaNum = 0;
        for (String linea : lineas) {
            lineaNum++;
            if (linea == null) continue;
            String l = linea.trim();
            if (l.isEmpty() || l.startsWith("#")) continue;
            String[] parts = l.split(",");
            // aceptar dos formatos: con id inicial (9 campos) o sin id (8 campos)
            if (parts.length < 8) continue;
            try {
                int idx = 0;
                Integer idOpt = null;
                // si hay 9 o más campos y el primero parece un entero, tratarlo como id
                if (parts.length >= 9) {
                    try {
                        idOpt = Integer.parseInt(parts[0].trim());
                        idx = 1; // desplazamos los índices
                    } catch (NumberFormatException nfe) {
                        idOpt = null;
                        idx = 0;
                    }
                }

                LocalDate inicio = LocalDate.parse(parts[idx + 0].trim());
                LocalDate fin = LocalDate.parse(parts[idx + 1].trim());
                int numeroHab = Integer.parseInt(parts[idx + 2].trim());
                String nombre = parts[idx + 3].trim();
                String apellido = parts[idx + 4].trim();
                String dni = parts[idx + 5].trim();
                String email = parts[idx + 6].trim();
                String telefono = parts[idx + 7].trim();

                // Crear huesped y empleado por defecto si no hay
                Huesped h = new Huesped(nombre, apellido, dni, email, telefono);
                Empleado e = empleados.isEmpty() ? new Empleado(1, "Admin", "Admin", "00000000", "Reception") : empleados.get(0);

                // buscar habitación (debe existir)
                Optional<Habitacion> habOpt = buscarHabitacionPorNumero(numeroHab);
                if (!habOpt.isPresent()) continue; // ignorar si la habitación no existe
                Habitacion hab = habOpt.get();

                // debug print
                System.out.println("[DEBUG] Procesando linea " + lineaNum + ": idOpt=" + idOpt + " hab=" + numeroHab);

                // Si ya existe una reserva con idOpt, ignorar para evitar duplicados
                if (idOpt != null) {
                    int idVal = idOpt;
                    boolean existe = reservas.stream().anyMatch(r -> r.getIdReserva() == idVal);
                    if (existe) {
                        System.out.println("[DEBUG] Ya existe reserva con id " + idVal + ", se ignora");
                        continue;
                    }
                    // crear reserva con id explícito usando constructor alternativo
                    try {
                        Reserva r = new Reserva(idVal, inicio, fin, hab, h, e);
                        reservas.add(r);
                        h.addReserva(r);
                        cargadas++;
                        System.out.println("[DEBUG] Añadida reserva id=" + r.getIdReserva());
                    } catch (Exception ex) {
                        // ignorar si falla
                        System.out.println("[DEBUG] Fallo al crear reserva con id en linea " + lineaNum + ": " + ex.getMessage());
                    }
                } else {
                    // crear reserva usando el flujo normal (chequeo de disponibilidad dentro)
                    try {
                        Reserva r = crearReserva(inicio, fin, numeroHab, h, e);
                        // crearReserva ya agrega la reserva y asocia al huésped
                        cargadas++;
                        System.out.println("[DEBUG] Añadida reserva (auto id) id=" + r.getIdReserva());
                    } catch (Exception ex) {
                        // ignorar reservas que no se pueden crear
                        System.out.println("[DEBUG] No se pudo crear reserva en linea " + lineaNum + ": " + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                // ignorar línea malformada
                System.out.println("[DEBUG] Linea malformada " + lineaNum + ": " + ex.getMessage());
            }
        }
        return cargadas;
    }
}
