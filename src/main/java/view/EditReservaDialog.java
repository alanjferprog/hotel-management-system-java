package view;

import model.entities.Reserva;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class EditReservaDialog extends JDialog {
    private ControladorGUI controlador;
    private Reserva reserva;

    private DatePicker dpInicio, dpFin;
    private JButton btnGuardar, btnCancelar;

    public EditReservaDialog(Frame owner, ControladorGUI controlador, Reserva reserva) {
        super(owner, "Editar Reserva", true);
        this.controlador = controlador;
        this.reserva = reserva;
        setSize(400, 220);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        form.add(new JLabel("ID:")); form.add(new JLabel(String.valueOf(reserva.getIdReserva())));
        form.add(new JLabel("Huésped:")); form.add(new JLabel(reserva.getHuesped().getNombre() + " " + reserva.getHuesped().getApellido()));
        form.add(new JLabel("Habitación:")); form.add(new JLabel(String.valueOf(reserva.getHabitacion().getNumero())));

        DatePickerSettings settingsInicio = new DatePickerSettings();
        settingsInicio.setFormatForDatesCommonEra("yyyy-MM-dd");
        // No permitir fechas pasadas
        settingsInicio.setDateRangeLimits(LocalDate.now(), null);
        dpInicio = new DatePicker(settingsInicio);
        dpInicio.setDate(reserva.getFechaInicio());

        DatePickerSettings settingsFin = new DatePickerSettings();
        settingsFin.setFormatForDatesCommonEra("yyyy-MM-dd");
        settingsFin.setDateRangeLimits(LocalDate.now(), null);
        dpFin = new DatePicker(settingsFin);
        dpFin.setDate(reserva.getFechaFin());

        form.add(new JLabel("Fecha Inicio:")); form.add(dpInicio);
        form.add(new JLabel("Fecha Fin:")); form.add(dpFin);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");
        bottom.add(btnGuardar); bottom.add(btnCancelar);
        add(bottom, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> setVisible(false));

        btnGuardar.addActionListener(e -> onGuardar());
    }

    private void onGuardar() {
        LocalDate inicio = dpInicio.getDate();
        LocalDate fin = dpFin.getDate();
        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(this, "Seleccione ambas fechas.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!fin.isAfter(inicio)) {
            JOptionPane.showMessageDialog(this, "La fecha de fin debe ser posterior a la fecha de inicio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Actualizar campos privados de Reserva usando reflection
            java.lang.reflect.Field fInicio = Reserva.class.getDeclaredField("fechaInicio");
            fInicio.setAccessible(true);
            fInicio.set(reserva, inicio);
            java.lang.reflect.Field fFin = Reserva.class.getDeclaredField("fechaFin");
            fFin.setAccessible(true);
            fFin.set(reserva, fin);

            // Persistir en BD
            controlador.guardarReservaEnDB(reserva);
            JOptionPane.showMessageDialog(this, "Reserva actualizada correctamente.", "OK", JOptionPane.INFORMATION_MESSAGE);
            setVisible(false);
        } catch (NoSuchFieldException | IllegalAccessException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar cambios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

