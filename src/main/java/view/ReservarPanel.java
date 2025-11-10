package view;

import model.entities.*;
import model.exceptions.*;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.sql.SQLException; // añadido
import controller.ReservaController;
import controller.HabitacionController;
import controller.HotelController;

public class ReservarPanel extends JPanel {
    private HotelController controlador;
    private JTextField tfNombre, tfApellido, tfDni, tfEmail, tfTelefono, tfNumeroHab;
    // Usamos DatePicker (LGoodDatePicker)
    private DatePicker dpInicio, dpFin;
    private JButton btnReservar;
    private JButton btnVolver;
    private Runnable onBackCallback;

    // Tabla de reservas
    private JTable tablaReservas;
    private javax.swing.table.DefaultTableModel modeloReservas;
    private JTextField tfBuscarId;
    private JButton btnBuscar, btnMostrarTodos;
    private ReservaController reservaController = new ReservaController();
    private HabitacionController habitacionController = new HabitacionController();

    public ReservarPanel(HotelController controlador) {
        this.controlador = controlador;
        setLayout(new BorderLayout());

        // Left: formulario
        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        tfNombre = new JTextField();
        tfApellido = new JTextField();
        tfDni = new JTextField();
        tfEmail = new JTextField();
        tfTelefono = new JTextField();
        tfNumeroHab = new JTextField();

        // DatePicker settings
        DatePickerSettings settingsInicio = new DatePickerSettings();
        settingsInicio.setFormatForDatesCommonEra("yyyy-MM-dd");
        dpInicio = new DatePicker(settingsInicio);
        dpInicio.setDate(LocalDate.now());

        DatePickerSettings settingsFin = new DatePickerSettings();
        settingsFin.setFormatForDatesCommonEra("yyyy-MM-dd");
        dpFin = new DatePicker(settingsFin);
        dpFin.setDate(LocalDate.now().plusDays(1));

        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("DNI:")); form.add(tfDni);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Telefono:")); form.add(tfTelefono);
        form.add(new JLabel("Número Hab:")); form.add(tfNumeroHab);
        form.add(new JLabel("Fecha Inicio:")); form.add(dpInicio);
        form.add(new JLabel("Fecha Fin:")); form.add(dpFin);

        btnReservar = new JButton("Crear Reserva");
        btnReservar.addActionListener(this::onReservar);

        // Botón Volver local
        btnVolver = new JButton("Volver");
        btnVolver.addActionListener(e -> {
            if (onBackCallback != null) onBackCallback.run();
        });

        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        bottom.add(btnReservar);
        bottom.add(Box.createHorizontalStrut(12));
        bottom.add(btnVolver);
        leftWrapper.add(bottom, BorderLayout.SOUTH);

        // Right: tabla de reservas + búsqueda
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel topSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfBuscarId = new JTextField(8);
        btnBuscar = new JButton("Buscar por ID");
        btnMostrarTodos = new JButton("Mostrar todos");
        topSearch.add(new JLabel("ID:")); topSearch.add(tfBuscarId); topSearch.add(btnBuscar); topSearch.add(btnMostrarTodos);
        rightPanel.add(topSearch, BorderLayout.NORTH);

        modeloReservas = new javax.swing.table.DefaultTableModel(new Object[]{"ID","Huésped","Hab","Inicio","Fin","Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        // Ocultar la columna ID en la vista pero mantenerla en el modelo para búsquedas/operaciones
        if (tablaReservas.getColumnModel().getColumnCount() > 0) {
            try {
                tablaReservas.removeColumn(tablaReservas.getColumnModel().getColumn(0));
            } catch (Exception ex) {
                System.err.println("No se pudo ocultar columna ID en ReservarPanel: " + ex.getMessage());
            }
        }
        tablaReservas.setFillsViewportHeight(true);
        rightPanel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        // Acciones de búsqueda
        btnBuscar.addActionListener(e -> onBuscarReserva());
        btnMostrarTodos.addActionListener(e -> refreshReservations());

        // Split pane: formulario a la izquierda, reservas a la derecha
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftWrapper, rightPanel);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        // Inicializar tabla de reservas
        refreshReservations();
    }

    /** Prefill número de habitación (utilizado cuando se reserva desde la tabla). */
    public void setNumeroHabitacion(int numero) {
        tfNumeroHab.setText(String.valueOf(numero));
        // Bloquear edición para evitar cambiar el número cuando se preselecciona desde la tabla
        tfNumeroHab.setEditable(false);
        // focus en fecha inicio spinner
        dpInicio.requestFocusInWindow();
    }

