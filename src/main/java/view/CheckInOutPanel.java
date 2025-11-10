package view;

import model.entities.*;
import model.exceptions.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.sql.SQLException;

public class CheckInOutPanel extends JPanel {
    private ControladorGUI controlador;
    private Runnable onBack; // callback para volver

    private JPanel leftCol; // Check-In
    private JPanel rightCol; // Check-Out

    public CheckInOutPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(1, 2, 8, 8));

        leftCol = new JPanel();
        leftCol.setLayout(new BorderLayout());
        leftCol.setBorder(BorderFactory.createTitledBorder("CHECK IN"));

        rightCol = new JPanel();
        rightCol.setLayout(new BorderLayout());
        rightCol.setBorder(BorderFactory.createTitledBorder("CHECK OUT"));

        main.add(leftCol);
        main.add(rightCol);

        add(main, BorderLayout.CENTER);

        // Botón actualizar
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> refresh());
        topButtons.add(btnRefresh);
        add(topButtons, BorderLayout.NORTH);

        // Botón volver en el bottom
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> {
            if (onBack != null) onBack.run();
            else {
                java.awt.Window w = SwingUtilities.getWindowAncestor(this);
                if (w instanceof MainFrame) {
                    ((MainFrame) w).showInicio();
                }
            }
        });
        bottomButtons.add(btnVolver);
        add(bottomButtons, BorderLayout.SOUTH);

        // Inicializar vista
        refresh();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    /** Reconstruye la vista desde los datos en memoria (ControladorGUI/Hotel) */
    public void refresh() {
        // Limpiar columnas
        leftCol.removeAll();
        rightCol.removeAll();

        // Paneles para secciones
        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));

        JPanel rightContent = new JPanel();
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));

        // Secciones: Realizados / Próximos
        leftContent.add(sectionTitle("✔ Realizados", Color.GREEN.darker()));
        JPanel leftRealizados = new JPanel(); leftRealizados.setLayout(new BoxLayout(leftRealizados, BoxLayout.Y_AXIS));

        leftContent.add(leftRealizados);
        leftContent.add(Box.createVerticalStrut(8));

        leftContent.add(sectionTitle("⏰ Próximos", Color.ORANGE));
        JPanel leftProximos = new JPanel(); leftProximos.setLayout(new BoxLayout(leftProximos, BoxLayout.Y_AXIS));
        leftContent.add(leftProximos);

        rightContent.add(sectionTitle("✔ Realizados", Color.GREEN.darker()));
        JPanel rightRealizados = new JPanel(); rightRealizados.setLayout(new BoxLayout(rightRealizados, BoxLayout.Y_AXIS));
        rightContent.add(rightRealizados);
        rightContent.add(Box.createVerticalStrut(8));

        rightContent.add(sectionTitle("⚠ Próximos / demoras", Color.ORANGE));
        JPanel rightProximos = new JPanel(); rightProximos.setLayout(new BoxLayout(rightProximos, BoxLayout.Y_AXIS));
        rightContent.add(rightProximos);

        // Obtener reservas
        List<Reserva> reservas = controlador.getHotel().getReservas();
        LocalDate hoy = LocalDate.now();

        for (Reserva r : reservas) {
            // Check-In logic
            if (isCheckInRealizado(r)) {
                leftRealizados.add(createRowPanel(r, TipoFila.CHECKIN, EstadoFila.REALIZADO));
            } else if (isCheckInProximo(r, hoy)) {
                leftProximos.add(createRowPanel(r, TipoFila.CHECKIN, EstadoFila.PROXIMO));
            }

            // Check-Out logic
            if (isCheckOutRealizado(r)) {
                rightRealizados.add(createRowPanel(r, TipoFila.CHECKOUT, EstadoFila.REALIZADO));
            } else if (isCheckOutAtrasado(r, hoy)) {
                rightProximos.add(createRowPanel(r, TipoFila.CHECKOUT, EstadoFila.ATRASADO));
            } else if (isCheckOutProximo(r, hoy)) {
                rightProximos.add(createRowPanel(r, TipoFila.CHECKOUT, EstadoFila.PROXIMO));
            }
        }

        // Añadir scroll panes por columna
        leftCol.add(new JScrollPane(leftContent), BorderLayout.CENTER);
        rightCol.add(new JScrollPane(rightContent), BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private enum TipoFila { CHECKIN, CHECKOUT }
    private enum EstadoFila { REALIZADO, PROXIMO, ATRASADO, EN_TIEMPO }

    private boolean isCheckInRealizado(Reserva r) {
        return "confirmada".equalsIgnoreCase(r.getEstado());
    }
    private boolean isCheckOutRealizado(Reserva r) {
        return "cancelada".equalsIgnoreCase(r.getEstado());
    }

    private boolean isCheckInProximo(Reserva r, LocalDate hoy) {
        // Próximo si fechaInicio >= hoy and not confirmed
        return !isCheckInRealizado(r) && !isCheckOutRealizado(r) && ( !r.getFechaInicio().isBefore(hoy) );
    }

    private boolean isCheckOutProximo(Reserva r, LocalDate hoy) {
        // Próximo si fechaFin >= hoy and still confirmada (checked-in) or pendiente
        return !isCheckOutRealizado(r) && ( !r.getFechaFin().isBefore(hoy) );
    }

    private boolean isCheckOutAtrasado(Reserva r, LocalDate hoy) {
        // Atrasado si fechaFin < hoy and not checked-out yet
        return !isCheckOutRealizado(r) && r.getFechaFin().isBefore(hoy);
    }

    private JPanel sectionTitle(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        JPanel p = new JPanel(new BorderLayout()); p.add(lbl, BorderLayout.WEST);
        return p;
    }

    private JPanel createRowPanel(Reserva r, TipoFila tipo, EstadoFila estado) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        // Izquierda: icono/estado y texto
        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("- " + r.getHuesped().getNombre() + " " + r.getHuesped().getApellido() + " (Hab " + r.getHabitacion().getNumero() + ")");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        left.add(title);

        String timeText = (tipo == TipoFila.CHECKIN) ? fechaRelativeText(r.getFechaInicio()) : fechaRelativeText(r.getFechaFin());
        JLabel timeLbl = new JLabel(timeText);
        left.add(timeLbl);

        row.add(left, BorderLayout.CENTER);

        // Right: botones de acción rápida
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnMarcar = new JButton(tipo == TipoFila.CHECKIN ? "✅ Marcar" : "✅ Marcar");
        JButton btnVer = new JButton("👁 Ver reserva");
        JButton btnContact = new JButton("💬 Contactar huésped"); btnContact.setEnabled(false);
        JButton btnFactura = new JButton("🧾 Ver factura"); btnFactura.setEnabled(false);

        btnMarcar.addActionListener(e -> onMarcar(r, tipo));
        btnVer.addActionListener(e -> JOptionPane.showMessageDialog(this, r.toString(), "Reserva", JOptionPane.INFORMATION_MESSAGE));

        actions.add(btnMarcar);
        actions.add(btnVer);
        actions.add(btnContact);
        actions.add(btnFactura);

        row.add(actions, BorderLayout.EAST);

        return row;
    }

    private String fechaRelativeText(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        long dias = ChronoUnit.DAYS.between(hoy, fecha);
        if (dias == 0) return "Hoy";
        if (dias > 0) return "En " + dias + " día(s)";
        return "Hace " + Math.abs(dias) + " día(s)";
    }

    private void onMarcar(Reserva r, TipoFila tipo) {
        try {
            Empleado empleado = controlador.getHotel().getEmpleados().isEmpty() ? new Empleado(0, "Sistema", "Auto", "00000000", "Sys", "All") : controlador.getHotel().getEmpleados().get(0);
            if (tipo == TipoFila.CHECKIN) {
                new CheckIn(empleado, r);
                JOptionPane.showMessageDialog(this, "Check-In realizado correctamente para la reserva #" + r.getIdReserva());
            } else {
                // Check-Out: no solicitar total al usuario (usar 0.0 por defecto)
                double total = 0.0;
                new CheckOut(empleado, r, total);
                JOptionPane.showMessageDialog(this, "Check-Out realizado correctamente para la reserva #" + r.getIdReserva());
            }

            // Persistir cambios en BD: guardar reserva y actualizar estado de habitación
            try {
                controlador.guardarReservaEnDB(r);
                // actualizar estado habitacion
                controlador.actualizarEstadoHabitacionEnDB(r.getHabitacion().getNumero(), r.getHabitacion().getEstado().getDbValue());
            } catch (SQLException sqlEx) {
                JOptionPane.showMessageDialog(this, "Error guardando cambios en BD: " + sqlEx.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }

            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al marcar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
