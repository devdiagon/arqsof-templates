package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaLoginDto;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Contrato del servicio SOAP: una interfaz con las operaciones que usa el cliente (JAX-WS la convierte en un proxy).
 * Los nombres de operación y de parámetro son EXACTAMENTE los del servidor (se ven en el WSDL) y van en su orden.
 *
 * DOMINIO: agregar aquí una operación por cada una que use el cliente (ver README §4), por ejemplo:
 * <pre>
 * &#64;WebMethod(operationName = "obtenerItems")
 * &#64;WebResult(name = "respuesta")
 * RespuestaListaDto obtenerItems(&#64;WebParam(name = "token") String token);
 * </pre>
 */
@WebService(name = ConfiguracionServicio.NOMBRE_SERVICIO, targetNamespace = ConfiguracionServicio.NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface ContratoWS {

    @WebMethod(operationName = "login")
    @WebResult(name = "respuesta")
    RespuestaLoginDto login(
            @WebParam(name = "usuario") String usuario,
            @WebParam(name = "contrasenia") String contrasenia);
}
