package view;

import model.entities.Empleado;

import javax.swing.*;
import java.awt.*;

public class EmpleadoFormDialog extends JDialog {
    private JTextField tfNombre, tfApellido, tfDni, tfCargo, tfTurno;
    private JSpinner spIdEmpleado;
    private boolean ok = false;

    public EmpleadoFormDialog(Window owner, Empleado existente) {
        super(owner, "Empleado", ModalityType.APPLICATION_MODAL);
        setSize(400,250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        spIdEmpleado = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        tfNombre = new JTextField(); 
        tfApellido = new JTextField(); 
        tfDni = new JTextField(); 
        tfCargo = new JTextField(); 
        tfTurno = new JTextField();
        
        form.add(new JLabel("Id:")); form.add(spIdEmpleado);
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("DNI:")); form.add(tfDni);
        form.add(new JLabel("Cargo:")); form.add(tfCargo);
        form.add(new JLabel("Turno:")); form.add(tfTurno);
        add(form, BorderLayout.CENTER);

        if (existente != null) {
            spIdEmpleado.setValue(existente.getIdEmpleado());
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

        btnOk.addActionListener(e -> { ok = true; setVisible(false); });
        btnCancel.addActionListener(e -> { ok = false; setVisible(false); });
    }

    public boolean isOk() { return ok; }
    public Empleado getEmpleado() { 
        return new Empleado(
            (int)spIdEmpleado.getValue(),
            tfNombre.getText().trim(), 
            tfApellido.getText().trim(), 
            tfDni.getText().trim(), 
            tfCargo.getText().trim(), 
            tfTurno.getText().trim()
        ); 
    }
}

