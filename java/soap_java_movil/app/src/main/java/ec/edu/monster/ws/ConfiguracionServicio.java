package ec.edu.monster.ws;

/**
 * UNICO lugar donde se configura el servidor SOAP. Al reutilizar la plantilla en otro dominio,
 * cambiar solo estas constantes (y el network_security_config.xml si cambia el host).
 *
 * @author: Frederick Tipán
 */
public final class ConfiguracionServicio {

    /**
     * DOMINIO: URL del endpoint (sin "?wsdl"). Emulador: 10.0.2.2 es la PC anfitriona.
     * Telefono real: IP de la PC en la red local (y agregarla en res/xml/network_security_config.xml).
     */
    public static final String URL_SERVICIO = "http://10.0.2.2:8080/WS_Servicio_SOAP/WSServicio";

    /** DOMINIO: targetNamespace del servicio (atributo targetNamespace del @WebService; sale en el WSDL). */
    public static final String NAMESPACE = "http://ws.monster.edu.ec/";

    /** Tiempo maximo de conexion y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }
}
