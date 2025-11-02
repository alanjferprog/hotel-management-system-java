package app;

// File: src/main/java/view/Main.java

import model.core.Hotel;
import view.ControladorGUI;
import view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Hotel hotel = new Hotel("Hotel Ejemplo");
            ControladorGUI controlador = new ControladorGUI(hotel);
            MainFrame frame = new MainFrame(controlador);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

