package ec.edu.monster.ws;

/**
 * ÚNICO lugar donde se configura el servidor SOAP. Al reutilizar la plantilla en otro dominio,
 * cambiar solo estas constantes (los valores salen del WSDL: {@code <URL>?wsdl}).
 *
 * También se puede cambiar la URL sin recompilar: {@code -Dws.url=...} o la variable de entorno {@code WS_URL}.
 */
public final class ConfiguracionServicio {

    /**
     * DOMINIO: URL COMPLETA del servicio, tal como la entrega el servidor (host, puerto y ruta; sin «?wsdl», sin «/» final).
     * El cliente no asume puerto ni «base + servicio»: cada servidor (Java, .NET, IIS) publica una ruta distinta.
     */
    public static final String URL_POR_DEFECTO = "http://localhost:8080/CAMBIAR_CONTEXTO/CAMBIAR_SERVICIO";

    /** DOMINIO: nombre del servicio: atributo {@code name} del {@code <service>} del WSDL. También es el nombre del portType (contrato). */
    public static final String NOMBRE_SERVICIO = "WSServicio";

    /**
     * DOMINIO: {@code targetNamespace} del servicio. Lo usan los DTO ({@code dto/package-info.java} y sus {@code @XmlType}),
     * el contrato ({@code targetNamespace} de {@code @WebParam}/{@code @WebResult}) y la SOAPAction ({@link #accion}).
     */
    public static final String NAMESPACE = "http://CAMBIAR.namespace/";

    /** Tiempo máximo de conexión y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }

    /** SOAPAction de una operación según la convención: {@code NAMESPACE + servicio + "/" + operación}. */
    public static String accion(String operacion) {
        return NAMESPACE + NOMBRE_SERVICIO + "/" + operacion;
    }
}
