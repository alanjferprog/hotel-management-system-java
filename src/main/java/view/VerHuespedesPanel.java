package view;

import model.entities.Huesped;
import controller.HuespedController;
import controller.HotelController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;

public class VerHuespedesPanel extends JPanel {
    private HotelController controlador;
    private HuespedController huespedController = new HuespedController();
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private JButton btnAgregar, btnModificar, btnEliminar;
    private Runnable onBack;

    public VerHuespedesPanel(HotelController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel(new Object[]{"Nombre","Apellido","DNI","Email","Telefono"},0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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
                List<Object[]> rows = huespedController.getHuespedRows(controlador);
                if (!rows.isEmpty()) {
                    for (Object[] row : rows) modelo.addRow(row);
                    return;
                }
                if (!retried) {
                    try {
                        huespedController.initializeAndLoad(controlador);
                        retried = true;
                        continue;
                    } catch (Exception ie) {
                        JOptionPane.showMessageDialog(this, "No se pudieron crear tablas o cargar huéspedes: " + ie.getMessage());
                        return;
                    }
                }
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudieron cargar huespedes: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private void onAgregar() {
        JTextField tfNombre = new JTextField();
        JTextField tfApellido = new JTextField();
        JTextField tfDni = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfTelefono = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Nombre:")); panel.add(tfNombre);
        panel.add(new JLabel("Apellido:")); panel.add(tfApellido);
        panel.add(new JLabel("DNI:")); panel.add(tfDni);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);
        panel.add(new JLabel("Telefono:")); panel.add(tfTelefono);
        int res = JOptionPane.showConfirmDialog(this, panel, "Agregar huésped", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        Huesped nuevo = new Huesped(tfNombre.getText().trim(), tfApellido.getText().trim(), tfDni.getText().trim(), tfEmail.getText().trim(), tfTelefono.getText().trim());
        try {
            huespedController.insertHuesped(controlador, nuevo);
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al insertar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
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
        JTextField tfNombre = new JTextField(nombre);
        JTextField tfApellido = new JTextField(apellido);
        JTextField tfDni = new JTextField(dni);
        JTextField tfEmail = new JTextField(email);
        JTextField tfTelefono = new JTextField(telefono);
        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Nombre:")); panel.add(tfNombre);
        panel.add(new JLabel("Apellido:")); panel.add(tfApellido);
        panel.add(new JLabel("DNI:")); panel.add(tfDni);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);
        panel.add(new JLabel("Telefono:")); panel.add(tfTelefono);
        int res = JOptionPane.showConfirmDialog(this, panel, "Modificar huésped", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        Huesped actualizado = new Huesped(tfNombre.getText().trim(), tfApellido.getText().trim(), tfDni.getText().trim(), tfEmail.getText().trim(), tfTelefono.getText().trim());
        try {
            huespedController.updateHuesped(controlador, actualizado);
            refresh();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEliminar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione un huésped"); return; }
        String dni = String.valueOf(modelo.getValueAt(sel,2));
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar huésped con DNI " + dni + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                huespedController.deleteHuesped(controlador, dni);
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
