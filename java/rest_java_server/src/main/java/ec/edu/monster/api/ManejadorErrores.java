package ec.edu.monster.api;

import ec.edu.monster.api.contrato.ConvencionRest;
import ec.edu.monster.api.contrato.RespuestaBase;
import ec.edu.monster.dto.Resultado;
import ec.edu.monster.excepciones.CodigoError;
import ec.edu.monster.excepciones.ExcepcionMonster;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejador de errores de plataforma (R6): todo lo que el framework o el recurso lancen sale con la
 * envoltura estado/codigo/mensaje y el HTTP de la categoría, nunca con una página HTML.
 * Cubre ruta inexistente, verbo no permitido, Content-Type incorrecto, cuerpo mal formado y cualquier
 * excepción no prevista (ERR-500 sin detalles internos). No registra cabeceras ni cuerpos.
 * Debe llevar {@code @Provider} y estar en un paquete escaneado: si no, el servidor responde con su página HTML.
 */
@Provider
public class ManejadorErrores implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(ManejadorErrores.class.getName());

    @Override
    public Response toResponse(Throwable excepcion) {
        ExcepcionMonster error = clasificar(excepcion);
        RespuestaBase cuerpo = new RespuestaBase();
        cuerpo.aplicarEnvoltura(Resultado.Estado.ERROR.name(), error.getCodigo(), error.getMessage());
        Response.ResponseBuilder respuesta = RespuestasHttp.construir(cuerpo);
        if (excepcion instanceof NotAllowedException noPermitido) {
            String permitidos = noPermitido.getResponse().getHeaderString(HttpHeaders.ALLOW);
            if (permitidos != null) {
                respuesta.header(HttpHeaders.ALLOW, permitidos);
            }
        }
        return respuesta.build();
    }

    /** Traduce una excepción a la entrada del catálogo que le corresponde. */
    static ExcepcionMonster clasificar(Throwable excepcion) {
        if (excepcion instanceof ExcepcionMonster monster) {
            return monster;
        }
        if (excepcion instanceof NotFoundException) {
            return new ExcepcionMonster(CodigoError.ERR_404);
        }
        if (excepcion instanceof NotAllowedException) {
            return new ExcepcionMonster(CodigoError.ERR_405);
        }
        if (excepcion instanceof NotSupportedException) {
            return new ExcepcionMonster(CodigoError.ERR_415);
        }
        if (excepcion instanceof BadRequestException) {
            return new ExcepcionMonster(CodigoError.DATA_001, ConvencionRest.MENSAJE_CUERPO_INVALIDO);
        }
        LOGGER.log(Level.SEVERE, "Error inesperado en la API", excepcion);
        return new ExcepcionMonster(CodigoError.ERR_500);
    }
}
