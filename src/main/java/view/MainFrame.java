package view;

import controller.ReservaController;
import model.entities.EstadoHabitacion;
import model.entities.Empleado;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private ControladorGUI controlador;
    private ReservarPanel reservarPanel;
    private VerHabitacionesPanel verPanel;
    private CheckInOutPanel checkinout;
    private VerEmpleadosPanel empleadoPanel;

    private CardLayout cardLayout;
    private JPanel contentPanel;

    public MainFrame(ControladorGUI controlador) {
        this.controlador = controlador;
        setTitle("Sistema de Reservas - " + controlador.getHotel().getNombre());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Intentar cargar habitaciones desde DB al inicializar la UI
        try {
            int cargadas = controlador.cargarHabitacionesDesdeDB();
            System.out.println("Habitaciones cargadas desde DB (MainFrame): " + cargadas);
            // Cargar reservas asociadas desde DB
            int rc = controlador.cargarReservasDesdeDB();
            System.out.println("Reservas cargadas desde DB (MainFrame): " + rc);
            // Cargar empleados desde DB y poblar en memoria
            try {
                java.util.List<model.entities.Empleado> empleados = controlador.cargarEmpleadoDesdeBD();
                controlador.getHotel().getEmpleados().clear();
                for (model.entities.Empleado emp : empleados) controlador.getHotel().getEmpleados().add(emp);
                System.out.println("Empleados cargados desde BD (MainFrame): " + empleados.size());
            } catch (Exception exEmp) {
                System.err.println("No se pudieron cargar empleados desde BD en MainFrame: " + exEmp.getMessage());
            }
        } catch (Exception ex) {
            System.err.println("No se pudieron cargar datos desde DB en MainFrame: " + ex.getMessage());
            // fallback: se puede poblar manualmente desde Main si es necesario
        }

        // Header (logo / nombre)
        JPanel header = new JPanel(new BorderLayout());
        JLabel logoLabel = new JLabel(controlador.getHotel().getNombre(), SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        header.add(logoLabel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Panels (instancias)
        empleadoPanel = new VerEmpleadosPanel(controlador);
        reservarPanel = new ReservarPanel(controlador);
        VerReservasPanel verReservasPanel = new VerReservasPanel(controlador);
        verPanel = new VerHabitacionesPanel(controlador);
        checkinout = new CheckInOutPanel(controlador);

        // Content con CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Tarjeta inicial: botones centrados
        JPanel inicioPanel = new JPanel();
        inicioPanel.setLayout(new BoxLayout(inicioPanel, BoxLayout.Y_AXIS));
        inicioPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnVerEmpleados = new JButton("Ver Empleados");
        JButton btnVerHabitaciones = new JButton("Ver habitaciones");
        JButton btnVerReserva = new JButton("Ver reserva");
        JButton btnVerHuespedes = new JButton("Ver huéspedes");
        JButton btnCheckInOut = new JButton("Check-in / Check-out");
        JButton btnSalir = new JButton("Salir");

        Dimension btnSize = new Dimension(220, 54);
        Font btnFont = new Font("SansSerif", Font.PLAIN, 16);

        for (JButton b : new JButton[] { btnVerEmpleados, btnVerHabitaciones, btnVerReserva, btnCheckInOut }) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setFont(btnFont);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        // Añadir estilo al botón Ver Huéspedes
        btnVerHuespedes.setPreferredSize(btnSize);
        btnVerHuespedes.setMaximumSize(btnSize);
        btnVerHuespedes.setFont(btnFont);
        btnVerHuespedes.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Salir con más separación encima
        btnSalir.setPreferredSize(btnSize);
        btnSalir.setMaximumSize(btnSize);
        btnSalir.setFont(btnFont);
        btnSalir.setAlignmentX(Component.CENTER_ALIGNMENT);

        inicioPanel.add(Box.createVerticalGlue());
        inicioPanel.add(btnVerEmpleados);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnVerHabitaciones);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnVerReserva);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnVerHuespedes);
        inicioPanel.add(Box.createVerticalStrut(14));
        inicioPanel.add(btnCheckInOut);
        inicioPanel.add(Box.createVerticalStrut(28)); // más separación antes de Salir
        inicioPanel.add(btnSalir);
        inicioPanel.add(Box.createVerticalGlue());

        contentPanel.add(inicioPanel, "INICIO");
        contentPanel.add(reservarPanel, "RESERVAR");
        contentPanel.add(verPanel, "VER");
        contentPanel.add(verReservasPanel, "RESERVAS");
        // Registrar el panel de empleados en el CardLayout
        contentPanel.add(empleadoPanel, "EMPLEADOS");
        // agregar panel de huéspedes
        VerHuespedesPanel huespedesPanel = new VerHuespedesPanel(controlador);
        contentPanel.add(huespedesPanel, "HUESPEDES");
        contentPanel.add(checkinout, "CHECK");
        add(contentPanel, BorderLayout.CENTER);

        // Conectar acción de volver desde VerHabitacionesPanel
        verPanel.setOnBack(() -> cardLayout.show(contentPanel, "INICIO"));
        // Conectar acción de volver desde VerHuespedesPanel
        huespedesPanel.setOnBack(() -> cardLayout.show(contentPanel, "INICIO"));
        // Conectar acción de reservar desde VerHabitacionesPanel: mostrar formulario y prefill numero
        verPanel.setOnReservar(numero -> {
            reservarPanel.setNumeroHabitacion(numero);
            cardLayout.show(contentPanel, "RESERVAR");
        });

        // Nuevo: manejar pedir limpieza desde la lista de habitaciones
        verPanel.setOnPedirLimpieza(numero -> {
            try {
                // Selección de empleado para asignar a limpieza (solo personal de limpieza)
                java.util.List<Empleado> empleadosAll = controlador.getHotel().getEmpleados();
                java.util.List<Empleado> limpieza = new java.util.ArrayList<>();
                for (Empleado emp : empleadosAll) {
                    if (emp.getCargo() != null && emp.getCargo().toLowerCase().contains("limpieza") && controlador.estaEmpleadoEnTurno(emp)) limpieza.add(emp);
                }
                String[] opciones;
                if (limpieza.isEmpty()) {
                    opciones = new String[] { "No asignar" };
                } else {
                    opciones = new String[limpieza.size() + 1];
                    opciones[0] = "No asignar";
                    for (int i = 0; i < limpieza.size(); i++) {
                        opciones[i+1] = limpieza.get(i).getNombre() + " " + limpieza.get(i).getApellido();
                    }
                }
                String elegido = (String) JOptionPane.showInputDialog(this, "Asignar limpieza a:", "Asignar empleado de limpieza", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

                // cambiar estado en memoria a 'limpieza_pedida' para indicar que ya se solicitó
                controlador.getHotel().buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEstado(EstadoHabitacion.LIMPIEZA_PEDIDA));
                // persistir cambio en BD
                controlador.actualizarEstadoHabitacionEnDB(numero, EstadoHabitacion.LIMPIEZA_PEDIDA.getDbValue());

                // si se seleccionó un empleado válido, marcarlo como ocupado y persistir
                if (elegido != null && !"No asignar".equals(elegido) && !limpieza.isEmpty()) {
                    int idx = java.util.Arrays.asList(opciones).indexOf(elegido) - 1; // porque opciones[0] = No asignar
                    if (idx >= 0 && idx < limpieza.size()) {
                        Empleado emp = limpieza.get(idx);
                        try {
                            controlador.asignarEmpleadoAHabitacion(numero, emp.getDni());
                            if (empleadoPanel != null) empleadoPanel.refresh();
                        } catch (java.sql.SQLException ex) {
                            JOptionPane.showMessageDialog(this, "No se pudo asignar el empleado a la habitación: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                // refrescar paneles
                verPanel.refresh();
                checkinout.refresh();

                String msg = "Solicitud de limpieza enviada para la habitación " + numero;
                if (elegido != null && !"No asignar".equals(elegido)) msg += " (asignada a: " + elegido + ")";
                JOptionPane.showMessageDialog(this, msg, "Limpieza solicitada", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo solicitar la limpieza: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Botón Volver dentro del ReservarPanel vuelve a INICIO (ajustable)
        reservarPanel.setOnBack(() -> cardLayout.show(contentPanel, "INICIO"));

        // Listeners
        btnVerHabitaciones.addActionListener(e -> {
            cardLayout.show(contentPanel, "VER");
            verPanel.refresh();
        });

        btnVerHuespedes.addActionListener(e -> {
            cardLayout.show(contentPanel, "HUESPEDES");
            huespedesPanel.refresh();
        });

        btnVerEmpleados.addActionListener(e -> {
            cardLayout.show(contentPanel, "EMPLEADOS");
            empleadoPanel.refresh();
        });

        btnVerReserva.addActionListener(e -> {
            cardLayout.show(contentPanel, "RESERVAS");
            verReservasPanel.refresh();
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

    /** Muestra la tarjeta de inicio (menú principal) */
    public void showInicio() {
        cardLayout.show(contentPanel, "INICIO");
    }
}
