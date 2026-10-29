package ec.edu.monster.ws.contrato;

/**
 * Constantes de la convención SOAP (CONVENCION_SOAP.md). El namespace de destino (TNS)
 * se declara solo aquí y todo lo demás lo referencia (R2).
 */
public final class ConvencionSoap {

    /** Namespace de destino de todos los servicios SOAP del equipo (R2). */
    public static final String TNS = "http://ws.monster.edu.ec/";

    /** EJEMPLO: nombre del servicio publicado. Sustituir por el del dominio. */
    public static final String SERVICIO_ELEMENTO = "ServicioElemento";

    /** Prefijo de la SOAPAction de cada servicio: TNS + servicio + "/" (R4). */
    public static final String ACCION_ELEMENTO = TNS + SERVICIO_ELEMENTO + "/";

    /** Nombre fijo del único elemento de retorno de toda operación (R5). */
    public static final String RETORNO = "respuesta";

    private ConvencionSoap() {
    }
}
