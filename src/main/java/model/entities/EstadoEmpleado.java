package model.entities;

//Usamos esta clase para definir los estados posibles de un empleado y evitar errores de tipeo
public enum EstadoEmpleado {
    DISPONIBLE("disponible", "Disponible"),
    OCUPADO("ocupado", "Ocupado");


    private final String dbValue;
    private final String label;

    EstadoEmpleado(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public String getDbValue() { return dbValue; }
    public String getLabel() { return label; }

    @Override
    public String toString() { return dbValue; }

    public static EstadoEmpleado fromString(String s) {
        if (s == null) return DISPONIBLE;
        String n = s.trim().toLowerCase();
        for (EstadoEmpleado e : values()) {
            if (e.dbValue.equalsIgnoreCase(n) || e.name().equalsIgnoreCase(n)) return e;
        }
        return DISPONIBLE;
    }
}
