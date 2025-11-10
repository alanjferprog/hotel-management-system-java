package model.entities;

public enum EstadoHabitacion {
    DISPONIBLE("disponible", "Disponible"),
    OCUPADA("ocupada", "Ocupada"),
    PENDIENTE_LIMPIEZA("pendiente_limpieza", "Pendiente limpieza"),
    LIMPIEZA_PEDIDA("limpieza_pedida", "Limpieza pedida"),
    EN_REPARACION("en_reparacion", "En reparación"),
    NO_DISPONIBLE("no_disponible", "No disponible");

    private final String dbValue;
    private final String label;

    EstadoHabitacion(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public String getDbValue() { return dbValue; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return dbValue; }

    public static EstadoHabitacion fromString(String s) {
        if (s == null) return NO_DISPONIBLE;
        String normalized = s.trim().toLowerCase().replace(' ', '_');
        for (EstadoHabitacion e : values()) {
            if (e.dbValue.equalsIgnoreCase(normalized) || e.name().equalsIgnoreCase(normalized)) return e;
        }
        // also handle cases like "no disponible" (with space)
        if ("no disponible".equalsIgnoreCase(s.trim())) return NO_DISPONIBLE;
        // fallback
        return NO_DISPONIBLE;
    }
}
