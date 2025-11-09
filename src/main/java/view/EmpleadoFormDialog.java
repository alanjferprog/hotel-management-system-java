package view;

import model.entities.Empleado;

import javax.swing.*;
import java.awt.*;

public class EmpleadoFormDialog extends JDialog {
    private JTextField tfNombre, tfApellido, tfDni, tfCargo, tfTurno;
    private boolean ok = false;
    private Empleado existente;

    public EmpleadoFormDialog(Window owner, Empleado existente) {
        super(owner, "Empleado", ModalityType.APPLICATION_MODAL);
        this.existente = existente;
        setSize(400,250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        tfNombre = new JTextField();
        tfApellido = new JTextField();
        tfDni = new JTextField(); 
        tfCargo = new JTextField(); 
        tfTurno = new JTextField();
        
        // No pedir id: la BD generará el id automáticamente
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("DNI:")); form.add(tfDni);
        form.add(new JLabel("Cargo:")); form.add(tfCargo);
        form.add(new JLabel("Turno:")); form.add(tfTurno);
        add(form, BorderLayout.CENTER);

        if (existente != null) {
            // mantener id existente internamente
            tfNombre.setText(existente.getNombre());
            tfApellido.setText(existente.getApellido());
            tfDni.setText(existente.getDni()); tfDni.setEditable(false);
            tfCargo.setText(existente.getCargo());
            tfTurno.setText(existente.getTurno());
        }

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancelar");
        bottom.add(btnOk); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        btnOk.addActionListener(e -> {
            String err = validateInputs();
            if (err == null) {
                ok = true;
                setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, err, "Validación", JOptionPane.WARNING_MESSAGE);
            }
        });
        btnCancel.addActionListener(e -> { ok = false; setVisible(false); });
    }

    private String validateInputs() {
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String dni = tfDni.getText().trim();
        if (nombre.isEmpty()) return "El nombre es obligatorio.";
        if (apellido.isEmpty()) return "El apellido es obligatorio.";
        if (dni.isEmpty()) return "El DNI es obligatorio.";
        // validación básica de DNI: solo dígitos y entre 6 y 10 caracteres
        if (!dni.matches("\\d{6,10}")) return "El DNI debe contener solo dígitos (6-10).";
        return null;
    }

    public boolean isOk() { return ok; }
    public Empleado getEmpleado() { 
        int id = (existente != null) ? existente.getIdEmpleado() : 0;
        return new Empleado(
            id,
            tfNombre.getText().trim(),
            tfApellido.getText().trim(),
            tfDni.getText().trim(), 
            tfCargo.getText().trim(), 
            tfTurno.getText().trim()
        ); 
    }
}
