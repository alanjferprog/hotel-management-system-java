package view;

import model.entities.*;
import model.exceptions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.sql.SQLException; // añadido
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.format.SignStyle;

public class ReservarPanel extends JPanel {
    private ControladorGUI controlador;
    private JTextField tfNombre, tfApellido, tfDni, tfEmail, tfTelefono, tfNumeroHab;
    private JTextField tfFechaInicio, tfFechaFin;
    private JButton btnReservar;
    private JButton btnVolver;
    private Runnable onBackCallback;

    // Formato flexible: acepta yyyy-M-d o yyyy-MM-dd (mes y día con 1 o 2 dígitos)
    private static final java.time.format.DateTimeFormatter FLEXIBLE_DATE_FORMAT =
            new DateTimeFormatterBuilder()
                    .appendValue(ChronoField.YEAR, 4)
                    .appendLiteral('-')
                    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral('-')
                    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                    .toFormatter();

    public ReservarPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        tfNombre = new JTextField();
        tfApellido = new JTextField();
        tfDni = new JTextField();
        tfEmail = new JTextField();
        tfTelefono = new JTextField();
        tfNumeroHab = new JTextField();
        tfFechaInicio = new JTextField("yyyy-mm-dd");
        tfFechaFin = new JTextField("yyyy-mm-dd");

        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("DNI:")); form.add(tfDni);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Telefono:")); form.add(tfTelefono);
        form.add(new JLabel("Número Hab:")); form.add(tfNumeroHab);
        form.add(new JLabel("Fecha Inicio:")); form.add(tfFechaInicio);
        form.add(new JLabel("Fecha Fin:")); form.add(tfFechaFin);

        btnReservar = new JButton("Crear Reserva");
        btnReservar.addActionListener(this::onReservar);

        // Botón Volver local
        btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> {
            if (onBackCallback != null) onBackCallback.run();
        });

        // Colocar el formulario en el centro para que ocupe el espacio y no deje un área en blanco debajo
        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        bottom.add(btnReservar);
        bottom.add(Box.createHorizontalStrut(12));
        bottom.add(btnVolver);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Prefill número de habitación (utilizado cuando se reserva desde la tabla).
     */
    public void setNumeroHabitacion(int numero) {
        tfNumeroHab.setText(String.valueOf(numero));
        tfFechaInicio.requestFocusInWindow();
    }

    /** Permite asignar acción al botón Volver dentro del panel. */
    public void setOnBack(Runnable onBack) {
        this.onBackCallback = onBack;
    }

    private LocalDate parseFechaFlexible(String text) throws IllegalArgumentException {
        String t = text == null ? "" : text.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("yyyy-mm-dd")) {
            throw new IllegalArgumentException("La fecha no está completada");
        }
        try {
            return LocalDate.parse(t, FLEXIBLE_DATE_FORMAT);
        } catch (DateTimeParseException dtpe) {
            // Re-throw para manejo arriba
            throw new IllegalArgumentException("Formato de fecha inválido. Use yyyy-MM-dd o yyyy-M-d", dtpe);
        }
    }

    private void limpiarFormulario() {
        tfNombre.setText("");
        tfApellido.setText("");
        tfDni.setText("");
        tfEmail.setText("");
        tfTelefono.setText("");
        tfNumeroHab.setText("");
        tfFechaInicio.setText("yyyy-mm-dd");
        tfFechaFin.setText("yyyy-mm-dd");
    }

    private void onReservar(ActionEvent ev) {
        try {
            String nombre = tfNombre.getText().trim();
            String apellido = tfApellido.getText().trim();
            String dni = tfDni.getText().trim();
            String email = tfEmail.getText().trim();
            String telefono = tfTelefono.getText().trim();
            int numeroHab = Integer.parseInt(tfNumeroHab.getText().trim());
            LocalDate inicio = parseFechaFlexible(tfFechaInicio.getText());
            LocalDate fin = parseFechaFlexible(tfFechaFin.getText());

            Huesped h = new Huesped(nombre, apellido, dni, email, telefono);

            Empleado e = controlador.getHotel().getEmpleados().isEmpty()
                    ? new Empleado(1, "Admin", "Admin", "00000000", "Reception", "AM")
                    : controlador.getHotel().getEmpleados().get(0);

            // Si el hotel no tiene empleados, añadir el empleado por defecto para que las reservas queden ligadas
            if (controlador.getHotel().getEmpleados().isEmpty()) {
                controlador.getHotel().agregarEmpleado(e);
            }

            Reserva r = controlador.crearReserva(inicio, fin, numeroHab, h, e);

            // Persistir en BD
            try {
                controlador.guardarReservaEnDB(r);
                // Mostrar componente de éxito con el número de reserva
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Reserva realizada con éxito, su nro de reserva es: #" + r.getIdReserva(),
                            "Reserva exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    // Refrescar tabla de habitaciones en MainFrame si está presente
                    java.awt.Window w = SwingUtilities.getWindowAncestor(this);
                    if (w instanceof MainFrame) {
                        ((MainFrame) w).refresh();
                        // Volver al menú principal
                        ((MainFrame) w).showInicio();
                    }
                });

                // limpiar formulario tras el éxito
                limpiarFormulario();

            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(this, "Error al guardar en DB: " + sqle.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Número de habitación inválido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HabitacionNoDisponibleException | ReservaInvalidaException ex) {
            // Mostrar alerta modal para estos errores
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No es posible reservar", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Fecha inválida", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
