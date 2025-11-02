package view;

import model.entities.*;
import model.exceptions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class ReservarPanel extends JPanel {
    private ControladorGUI controlador;
    private JTextField tfNombre, tfApellido, tfDni, tfEmail, tfTelefono, tfNumeroHab;
    private JTextField tfFechaInicio, tfFechaFin;
    private JTextArea taOutput;

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

        JButton btnReservar = new JButton("Crear Reserva");
        btnReservar.addActionListener(this::onReservar);

        taOutput = new JTextArea(8,40);
        taOutput.setEditable(false);

        add(form, BorderLayout.NORTH);
        add(btnReservar, BorderLayout.CENTER);
        add(new JScrollPane(taOutput), BorderLayout.SOUTH);
    }

    private void onReservar(ActionEvent ev) {
        try {
            String nombre = tfNombre.getText().trim();
            String apellido = tfApellido.getText().trim();
            String dni = tfDni.getText().trim();
            String email = tfEmail.getText().trim();
            String telefono = tfTelefono.getText().trim();
            int numeroHab = Integer.parseInt(tfNumeroHab.getText().trim());
            LocalDate inicio = LocalDate.parse(tfFechaInicio.getText().trim());
            LocalDate fin = LocalDate.parse(tfFechaFin.getText().trim());

            Huesped h = new Huesped(nombre, apellido, dni, email, telefono);

            Empleado e = controlador.getHotel().getEmpleados().isEmpty()
                    ? new Empleado(1, "Admin", "Admin", "00000000", "Reception")
                    : controlador.getHotel().getEmpleados().get(0);

            Reserva r = controlador.crearReserva(inicio, fin, numeroHab, h, e);
            taOutput.append("Reserva creada: " + r + "\n");
        } catch (NumberFormatException nfe) {
            taOutput.append("Número de habitación inválido\n");
        } catch (HabitacionNoDisponibleException | ReservaInvalidaException ex) {
            taOutput.append("Error: " + ex.getMessage() + "\n");
        } catch (Exception ex) {
            taOutput.append("Error inesperado: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }
}
