package ec.edu.monster.controlador;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionNegocio;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Patrón común de los controladores (regla R8): ejecutar la operación y traducir cualquier excepción a una
 * respuesta de error. Nunca se propaga una excepción hacia el contrato SOAP (no hay SOAP Fault por errores de
 * negocio). El detalle de un error inesperado va al log; al cliente solo llega el mensaje genérico ERR-500.
 */
public abstract class ControladorBase {

    private static final Logger LOG = Logger.getLogger(ControladorBase.class.getName());

    protected <T extends RespuestaBaseDto> T ejecutar(Supplier<T> operacion, Function<ExcepcionNegocio, T> aError) {
        try {
            return operacion.get();
        } catch (ExcepcionNegocio e) {
            return aError.apply(e);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado al procesar la operación", e);
            return aError.apply(new ExcepcionNegocio(CodigoError.ERROR_INTERNO));
        }
    }
}
