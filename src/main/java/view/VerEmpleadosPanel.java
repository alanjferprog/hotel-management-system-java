package view;

import javax.swing.JPanel;

import model.entities.Empleado;
import controller.DatabaseInitializer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class VerEmpleadosPanel extends JPanel {
    private ControladorGUI controlador;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private JButton btnAgregar, btnModificar, btnEliminar;
    private Runnable onBack;

    public VerEmpleadosPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{0 ,"Nombre","Apellido","DNI","Cargo","Turno"}, 0) {
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
                List<Empleado> empleados = controlador.cargarEmpleadoDesdeBD();
                for (Empleado e : empleados) {
                    modelo.addRow(new Object[]{e.getIdEmpleado(), e.getNombre(), e.getApellido(), e.getDni(), e.getCargo(), e.getTurno()});
                }
                return;
            } catch (Exception ex) {
                if (!retried && isNoSuchTable(ex)) {
                    try { DatabaseInitializer.initialize(); retried = true; continue; } catch (Exception ie) { JOptionPane.showMessageDialog(this, "No se pudieron crear tablas: " + ie.getMessage()); return; }
                }
                JOptionPane.showMessageDialog(this, "No se pudieron cargar empleados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private boolean isNoSuchTable(Throwable ex) {
        if (ex==null) return false;
        String m = ex.getMessage(); if (m!=null && m.toLowerCase().contains("no such table")) return true; return isNoSuchTable(ex.getCause());
    }

    private void onAgregar() {
        EmpleadoFormDialog dlg = new EmpleadoFormDialog(SwingUtilities.getWindowAncestor(this), null);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            try {
                controlador.insertarEmpleadoEnDB(dlg.getEmpleado());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al insertar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onModificar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un Empleado"); return; }
        int idEmpleado = Integer.valueOf(String.valueOf(modelo.getValueAt(sel, 0)));
        String nombre = String.valueOf(modelo.getValueAt(sel,1));
        String apellido = String.valueOf(modelo.getValueAt(sel,2));
        String dni = String.valueOf(modelo.getValueAt(sel,3));
        String cargo = String.valueOf(modelo.getValueAt(sel,4));
        String turno = String.valueOf(modelo.getValueAt(sel,5));
        Empleado actual = new Empleado(idEmpleado, nombre, apellido, dni, cargo, turno);
        EmpleadoFormDialog dlg = new EmpleadoFormDialog(SwingUtilities.getWindowAncestor(this), actual);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            try {
                controlador.actualizarEmpleadoEnDB(dlg.getEmpleado());
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEliminar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un empleado"); return; }
        String dni = String.valueOf(modelo.getValueAt(sel,3));
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar huésped con DNI " + dni + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                controlador.eliminarEmpleadoEnDB(dni);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}