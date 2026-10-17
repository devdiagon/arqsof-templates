package ec.edu.monster.ws;

import ec.edu.monster.controlador.EjemploControlador;
import ec.edu.monster.dto.EspacioNombres;
import ec.edu.monster.dto.RespuestaEcoDto;
import ec.edu.monster.dto.RespuestaListaDto;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * Punto de entrada SOAP: sin lógica, solo delega en el controlador. Un solo servicio (una URL, un WSDL).
 *
 * Convención (CONVENCION_SOAP.md), en CADA operación:
 *  - {@code @WebMethod(action = TNS + "<Servicio>/<operación>")}   -> SOAPAction con URI (R4); JAX-WS usa "" por defecto.
 *  - {@code @WebResult(name = "respuesta", targetNamespace = TNS)} -> retorno único calificado (R3, R5).
 *  - {@code @WebParam(name = "...", targetNamespace = TNS)}        -> parámetros calificados (R3).
 * Los nombres (serviceName, operationName, name) forman parte del contrato (R5): no se renombran sin regenerar clientes.
 */
@WebService(serviceName = "WSEjemplo", targetNamespace = EspacioNombres.SERVICIO)
public class WSEjemplo {

    @Inject
    private EjemploControlador controlador;

    @WebMethod(operationName = "eco", action = EspacioNombres.SERVICIO + "WSEjemplo/eco")
    @WebResult(name = "respuesta", targetNamespace = EspacioNombres.SERVICIO)
    public RespuestaEcoDto eco(
            @WebParam(name = "texto", targetNamespace = EspacioNombres.SERVICIO) String texto) {
        return controlador.eco(texto);
    }

    @WebMethod(operationName = "listar", action = EspacioNombres.SERVICIO + "WSEjemplo/listar")
    @WebResult(name = "respuesta", targetNamespace = EspacioNombres.SERVICIO)
    public RespuestaListaDto listar() {
        return controlador.listar();
    }
}
