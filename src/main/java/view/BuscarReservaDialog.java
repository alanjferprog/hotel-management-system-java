package view;

import model.entities.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Optional;

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
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnCerrar = new JButton("Cerrar");
        bottom.add(btnCerrar);
        add(bottom, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> onBuscar());
        btnCerrar.addActionListener(e -> setVisible(false));
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
                JOptionPane.showMessageDialog(this, "Reserva no encontrada", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Código inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
