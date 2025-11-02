package view;

import model.entities.*;
import model.exceptions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CheckInOutPanel extends JPanel {
    private ControladorGUI controlador;
    private JTextArea taOutput;

    public CheckInOutPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        taOutput = new JTextArea(10, 50);
        taOutput.setEditable(false);
        add(new JScrollPane(taOutput), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JButton btnCheckIn = new JButton("Check-In");
        btnCheckIn.addActionListener(this::onCheckIn);
        JButton btnCheckOut = new JButton("Check-Out");
        btnCheckOut.addActionListener(this::onCheckOut);

        buttonsPanel.add(btnCheckIn);
        buttonsPanel.add(btnCheckOut);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void onCheckIn(ActionEvent e) {
        try {
            int idReserva = Integer.parseInt(JOptionPane.showInputDialog(this, "Ingresa ID de la reserva para Check-In:"));
            Reserva r = controlador.getHotel().getReservas().stream().filter(reserva -> reserva.getIdReserva() == idReserva).findFirst().orElseThrow(() -> new ReservaInvalidaException("Reserva no encontrada"));
            Empleado empleado = controlador.getHotel().getEmpleados().get(0);

            new CheckIn(empleado, r);
            taOutput.append("Check-In realizado correctamente para la reserva #" + idReserva + "\n");
        } catch (ReservaInvalidaException | NumberFormatException ex) {
            taOutput.append("Error: " + ex.getMessage() + "\n");
        }
    }

    private void onCheckOut(ActionEvent e) {
        try {
            int idReserva = Integer.parseInt(JOptionPane.showInputDialog(this, "Ingresa ID de la reserva para Check-Out:"));
            Reserva r = controlador.getHotel().getReservas().stream().filter(reserva -> reserva.getIdReserva() == idReserva).findFirst().orElseThrow(() -> new ReservaInvalidaException("Reserva no encontrada"));
            Empleado empleado = controlador.getHotel().getEmpleados().get(0);
            double totalConsumido = 100.0;

            new CheckOut(empleado, r, totalConsumido);
            taOutput.append("Check-Out realizado correctamente para la reserva #" + idReserva + "\n");
        } catch (ReservaInvalidaException | NumberFormatException ex) {
            taOutput.append("Error: " + ex.getMessage() + "\n");
        }
    }
}
