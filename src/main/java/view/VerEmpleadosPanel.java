package view;

import javax.swing.JPanel;

import model.entities.Empleado;
import controller.EmpleadoController;
import controller.HotelController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;

public class VerEmpleadosPanel extends JPanel {
    private HotelController controlador;
    private EmpleadoController empleadoController = new EmpleadoController();
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private JButton btnAgregar, btnModificar, btnEliminar;
    private Runnable onBack;

    public VerEmpleadosPanel(HotelController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        // Mantener la columna Id en el modelo (para operaciones internas) pero ocultarla en la vista
        modelo = new DefaultTableModel(new Object[]{"Id","Nombre","Apellido","DNI","Cargo","Turno","Estado","En turno"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        // Ocultar la columna Id de la vista (tabla) pero conservarla en el modelo
        if (tabla.getColumnModel().getColumnCount() > 0) {
            try {
                tabla.removeColumn(tabla.getColumnModel().getColumn(0));
            } catch (Exception ex) {
                System.err.println("No se pudo ocultar columna Id en la tabla de empleados: " + ex.getMessage());
            }
        }
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
                // Intentar usar los empleados ya cargados en memoria
                List<Object[]> rows = empleadoController.getEmpleadoRows(controlador);
                if (!rows.isEmpty()) {
                    for (Object[] row : rows) modelo.addRow(row);
                    return;
                }
                // Si no hay empleados en memoria, intentar inicializar y cargar desde BD
                if (!retried) {
                    try {
                        empleadoController.initializeAndLoad(controlador);
                        retried = true;
                        continue;
                    } catch (Exception ie) {
                        JOptionPane.showMessageDialog(this, "No se pudieron crear tablas o cargar empleados: " + ie.getMessage());
                        return;
                    }
                }
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudieron cargar empleados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private void onAgregar() {
        // Formulario simple usando JOptionPane
        JTextField tfNombre = new JTextField();
        JTextField tfApellido = new JTextField();
        JTextField tfDni = new JTextField();
        JTextField tfCargo = new JTextField();
        JTextField tfTurno = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Nombre:")); panel.add(tfNombre);
        panel.add(new JLabel("Apellido:")); panel.add(tfApellido);
        panel.add(new JLabel("DNI:")); panel.add(tfDni);
        panel.add(new JLabel("Cargo:")); panel.add(tfCargo);
        panel.add(new JLabel("Turno:")); panel.add(tfTurno);

        int res = JOptionPane.showConfirmDialog(this, panel, "Agregar empleado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String dni = tfDni.getText().trim();
        String cargo = tfCargo.getText().trim();
        String turno = tfTurno.getText().trim();
        if (nombre.isEmpty() || dni.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y DNI son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Empleado nuevo = new Empleado(0, nombre, apellido, dni, cargo, turno);
        try {
            if (empleadoController.existsByDni(controlador, nuevo.getDni())) {
                JOptionPane.showMessageDialog(this, "Ya existe un empleado con DNI " + nuevo.getDni(), "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            empleadoController.insertEmpleado(controlador, nuevo);
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al insertar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
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
        // Mostrar formulario prellenado con JOptionPane
        JTextField tfNombre = new JTextField(nombre);
        JTextField tfApellido = new JTextField(apellido);
        JTextField tfDni = new JTextField(dni);
        JTextField tfCargo = new JTextField(cargo);
        JTextField tfTurno = new JTextField(turno);
        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Nombre:")); panel.add(tfNombre);
        panel.add(new JLabel("Apellido:")); panel.add(tfApellido);
        panel.add(new JLabel("DNI:")); panel.add(tfDni);
        panel.add(new JLabel("Cargo:")); panel.add(tfCargo);
        panel.add(new JLabel("Turno:")); panel.add(tfTurno);

        int res = JOptionPane.showConfirmDialog(this, panel, "Modificar empleado", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        Empleado actualizado = new Empleado(idEmpleado, tfNombre.getText().trim(), tfApellido.getText().trim(), tfDni.getText().trim(), tfCargo.getText().trim(), tfTurno.getText().trim());
        try {
            empleadoController.updateEmpleado(controlador, actualizado);
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEliminar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un empleado"); return; }
        String dni = String.valueOf(modelo.getValueAt(sel,3));
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar huésped con DNI " + dni + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                // usar controller para eliminar
                try {
                    empleadoController.deleteEmpleado(controlador, dni);
                    refresh();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}