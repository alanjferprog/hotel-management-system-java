package view;

import model.entities.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Optional;
import controller.DatabaseInitializer;
import bdd.ConexionSQLite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BuscarReservaDialog extends JDialog {
    private ControladorGUI controlador;
    private JTextField tfCodigo;
    private JButton btnBuscar;
    private JButton btnCerrar;
    private DefaultTableModel modelo;
    private JTable tabla;

    public BuscarReservaDialog(Frame owner, ControladorGUI controlador) {
        super(owner, "Buscar Reserva", true);
        this.controlador = controlador;
        setSize(700, 300);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tfCodigo = new JTextField(10);
        btnBuscar = new JButton("Buscar");
        top.add(new JLabel("Código reserva:"));
        top.add(tfCodigo);
        top.add(btnBuscar);
        add(top, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"ID","Huésped","Hab","Inicio","Fin","Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        // Ocultar la columna ID en la vista pero mantenerla en el modelo
        if (tabla.getColumnModel().getColumnCount() > 0) {
            try { tabla.removeColumn(tabla.getColumnModel().getColumn(0)); } catch (Exception ex) { System.err.println("No se pudo ocultar columna ID en BuscarReservaDialog: " + ex.getMessage()); }
        }
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnEditar = new JButton("Editar");
        btnCerrar = new JButton("Cerrar");
        bottom.add(btnEditar);
        bottom.add(btnCerrar);
        add(bottom, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> onBuscar());
        btnCerrar.addActionListener(e -> setVisible(false));
        btnEditar.addActionListener(e -> onEditar());

        // Cargar todas las reservas al abrir
        loadAllReservations();
    }

    private void loadAllReservations() {
        modelo.setRowCount(0);
        // Asegurar BD (crear tablas si es la primera vez)
        try {
            try { DatabaseInitializer.initialize(); } catch (Exception ex) { System.err.println("BD init warning: " + ex.getMessage()); }

            int habCount = 0, resCount = 0;
            try {
                habCount = controlador.cargarHabitacionesDesdeDB();
                System.out.println("BuscarReservaDialog: habitaciones cargadas=" + habCount);
            } catch (SQLException ex) {
                System.err.println("BuscarReservaDialog: no se pudieron cargar habitaciones: " + ex.getMessage());
            }
            try {
                resCount = controlador.cargarReservasDesdeDB();
                System.out.println("BuscarReservaDialog: reservas cargadas=" + resCount);
            } catch (SQLException ex) {
                System.err.println("BuscarReservaDialog: no se pudieron cargar reservas: " + ex.getMessage());
            }

            // Diagnóstico directo: COUNT(*) en la tabla reserva
            try (Connection c = ConexionSQLite.conectar(); PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) AS cnt FROM reserva"); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    System.out.println("BuscarReservaDialog: COUNT(reserva) = " + cnt);
                }
            } catch (Exception ex) {
                System.err.println("BuscarReservaDialog: no se pudo contar filas en reserva: " + ex.getMessage());
            }

            // Llenar desde memoria si existe
            if (!controlador.getHotel().getReservas().isEmpty()) {
                for (Reserva r : controlador.getHotel().getReservas()) {
                    modelo.addRow(new Object[]{r.getIdReserva(), r.getHuesped().getNombre() + " " + r.getHuesped().getApellido(), r.getHabitacion().getNumero(), r.getFechaInicio(), r.getFechaFin(), r.getEstado()});
                }
                return;
            }

            // Fallback: leer directamente la tabla reserva y mostrar resultados aunque no se puedan mapear a objetos completos
            try (Connection conn = ConexionSQLite.conectar()) {
                String sql = "SELECT id, nombre, apellido, numeroHab, fechaInicio, fechaFin, estado FROM reserva ORDER BY id";
                try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        int id = rs.getInt("id");
                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        int numeroHab = rs.getInt("numeroHab");
                        String inicio = rs.getString("fechaInicio");
                        String fin = rs.getString("fechaFin");
                        String estado = rs.getString("estado");
                        modelo.addRow(new Object[]{id, (nombre==null?"":nombre) + " " + (apellido==null?"":apellido), numeroHab, inicio, fin, estado});
                    }
                    if (!any) System.out.println("BuscarReservaDialog: no hay filas en tabla reserva (fallback)");
                }
            } catch (Exception ex) {
                System.err.println("BuscarReservaDialog: error fallback BD: " + ex.getMessage());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error cargando reservas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean addReservationFromDbById(int id) {
        try (Connection conn = ConexionSQLite.conectar()) {
            String sql = "SELECT id, nombre, apellido, numeroHab, fechaInicio, fechaFin, estado FROM reserva WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String nombre = rs.getString("nombre");
                        String apellido = rs.getString("apellido");
                        int numeroHab = rs.getInt("numeroHab");
                        String inicio = rs.getString("fechaInicio");
                        String fin = rs.getString("fechaFin");
                        String estado = rs.getString("estado");
                        modelo.addRow(new Object[]{id, (nombre==null?"":nombre) + " " + (apellido==null?"":apellido), numeroHab, inicio, fin, estado});
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("BuscarReservaDialog: error buscando reserva por id en BD: " + ex.getMessage());
        }
        return false;
    }

    private void onBuscar() {
        modelo.setRowCount(0);
        String txt = tfCodigo.getText().trim();
        if (txt.isEmpty()) return;
        try {
            int id = Integer.parseInt(txt);
            Optional<Reserva> res = controlador.buscarReservaPorId(id);
            if (res.isPresent()) {
                Reserva r = res.get();
                modelo.addRow(new Object[]{r.getIdReserva(), r.getHuesped(), r.getHabitacion().getNumero(), r.getFechaInicio(), r.getFechaFin(), r.getEstado()});
            } else {
                // intentar fallback a BD directa
                boolean found = addReservationFromDbById(id);
                if (!found) JOptionPane.showMessageDialog(this, "Reserva no encontrada", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Código inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEditar() {
        int sel = tabla.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Seleccione una reserva para editar"); return; }
        // Id está en el modelo original en la columna 0
        int id = (int) modelo.getValueAt(sel, 0);
        try {
            controlador.cargarReservasDesdeDB();
        } catch (SQLException ignore) {}
        var opt = controlador.buscarReservaPorId(id);
        if (opt.isPresent()) {
            EditReservaDialog dlg = new EditReservaDialog((Frame) SwingUtilities.getWindowAncestor(this), controlador, opt.get());
            dlg.setVisible(true);
            // tras cerrar, recargar la tabla
            loadAllReservations();
        } else {
            JOptionPane.showMessageDialog(this, "Reserva no encontrada en memoria para editar.");
        }
    }
}
