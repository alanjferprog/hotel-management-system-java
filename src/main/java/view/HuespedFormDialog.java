package view;

import model.entities.Huesped;

import javax.swing.*;
import java.awt.*;

public class HuespedFormDialog extends JDialog {
    private JTextField tfNombre, tfApellido, tfDni, tfEmail, tfTelefono;
    private boolean ok = false;

    public HuespedFormDialog(Window owner, Huesped existente) {
        super(owner, "Huésped", ModalityType.APPLICATION_MODAL);
        setSize(400,250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        tfNombre = new JTextField(); tfApellido = new JTextField(); tfDni = new JTextField(); tfEmail = new JTextField(); tfTelefono = new JTextField();
        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("DNI:")); form.add(tfDni);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Telefono:")); form.add(tfTelefono);
        add(form, BorderLayout.CENTER);

        if (existente != null) {
            tfNombre.setText(existente.getNombre());
            tfApellido.setText(existente.getApellido());
            tfDni.setText(existente.getDni()); tfDni.setEditable(false);
            tfEmail.setText(existente.getEmail());
            tfTelefono.setText(existente.getTelefono());
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
    public Huesped getHuesped() { return new Huesped(tfNombre.getText().trim(), tfApellido.getText().trim(), tfDni.getText().trim(), tfEmail.getText().trim(), tfTelefono.getText().trim()); }
}

