package ec.edu.monster.ws;

/**
 * Datos fijos del servicio SOAP. Al reutilizar la plantilla en otro dominio, cambiar estas constantes.
 * La URL del servicio NO esta aqui: es configuracion y vive en res/values/config.xml (url_servicio); si cambia el
 * host, permitirlo tambien en res/xml/network_security_config.xml.
 *
 * @author: Frederick Tipán
 */
public final class ConfiguracionServicio {

    /** DOMINIO: targetNamespace del servicio (atributo targetNamespace del @WebService; sale en el WSDL). */
    public static final String NAMESPACE = "http://CAMBIAR.namespace/";

    /** DOMINIO: nombre del servicio (atributo name del <service> del WSDL); forma la SOAPAction de cada operacion. */
    public static final String NOMBRE_SERVICIO = "WSServicio";

    /** Tiempo maximo de conexion y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }
}
