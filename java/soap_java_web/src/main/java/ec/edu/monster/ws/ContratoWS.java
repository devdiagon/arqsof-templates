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
 * En CADA operación van las tres declaraciones de la convención: {@code action} (SOAPAction, R4) y
 * {@code targetNamespace} en {@code @WebResult} y en cada {@code @WebParam} (R3).
 *
 * DOMINIO: agregar aquí una operación por cada una que use el cliente (ver README §4), por ejemplo:
 * <pre>
 * &#64;WebMethod(operationName = "obtenerItems", action = NAMESPACE + NOMBRE_SERVICIO + "/obtenerItems")
 * &#64;WebResult(name = "respuesta", targetNamespace = ConfiguracionServicio.NAMESPACE)
 * RespuestaListaDto obtenerItems(&#64;WebParam(name = "token", targetNamespace = ConfiguracionServicio.NAMESPACE) String token);
 * </pre>
 */
@WebService(name = ConfiguracionServicio.NOMBRE_SERVICIO, targetNamespace = ConfiguracionServicio.NAMESPACE)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface ContratoWS {

    @WebMethod(operationName = "login", action = ConfiguracionServicio.NAMESPACE + ConfiguracionServicio.NOMBRE_SERVICIO + "/login")
    @WebResult(name = "respuesta", targetNamespace = ConfiguracionServicio.NAMESPACE)
    RespuestaLoginDto login(
            @WebParam(name = "usuario", targetNamespace = ConfiguracionServicio.NAMESPACE) String usuario,
            @WebParam(name = "contrasenia", targetNamespace = ConfiguracionServicio.NAMESPACE) String contrasenia);
}
