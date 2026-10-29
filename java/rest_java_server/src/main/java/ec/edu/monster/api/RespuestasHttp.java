package ec.edu.monster.api;

import ec.edu.monster.api.contrato.ConvencionRest;
import ec.edu.monster.api.contrato.RespuestaBase;
import jakarta.ws.rs.core.Response;

/**
 * Construye la respuesta HTTP de un tipo de contrato: el HTTP sale de la categoría del error (R7)
 * y el cuerpo es JSON UTF-8 (R3).
 * <p>
 * Cada proyecto que defina autenticación agrega aquí lo que su mecanismo exija en un 401
 * (p. ej. la cabecera {@code WWW-Authenticate}); la plantilla no la fija (R8).
 */
public final class RespuestasHttp {

    private RespuestasHttp() {
    }

    public static Response.ResponseBuilder construir(RespuestaBase respuesta) {
        int http = CodigoHttp.deRespuesta(respuesta.getEstado(), respuesta.getCodigo());
        return Response.status(http)
                .type(ConvencionRest.JSON_UTF8)
                .entity(respuesta);
    }

    public static Response de(RespuestaBase respuesta) {
        return construir(respuesta).build();
    }
}
