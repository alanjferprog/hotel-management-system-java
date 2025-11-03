package view;

import model.entities.Huesped;
import controller.DatabaseInitializer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class VerHuespedesPanel extends JPanel {
    private ControladorGUI controlador;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private JButton btnAgregar, btnModificar, btnEliminar;
    private Runnable onBack;

    public VerHuespedesPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"Nombre","Apellido","DNI","Email","Telefono"},0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAgregar = new JButton("Agregar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnVolver = new JButton("Volver");

        bottom.add(btnAgregar);
        bottom.add(btnModificar);
        bottom.add(btnEliminar);
        bottom.add(btnVolver);
        add(bottom, BorderLayout.SOUTH);

        // listeners básicos: abriremos diálogos simples
        btnVolver.addActionListener(e -> { if (onBack!=null) onBack.run(); else { Window w = SwingUtilities.getWindowAncestor(this); if (w instanceof MainFrame) ((MainFrame)w).showInicio(); } });

        btnAgregar.addActionListener(e -> onAgregar());
        btnModificar.addActionListener(e -> onModificar());
        btnEliminar.addActionListener(e -> onEliminar());
    }

    public void setOnBack(Runnable r) { this.onBack = r; }

    public void refresh() {
        modelo.setRowCount(0);
        boolean retried = false;
        while (true) {
            try {
                List<Huesped> clientes = controlador.cargarHuespedesDesdeDB();
                for (Huesped h : clientes) {
                    modelo.addRow(new Object[]{h.getNombre(), h.getApellido(), h.getDni(), h.getEmail(), h.getTelefono()});
                }
                return;
            } catch (Exception ex) {
                if (!retried && isNoSuchTable(ex)) {
                    try { DatabaseInitializer.initialize(); retried = true; continue; } catch (Exception ie) { JOptionPane.showMessageDialog(this, "No se pudieron crear tablas: " + ie.getMessage()); return; }
                }
                JOptionPane.showMessageDialog(this, "No se pudieron cargar huespedes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private boolean isNoSuchTable(Throwable ex) {
        if (ex==null) return false;
        String m = ex.getMessage(); if (m!=null && m.toLowerCase().contains("no such table")) return true; return isNoSuchTable(ex.getCause());
    }

    private void onAgregar() {
        HuespedFormDialog dlg = new HuespedFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            try {
                controlador.insertarHuespedEnDB(dlg.getHuesped());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al insertar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onModificar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un huésped"); return; }
        String dni = String.valueOf(modelo.getValueAt(sel,2));
        String nombre = String.valueOf(modelo.getValueAt(sel,0));
        String apellido = String.valueOf(modelo.getValueAt(sel,1));
        String email = String.valueOf(modelo.getValueAt(sel,3));
        String telefono = String.valueOf(modelo.getValueAt(sel,4));
        Huesped actual = new Huesped(nombre, apellido, dni, email, telefono);
        HuespedFormDialog dlg = new HuespedFormDialog(SwingUtilities.getWindowAncestor(this), actual);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            try {
                controlador.actualizarHuespedEnDB(dlg.getHuesped());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEliminar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un huésped"); return; }
        String dni = String.valueOf(modelo.getValueAt(sel,2));
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar huésped con DNI " + dni + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controlador.eliminarHuespedEnDB(dni);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
