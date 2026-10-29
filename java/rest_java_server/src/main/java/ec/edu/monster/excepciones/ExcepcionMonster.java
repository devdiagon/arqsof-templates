package ec.edu.monster.excepciones;

/**
 * Única excepción de negocio. Transporta una entrada del catálogo y, opcionalmente,
 * un mensaje específico que reemplaza al mensaje por defecto.
 */
public class ExcepcionMonster extends RuntimeException {

    private final CodigoError error;

    public ExcepcionMonster(CodigoError error) {
        super(error.getMensaje());
        this.error = error;
    }

    public ExcepcionMonster(CodigoError error, String mensaje) {
        super(mensaje);
        this.error = error;
    }

    public CodigoError getError() {
        return error;
    }

    public String getCodigo() {
        return error.getCodigo();
    }

    public CodigoError.Categoria getCategoria() {
        return error.getCategoria();
    }
}
