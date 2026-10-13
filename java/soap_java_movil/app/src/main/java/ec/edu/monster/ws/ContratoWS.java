package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaLoginDto;

/**
 * Operaciones del servicio SOAP. Los nombres de operacion y de parametro coinciden con el contrato del
 * servidor; los implementa ClienteSoapHttp. Las implementaciones traducen los fallos de comunicacion
 * (red, timeout, Fault) a ExcepcionServicio con codigo RED.
 *
 * DOMINIO: agregar aqui una firma por cada operacion del servidor (ver README.md, "Agregar una operacion").
 *
 * @author: Frederick Tipán
 */
public interface ContratoWS {

    /** Compartida: todos los servidores de esta familia exponen login(usuario, contrasenia) -> token. */
    RespuestaLoginDto login(String usuario, String contrasenia);
}
