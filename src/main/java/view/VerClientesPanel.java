package view;

import model.entities.Huesped;
import view.ControladorGUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class VerClientesPanel extends JPanel {
    private ControladorGUI controlador;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private Runnable onBack;

    public VerClientesPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"Nombre","Apellido","DNI","Email","Telefono"},0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> { if (onBack!=null) onBack.run(); });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(btnVolver);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    public void refresh() {
        modelo.setRowCount(0);
        // cargar clientes desde controlador -> controlador debe exponer método para cargar clientes desde BD
        try {
            List<Huesped> clientes = controlador.cargarClientesDesdeDB();
            for (Huesped h : clientes) {
                modelo.addRow(new Object[]{h.getNombre(), h.getApellido(), h.getDni(), h.getEmail(), h.getTelefono()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar clientes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

