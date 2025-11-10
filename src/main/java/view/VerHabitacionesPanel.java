package view;

import model.entities.Habitacion;
import model.entities.EstadoHabitacion;

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
import java.sql.SQLException;

public class VerHabitacionesPanel extends JPanel {
    private ControladorGUI controlador;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JButton btnVolver;
    private JButton btnDarDeBaja;
    private Runnable onBackCallback;
    private Consumer<Integer> onReservarCallback;
    private Consumer<Integer> onPedirLimpiezaCallback;

    public VerHabitacionesPanel(ControladorGUI controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());
        // Modelo y tabla: columnas Número, Tipo, Precio, Estado, Empleado asignado, Acción, Alta
        modelo = new DefaultTableModel(new Object[] { "Número", "Tipo", "Precio", "Estado", "Empleado asignado", "Acción", "Alta" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Columnas 5 (Acción) y 6 (Alta) son editables para botones
                return column == 5 || column == 6;
            }
        };
        tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setRowSelectionAllowed(true);

        // Añadir renderer/editor de botón en las columnas (Acción = 4, Alta = 5)
        tabla.getColumnModel().setColumnSelectionAllowed(false);
        tabla.setRowHeight(28);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        tabla.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        tabla.getColumnModel().getColumn(6).setCellRenderer(new AltaButtonRenderer());
        tabla.getColumnModel().getColumn(6).setCellEditor(new AltaButtonEditor(new JCheckBox()));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Botones abajo: Volver y Dar de baja
        btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> {
            if (onBackCallback != null) onBackCallback.run();
        });

        btnDarDeBaja = new JButton("Dar de baja");
        btnDarDeBaja.addActionListener(e -> onDarDeBaja());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.add(btnVolver);
        bottom.add(Box.createHorizontalStrut(8));
        bottom.add(btnDarDeBaja);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private void onDarDeBaja() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una habitación para dar de baja.", "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int numero = (int) modelo.getValueAt(sel, 0);
        int r = JOptionPane.showConfirmDialog(this, "Dar de baja la habitación " + numero + " (marcar como en_reparacion)?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        // Pedir asignación a personal de mantenimiento (opcional)
        java.util.List<model.entities.Empleado> empleados = controlador.getHotel().getEmpleados();
        java.util.List<model.entities.Empleado> mantenimiento = new java.util.ArrayList<>();
        for (model.entities.Empleado emp : empleados) {
            if (emp.getCargo() != null && emp.getCargo().toLowerCase().contains("mantenimiento") && controlador.estaEmpleadoEnTurno(emp)) mantenimiento.add(emp);
        }
        String[] opciones;
        if (mantenimiento.isEmpty()) {
            opciones = new String[] { "No asignar" };
        } else {
            opciones = new String[mantenimiento.size() + 1];
            opciones[0] = "No asignar";
            for (int i = 0; i < mantenimiento.size(); i++) opciones[i+1] = mantenimiento.get(i).getNombre() + " " + mantenimiento.get(i).getApellido();
        }
        String elegido = (String) JOptionPane.showInputDialog(this, "Asignar tarea de reparación a:", "Asignar mantenimiento", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);

        // Si se seleccionó un empleado de mantenimiento, marcarlo como ocupado y persistir
        if (elegido != null && !"No asignar".equals(elegido) && !mantenimiento.isEmpty()) {
            int idx = java.util.Arrays.asList(opciones).indexOf(elegido) - 1; // opciones[0] = No asignar
            if (idx >= 0 && idx < mantenimiento.size()) {
                model.entities.Empleado emp = mantenimiento.get(idx);
                try {
                    controlador.asignarEmpleadoAHabitacion(numero, emp.getDni());
                } catch (java.sql.SQLException ex) {
                    JOptionPane.showMessageDialog(this, "No se pudo asignar el empleado a la habitacion: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        // Marcar habitacion como en_reparacion en memoria y persistir
        controlador.getHotel().buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEstado(EstadoHabitacion.EN_REPARACION));
        try {
            controlador.actualizarEstadoHabitacionEnDB(numero, EstadoHabitacion.EN_REPARACION.getDbValue());
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar BD: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
        }
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

    /**
     * Asigna un callback que recibirá el número de habitación cuando se pulse "Pedir limpieza" en una fila.
     */
    public void setOnPedirLimpieza(Consumer<Integer> onPedirLimpieza) {
        this.onPedirLimpiezaCallback = onPedirLimpieza;
    }

    public void refresh() {
        // Llenar el modelo de la tabla con las habitaciones
        List<Habitacion> habitaciones = controlador.getHotel().getHabitaciones();
        modelo.setRowCount(0);
        for (Habitacion h : habitaciones) {
            String accion = "Reservar";
            EstadoHabitacion estado = h.getEstado();
            if (EstadoHabitacion.PENDIENTE_LIMPIEZA.equals(estado)) {
                accion = "Pedir limpieza"; // acción disponible para solicitar limpieza
            } else if (EstadoHabitacion.LIMPIEZA_PEDIDA.equals(estado)) {
                accion = "Limpieza pedida"; // ya solicitada -> deshabilitar botón
            } else if (EstadoHabitacion.EN_REPARACION.equals(estado)) {
                accion = "No disponible";
            } else if (!EstadoHabitacion.DISPONIBLE.equals(estado)) {
                // ocupado u otros estados
                accion = "No disponible";
            }
            String alta = (EstadoHabitacion.DISPONIBLE.equals(estado)) ? "" : "Dar de alta";
            // buscar nombre del empleado asignado (si hay dni)
            String empleadoNombre = "";
            String dniAsignado = h.getEmpleadoAsignado();
            if (dniAsignado != null && !dniAsignado.isBlank()) {
                for (model.entities.Empleado emp : controlador.getHotel().getEmpleados()) {
                    if (emp.getDni() != null && emp.getDni().equals(dniAsignado)) {
                        empleadoNombre = emp.getNombre() + " " + emp.getApellido();
                        break;
                    }
                }
            }
            modelo.addRow(new Object[] { h.getNumero(), h.getTipo(), h.getPrecioPorNoche(), estado.getLabel(), empleadoNombre, accion, alta });
        }
    }

    // Renderer para botón principal (Acción)
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String estadoLabel = String.valueOf(table.getValueAt(row, 3));
            // convertir label a enum para decidir
            EstadoHabitacion estado = EstadoHabitacion.fromString(estadoLabel);
            boolean disponible = EstadoHabitacion.DISPONIBLE.equals(estado);
            boolean pendiente = EstadoHabitacion.PENDIENTE_LIMPIEZA.equals(estado);
            boolean pedida = EstadoHabitacion.LIMPIEZA_PEDIDA.equals(estado);
            String val = value == null ? "" : value.toString();
            if (pedida) {
                setText("Limpieza pedida");
                setEnabled(false);
            } else if (pendiente) {
                setText("Pedir limpieza");
                setEnabled(true);
            } else {
                setText(disponible ? val : "No disponible");
                setEnabled(disponible);
            }
            return this;
        }
    }

    // Editor para botón principal que lanza la acción (Acción)
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
            String estadoLabel = String.valueOf(table.getValueAt(row, 3));
            EstadoHabitacion estado = EstadoHabitacion.fromString(estadoLabel);
            boolean disponible = EstadoHabitacion.DISPONIBLE.equals(estado);
            boolean pendiente = EstadoHabitacion.PENDIENTE_LIMPIEZA.equals(estado);
            boolean pedida = EstadoHabitacion.LIMPIEZA_PEDIDA.equals(estado);
            if (pedida) {
                button.setText("Limpieza pedida");
                button.setEnabled(false);
            } else if (pendiente) {
                button.setText("Pedir limpieza");
                button.setEnabled(true);
            } else {
                button.setText(disponible ? label : "No disponible");
                button.setEnabled(disponible);
            }
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
            int numero = (int) tabla.getValueAt(fila, 0);
            String estadoLabel = String.valueOf(tabla.getValueAt(fila, 3));
            EstadoHabitacion estado = EstadoHabitacion.fromString(estadoLabel);
            boolean disponible = EstadoHabitacion.DISPONIBLE.equals(estado);
            boolean pendiente = EstadoHabitacion.PENDIENTE_LIMPIEZA.equals(estado);
            boolean pedida = EstadoHabitacion.LIMPIEZA_PEDIDA.equals(estado);
            if (pedida) {
                // ya fue pedida, no hacemos nada
                Window w = SwingUtilities.getWindowAncestor(tabla);
                JOptionPane.showMessageDialog(w, "La limpieza ya fue pedida para esta habitación.", "Información", JOptionPane.INFORMATION_MESSAGE);
                fireEditingStopped();
                return;
            }
            if (pendiente) {
                if (onPedirLimpiezaCallback != null) onPedirLimpiezaCallback.accept(numero);
                fireEditingStopped();
                return;
            }
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

    // Renderer para botón de "Dar de alta"
    private class AltaButtonRenderer extends JButton implements TableCellRenderer {
        public AltaButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String estadoLabel = String.valueOf(table.getValueAt(row, 3));
            EstadoHabitacion estado = EstadoHabitacion.fromString(estadoLabel);
            if (EstadoHabitacion.DISPONIBLE.equals(estado)) {
                setText("");
                setEnabled(false);
            } else {
                setText("Dar de alta");
                setEnabled(true);
            }
            return this;
        }
    }

    // Editor para botón de "Dar de alta"
    private class AltaButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private String label;
        private int fila;

        public AltaButtonEditor(JCheckBox chk) {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = value == null ? "" : value.toString();
            String estadoLabel = String.valueOf(table.getValueAt(row, 3));
            EstadoHabitacion estado = EstadoHabitacion.fromString(estadoLabel);
            if (EstadoHabitacion.DISPONIBLE.equals(estado)) {
                button.setText("");
                button.setEnabled(false);
            } else {
                button.setText("Dar de alta");
                button.setEnabled(true);
            }
            this.fila = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() { return label; }

        @Override
        public boolean isCellEditable(EventObject e) { return true; }

        @Override
        public void actionPerformed(ActionEvent e) {
            int numero = (int) tabla.getValueAt(fila, 0);
            controlador.getHotel().buscarHabitacionPorNumero(numero).ifPresent(h -> h.setEstado(EstadoHabitacion.DISPONIBLE));
            try {
                controlador.actualizarEstadoHabitacionEnDB(numero, EstadoHabitacion.DISPONIBLE.getDbValue());
                // liberar empleado asignado (si corresponde)
                try {
                    controlador.liberarEmpleadoPorHabitacion(numero);
                    // refrescar panel de empleados si está visible
                } catch (java.sql.SQLException ex2) {
                    // mostrar pero no detener
                    JOptionPane.showMessageDialog(VerHabitacionesPanel.this, "Aviso: no se pudo liberar empleado asignado: " + ex2.getMessage(), "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(VerHabitacionesPanel.this, "Error al actualizar BD: " + ex.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
            refresh();
            // intentar refrescar panel de empleados (si la UI principal lo tiene)
            Window w = SwingUtilities.getWindowAncestor(VerHabitacionesPanel.this);
            if (w instanceof MainFrame) {
                ((MainFrame) w).refresh(); // esto refresca VerHabitacionesPanel actualmente, pero empleado panel se refresca cuando se abre
            }
            fireEditingStopped();
        }
    }
}
