package ec.edu.monster.excepciones;

/**
 * Catálogo de errores de negocio. Cada uno lleva su código textual y su mensaje por defecto.
 * Añada aquí los del dominio ({@code AUTH_001}, {@code DATA_002}, ...). {@code ERROR_INTERNO} es obligatorio:
 * traduce cualquier excepción no controlada sin filtrar detalles internos (regla R8).
 */
public enum CodigoError {

    DATO_001("DATO-001", "El dato recibido no es válido"),
    ERROR_INTERNO("ERR-500", "Error interno del servidor");

    private final String codigo;
    private final String mensaje;

    CodigoError(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