    /** Permite asignar acción al botón Volver dentro del panel. */
    public void setOnBack(Runnable onBack) {
        this.onBackCallback = onBack;
    }

    private LocalDate dateFromPicker(DatePicker dp) {
        return dp.getDate();
    }

    private void limpiarFormulario() {
        tfNombre.setText("");
        tfApellido.setText("");
        tfDni.setText("");
        tfEmail.setText("");
        tfTelefono.setText("");
        // Limpiar y desbloquear el campo de número de habitación
        tfNumeroHab.setText("");
        tfNumeroHab.setEditable(true);
        // reset datepickers a valores por defecto
        dpInicio.setDate(LocalDate.now());
        dpFin.setDate(LocalDate.now().plusDays(1));
    }

    private void onReservar(ActionEvent ev) {
        try {
            String nombre = tfNombre.getText().trim();
            String apellido = tfApellido.getText().trim();
            String dni = tfDni.getText().trim();
            String email = tfEmail.getText().trim();
            String telefono = tfTelefono.getText().trim();
            int numeroHab = Integer.parseInt(tfNumeroHab.getText().trim());

            LocalDate inicio = dateFromPicker(dpInicio);
            LocalDate fin = dateFromPicker(dpFin);

            // Validaciones básicas
            if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre, apellido y DNI son obligatorios", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (inicio == null || fin == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar fechas de inicio y fin", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (fin.isBefore(inicio) || fin.isEqual(inicio)) {
                JOptionPane.showMessageDialog(this, "La fecha de fin debe ser posterior a la fecha de inicio", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Huesped h = new Huesped(nombre, apellido, dni, email, telefono);

            Empleado e = controlador.getHotel().getEmpleados().isEmpty()
                    ? new Empleado(1, "Admin", "Admin", "00000000", "Reception", "AM")
                    : controlador.getHotel().getEmpleados().get(0);

            // Si el hotel no tiene empleados, añadir el empleado por defecto para que las reservas queden ligadas
            if (controlador.getHotel().getEmpleados().isEmpty()) {
                controlador.getHotel().agregarEmpleado(e);
            }

            // Usar HabitacionController para crear y persistir la reserva (MVC: vista -> controller -> ControladorGUI)
            try {
                Reserva r = habitacionController.crearYGuardarReserva(controlador, inicio, fin, numeroHab, h, e);
                // Actualizar tabla de reservas
                refreshReservations();

                // Mensaje de éxito y volver al inicio
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Reserva realizada con éxito, su nro de reserva es: #" + r.getIdReserva(),
                            "Reserva exitosa",
                            JOptionPane.INFORMATION_MESSAGE);
                    java.awt.Window w = SwingUtilities.getWindowAncestor(this);
                    if (w instanceof MainFrame) {
                        ((MainFrame) w).refresh();
                        ((MainFrame) w).showInicio();
                    }
                });

                // limpiar formulario tras el éxito
                limpiarFormulario();
            } catch (HabitacionNoDisponibleException | ReservaInvalidaException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "No es posible reservar", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException sqle) {
                JOptionPane.showMessageDialog(this, "Error al guardar en DB: " + sqle.getMessage(), "Error BD", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Número de habitación inválido", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (HabitacionNoDisponibleException | ReservaInvalidaException ex) {
            // Mostrar alerta modal para estos errores
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No es posible reservar", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(this, iae.getMessage(), "Fecha inválida", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error inesperado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /** Carga todas las reservas desde la BD (si es posible) y rellena la tabla. */
    private void refreshReservations() {
        modeloReservas.setRowCount(0);
        try {
            // Delegar a ReservaController: inicializar y obtener filas (usa memoria o BD según corresponda)
            try {
                reservaController.initializeAndLoad(controlador);
            } catch (Exception initEx) {
                // advertir pero continuar con lo que haya en memoria
                System.err.println("Advertencia: no se pudo inicializar la BD: " + initEx.getMessage());
            }

            var rows = reservaController.getReservationRows(controlador);
            if (!rows.isEmpty()) {
                for (Object[] row : rows) modeloReservas.addRow(row);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudieron cargar reservas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onBuscarReserva() {
        String s = tfBuscarId.getText().trim();
        if (s.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese un ID"); return; }
        try {
            int id = Integer.parseInt(s);
            // Buscar usando ReservaController (primero memoria, luego BD)
            var optRow = reservaController.findReservationRowById(id, controlador);
            if (optRow.isPresent()) {
                modeloReservas.setRowCount(0);
                modeloReservas.addRow(optRow.get());
            } else {
                JOptionPane.showMessageDialog(this, "Reserva no encontrada: " + id);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "ID inválido", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
