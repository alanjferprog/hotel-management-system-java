package view;

import model.entities.Habitacion;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

public class VerHabitacionesPanel extends JPanel {
    private ControladorGUI controlador;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private Runnable onBackCallback;
    private Consumer<Integer> onReservarCallback;

    public VerHabitacionesPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        // Modelo y tabla: columnas Número, Tipo, Precio, Estado
        modelo = new DefaultTableModel(new Object[] { "Número", "Tipo", "Precio", "Estado", "Acción" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Sólo la columna "Acción" (índice 4) es editable para que funcione el editor de botón
                return column == 4;
            }
        };
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowSelectionAllowed(true);

        // Añadir renderer/editor de botón en la última columna (Acción)
        tabla.getColumnModel().setColumnSelectionAllowed(false);
        tabla.setRowHeight(28);
        tabla.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tabla.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botón Volver siempre visible en la parte inferior
        btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> {
            if (onBackCallback != null) onBackCallback.run();
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.add(btnVolver);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    /**
     * Permite asignar la acción a ejecutar cuando se pulsa "Volver".
     */
    public void setOnBack(Runnable onBack) {
        this.onBackCallback = onBack;
    }

    /**
     * Asigna un callback que recibirá el número de habitación cuando se pulse "Reservar" en una fila.
     */
    public void setOnReservar(Consumer<Integer> onReservar) {
        this.onReservarCallback = onReservar;
    }

    public void refresh() {
        // Llenar el modelo de la tabla con las habitaciones
        List<Habitacion> habitaciones = controlador.getHotel().getHabitaciones();
        modelo.setRowCount(0);
        for (Habitacion h : habitaciones) {
            modelo.addRow(new Object[] { h.getNumero(), h.getTipo(), h.getPrecioPorNoche(), h.getEstado(), "Reservar" });
        }
    }

    // Renderer para botón
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String estado = String.valueOf(table.getValueAt(row, 3));
            boolean disponible = "disponible".equalsIgnoreCase(estado);
            setText(disponible ? (value == null ? "" : value.toString()) : "No disponible");
            setEnabled(disponible);
            return this;
        }
    }

    // Editor para botón que lanza la acción
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private String label;
        private int fila;

        public ButtonEditor(JCheckBox chk) {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = value == null ? "" : value.toString();
            // comprobar estado para habilitar/deshabilitar
            String estado = String.valueOf(table.getValueAt(row, 3));
            boolean disponible = "disponible".equalsIgnoreCase(estado);
            button.setText(disponible ? label : "No disponible");
            button.setEnabled(disponible);
            this.fila = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return label;
        }

        @Override
        public boolean isCellEditable(EventObject e) { return true; }

        @Override
        public void actionPerformed(ActionEvent e) {
            // obtener número de habitación de la fila y ejecutar callback
            int numero = (int) tabla.getValueAt(fila, 0);
            String estado = String.valueOf(tabla.getValueAt(fila, 3));
            boolean disponible = "disponible".equalsIgnoreCase(estado);
            if (!disponible) {
                // mostrar alerta modal
                Window w = SwingUtilities.getWindowAncestor(tabla);
                JOptionPane.showMessageDialog(w, "La habitación no está disponible para reservar.", "No disponible", JOptionPane.WARNING_MESSAGE);
                fireEditingStopped();
                return;
            }
            if (onReservarCallback != null) onReservarCallback.accept(numero);
            fireEditingStopped();
        }
    }
}
