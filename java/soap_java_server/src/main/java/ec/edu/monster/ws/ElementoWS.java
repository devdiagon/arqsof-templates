package ec.edu.monster.ws;

import ec.edu.monster.controlador.ElementoControlador;
import ec.edu.monster.ws.contrato.ConvencionSoap;
import ec.edu.monster.ws.contrato.RespuestaElemento;
import ec.edu.monster.ws.contrato.RespuestaElementos;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * EJEMPLO de endpoint SOAP publicado como ServicioElemento. Solo recibe, delega y devuelve.
 * En CADA operación se conservan las tres declaraciones de la convención: {@code action} con URI (R4),
 * {@code @WebResult} llamado "respuesta" (R5) y {@code @WebParam} calificados con el TNS (R3).
 */
@WebService(name = ConvencionSoap.SERVICIO_ELEMENTO,
        serviceName = ConvencionSoap.SERVICIO_ELEMENTO,
        portName = ConvencionSoap.SERVICIO_ELEMENTO + "Port",
        targetNamespace = ConvencionSoap.TNS)
public class ElementoWS {

    @Inject
    private ElementoControlador controlador;

    public ElementoWS() {
    }

    /** Para pruebas, sin contenedor. */
    public ElementoWS(ElementoControlador controlador) {
        this.controlador = controlador;
    }

    @WebMethod(operationName = "consultarElemento",
            action = ConvencionSoap.ACCION_ELEMENTO + "consultarElemento")
    @WebResult(name = ConvencionSoap.RETORNO, targetNamespace = ConvencionSoap.TNS)
    public RespuestaElemento consultarElemento(
            @WebParam(name = "codigoElemento", targetNamespace = ConvencionSoap.TNS) String codigoElemento) {
        return ElementoMapeador.aRespuestaElemento(controlador.consultar(codigoElemento));
    }

    @WebMethod(operationName = "listarElementos",
            action = ConvencionSoap.ACCION_ELEMENTO + "listarElementos")
    @WebResult(name = ConvencionSoap.RETORNO, targetNamespace = ConvencionSoap.TNS)
    public RespuestaElementos listarElementos() {
        return ElementoMapeador.aRespuestaElementos(controlador.listar());
    }

    @WebMethod(operationName = "registrarElemento",
            action = ConvencionSoap.ACCION_ELEMENTO + "registrarElemento")
    @WebResult(name = ConvencionSoap.RETORNO, targetNamespace = ConvencionSoap.TNS)
    public RespuestaElemento registrarElemento(
            @WebParam(name = "codigoElemento", targetNamespace = ConvencionSoap.TNS) String codigoElemento,
            @WebParam(name = "nombre", targetNamespace = ConvencionSoap.TNS) String nombre,
            @WebParam(name = "valor", targetNamespace = ConvencionSoap.TNS) String valor) {
        return ElementoMapeador.aRespuestaElemento(controlador.registrar(codigoElemento, nombre, valor));
    }
}
