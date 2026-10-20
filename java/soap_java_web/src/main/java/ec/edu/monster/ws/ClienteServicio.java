package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaLoginDto;

/**
 * Operaciones del servicio SOAP que usa el cliente. Es lo ÚNICO que ven los servicios, los controladores y las vistas:
 * las clases que genera {@code wsimport} (paquete {@code ec.edu.monster.ws.generado}) quedan ocultas tras {@link ClienteWS}.
 * Los nombres de operación y de parámetro son los del servidor (se ven en el WSDL). Los tipos de retorno son los DTO propios
 * del cliente, no los generados.
 *
 * DOMINIO: agregar aquí una operación por cada una que use el cliente (ver README §4), por ejemplo:
 * <pre>
 * RespuestaListaDto obtenerItems(String token);
 * </pre>
 */
public interface ClienteServicio {

    RespuestaLoginDto login(String usuario, String contrasenia);
}
