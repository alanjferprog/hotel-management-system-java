package view;

import controller.ReservaController;
import controller.HotelController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VerReservasPanel extends JPanel {
    private HotelController controlador;
    private ReservaController reservaController = new ReservaController();
    private javax.swing.table.DefaultTableModel modeloReservas;
    private JTable tablaReservas;
    private JTextField tfBuscarId;
    private JButton btnBuscar, btnMostrarTodos, btnVolver;
    private Runnable onBack;

    public VerReservasPanel(HotelController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        // Top: búsqueda
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfBuscarId = new JTextField(8);
        btnBuscar = new JButton("Buscar por ID");
        btnMostrarTodos = new JButton("Mostrar todos");
        top.add(new JLabel("ID:")); top.add(tfBuscarId); top.add(btnBuscar); top.add(btnMostrarTodos);
        add(top, BorderLayout.NORTH);

        // Tabla
        modeloReservas = new javax.swing.table.DefaultTableModel(new Object[]{"ID","Huésped","Hab","Inicio","Fin","Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setFillsViewportHeight(true);
        // ocultar columna ID visualmente
        try { tablaReservas.removeColumn(tablaReservas.getColumnModel().getColumn(0)); } catch (Exception ignored) {}

        add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        // Bottom: volver
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnVolver = new JButton("Volver");
        bottom.add(btnVolver);
        add(bottom, BorderLayout.SOUTH);

        // Listeners
        btnBuscar.addActionListener(e -> onBuscar());
        btnMostrarTodos.addActionListener(e -> refresh());
        btnVolver.addActionListener(e -> { if (onBack != null) onBack.run(); });

        // Cargar filas iniciales
        refresh();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    public void refresh() {
        modeloReservas.setRowCount(0);
        try {
            // intentar obtener filas desde el controlador
            List<Object[]> rows = reservaController.getReservationRows(controlador);
            if (rows != null) {
                for (Object[] row : rows) modeloReservas.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar reservas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onBuscar() {
        String s = tfBuscarId.getText().trim();
        if (s.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese un ID"); return; }
        try {
            int id = Integer.parseInt(s);
            var opt = reservaController.findReservationRowById(id, controlador);
            if (opt.isPresent()) {
                modeloReservas.setRowCount(0);
                modeloReservas.addRow(opt.get());
            } else {
                JOptionPane.showMessageDialog(this, "Reserva no encontrada: " + id);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "ID inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
