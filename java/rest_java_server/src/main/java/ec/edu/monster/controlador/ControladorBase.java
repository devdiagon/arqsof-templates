package ec.edu.monster.controlador;

import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Único punto del servidor que captura excepciones. Ejecuta una operación y siempre devuelve
 * un Resultado: de éxito, de error de negocio (ExcepcionMonster con su código y mensaje) o,
 * ante cualquier otra excepción, ERR-500 sin exponer detalles internos.
 */
public abstract class ControladorBase {

    private static final Logger LOGGER = Logger.getLogger(ControladorBase.class.getName());

    @FunctionalInterface
    protected interface Operacion<T> {
        T ejecutar() throws Exception;
    }

    protected <T> Resultado<T> ejecutar(Operacion<T> operacion) {
        try {
            return Resultado.exito(operacion.ejecutar());
        } catch (ExcepcionMonster e) {
            return Resultado.error(e.getCodigo(), e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inesperado", e);
            return Resultado.error(CodigoError.ERR_500.getCodigo(), CodigoError.ERR_500.getMensaje());
        }
    }
}
