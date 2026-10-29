package ec.edu.monster.excepciones;

/**
 * Catálogo de errores del servidor. Cada entrada define código, mensaje por defecto y categoría.
 * Un código nunca se reutiliza con otro significado.
 * Núcleo: DATA_001 y ERR_500 (no se quitan). Las entradas ELE_* son de EJEMPLO: sustituirlas por las del dominio.
 */
public enum CodigoError {

    DATA_001("DATA-001", "Dato de entrada inválido", Categoria.VALIDACION),

    ELE_001("ELE-001", "El elemento no existe", Categoria.NO_ENCONTRADO),
    ELE_002("ELE-002", "El elemento ya existe", Categoria.CONFLICTO),

    ERR_500("ERR-500", "Error interno del servidor", Categoria.TECNICO);

    public enum Categoria {
        VALIDACION, NO_ENCONTRADO, CONFLICTO, OPERACION_INVALIDA, NO_AUTORIZADO, TECNICO
    }

    private final String codigo;
    private final String mensaje;
    private final Categoria categoria;

    CodigoError(String codigo, String mensaje, Categoria categoria) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.categoria = categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
