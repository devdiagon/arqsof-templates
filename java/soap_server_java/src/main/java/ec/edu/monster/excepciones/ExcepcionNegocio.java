package ec.edu.monster.excepciones;

/** Error controlado de negocio: sube hasta el controlador, que lo convierte en una respuesta de error. */
public class ExcepcionNegocio extends RuntimeException {

    private final CodigoError codigoError;

    public ExcepcionNegocio(CodigoError codigoError) {
        super(codigoError.getMensaje());
        this.codigoError = codigoError;
    }

    public ExcepcionNegocio(CodigoError codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }
}
