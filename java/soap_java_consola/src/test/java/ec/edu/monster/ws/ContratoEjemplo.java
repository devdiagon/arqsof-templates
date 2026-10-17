package ec.edu.monster.ws;

import ec.edu.monster.dto.EspacioNombres;
import ec.edu.monster.dto.RespuestaEcoDto;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Contrato de EJEMPLO (solo pruebas): muestra cómo se escribe el contrato real del proyecto.
 * Las tres declaraciones de la convención, en CADA operación:
 *  - action        = TNS + "<Servicio>/<operación>"  (regla R4: SOAPAction con URI)
 *  - @WebResult    con name = "respuesta" y targetNamespace = TNS (reglas R3 y R5)
 *  - @WebParam     con targetNamespace = TNS (regla R3)
 */
@WebService(name = "WSEjemplo", targetNamespace = EspacioNombres.SERVICIO)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface ContratoEjemplo {

    @WebMethod(operationName = "eco", action = EspacioNombres.SERVICIO + "WSEjemplo/eco")
    @WebResult(name = "respuesta", targetNamespace = EspacioNombres.SERVICIO)
    RespuestaEcoDto eco(@WebParam(name = "texto", targetNamespace = EspacioNombres.SERVICIO) String texto);
}
