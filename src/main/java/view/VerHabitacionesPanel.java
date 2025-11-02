package view;

import model.entities.Habitacion;

import javax.swing.*;
import java.awt.*;

public class VerHabitacionesPanel extends JPanel {
    private ControladorGUI controlador;
    private JTextArea taListado;

    public VerHabitacionesPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        taListado = new JTextArea();
        taListado.setEditable(false);
        add(new JScrollPane(taListado), BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        StringBuilder sb = new StringBuilder();
        sb.append("Habitaciones:\n");
        for (Habitacion h : controlador.getHotel().getHabitaciones()) {
            sb.append(h).append("\n");
        }
        taListado.setText(sb.toString());
    }
}
