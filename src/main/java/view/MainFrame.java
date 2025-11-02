package view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ControladorGUI controlador;
    private ReservarPanel reservarPanel;
    private VerHabitacionesPanel verPanel;
    private CheckInOutPanel checkinout;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainFrame(ControladorGUI controlador) {
        this.controlador = controlador;
        setTitle("Sistema de Reservas - " + controlador.getHotel().getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Header (logo / nombre)
        JPanel header = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel(controlador.getHotel().getNombre(), SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(logoLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Panels (instancias)
        reservarPanel = new ReservarPanel(controlador);
        verPanel = new VerHabitacionesPanel(controlador);
        checkinout = new CheckInOutPanel(controlador);

        // Content con CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Tarjeta inicial: botones centrados
        JPanel inicioPanel = new JPanel();
        inicioPanel.setLayout(new BoxLayout(inicioPanel, BoxLayout.Y_AXIS));
        inicioPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnVerHabitaciones = new JButton("Ver habitaciones");
        JButton btnVerReserva = new JButton("Ver reserva");
        JButton btnCheckInOut = new JButton("Check-in / Check-out");
        JButton btnSalir = new JButton("Salir");

        Dimension btnSize = new Dimension(220, 54);
        Font btnFont = new Font("SansSerif", Font.PLAIN, 16);

        for (JButton b : new JButton[] { btnVerHabitaciones, btnVerReserva, btnCheckInOut }) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setFont(btnFont);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        // Salir con más separación encima
        btnSalir.setPreferredSize(btnSize);
        btnSalir.setMaximumSize(btnSize);
        btnSalir.setFont(btnFont);
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);

        inicioPanel.add(Box.createVerticalGlue());
        inicioPanel.add(btnVerHabitaciones);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnVerReserva);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnCheckInOut);
        inicioPanel.add(Box.createVerticalStrut(28)); // más separación antes de Salir
        inicioPanel.add(btnSalir);
        inicioPanel.add(Box.createVerticalGlue());

        contentPanel.add(inicioPanel, "INICIO");
        contentPanel.add(reservarPanel, "RESERVAR");
        contentPanel.add(verPanel, "VER");
        contentPanel.add(checkinout, "CHECK");
        add(contentPanel, BorderLayout.CENTER);

        // Conectar acción de volver desde VerHabitacionesPanel
        verPanel.setOnBack(() -> cardLayout.show(contentPanel, "INICIO"));
        // Conectar acción de reservar desde VerHabitacionesPanel: mostrar formulario y prefill numero
        verPanel.setOnReservar(numero -> {
            reservarPanel.setNumeroHabitacion(numero);
            cardLayout.show(contentPanel, "RESERVAR");
        });

        // Botón Volver dentro del ReservarPanel vuelve a INICIO (ajustable)
        reservarPanel.setOnBack(() -> cardLayout.show(contentPanel, "INICIO"));

        // Listeners
        btnVerHabitaciones.addActionListener(e -> {
            cardLayout.show(contentPanel, "VER");
            verPanel.refresh();
        });

        btnVerReserva.addActionListener(e -> {
            cardLayout.show(contentPanel, "RESERVAR");
        });

        btnCheckInOut.addActionListener(e -> {
            cardLayout.show(contentPanel, "CHECK");
        });

        btnSalir.addActionListener(e -> {
            dispose();
            System.exit(0);
        });

        // Mostrar tarjeta INICIO (botones centrados)
        cardLayout.show(contentPanel, "INICIO");
    }

    public void refresh() {
        verPanel.refresh();
    }
}
