package ec.edu.monster.ws;

/**
 * ÚNICO lugar donde se configura el servidor SOAP. Al reutilizar la plantilla en otro dominio,
 * cambiar solo estas constantes (los valores salen del WSDL: {@code <URL>?wsdl}).
 *
 * También se puede cambiar la URL base sin recompilar: {@code -Dws.base.url=...} o la variable de entorno {@code WS_BASE_URL}.
 */
public final class ConfiguracionServicio {

    /** DOMINIO: URL base de la aplicación en el servidor (sin el nombre del servicio, sin «?wsdl», sin «/» final). */
    public static final String BASE_POR_DEFECTO = "http://127.0.0.1:8080/WS_Servicio_SOAP";

    /** DOMINIO: nombre del servicio: atributo {@code name} del {@code <service>} del WSDL; también es el último tramo de la URL del endpoint. */
    public static final String NOMBRE_SERVICIO = "WSServicio";

    /** DOMINIO: nombre del puerto: atributo {@code name} del {@code <port>} del WSDL (suele ser el servicio + «Port»). */
    public static final String NOMBRE_PUERTO = "WSServicioPort";

    /**
     * DOMINIO: {@code targetNamespace} del servicio. Los tipos de respuesta (DTO) usan el mismo
     * (si el WSDL declara otro para los tipos, cambiar el de cada DTO).
     */
    public static final String NAMESPACE = "http://ws.monster.edu.ec/";

    /** Tiempo máximo de conexión y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }
}
