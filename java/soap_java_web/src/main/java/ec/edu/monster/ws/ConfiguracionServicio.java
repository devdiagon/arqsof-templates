package ec.edu.monster.ws;

/**
 * ÚNICO lugar donde se configura el servidor SOAP. Al reutilizar la plantilla en otro dominio, cambiar solo estas constantes.
 * El namespace y el nombre del servicio ya no se escriben a mano: salen del WSDL de {@code src/main/resources/wsdl}.
 *
 * También se puede cambiar la URL sin recompilar: {@code -Dws.url=...} o la variable de entorno {@code WS_URL}.
 */
public final class ConfiguracionServicio {

    /**
     * DOMINIO: URL COMPLETA del servicio, tal como la entrega el servidor (host, puerto y ruta; sin «?wsdl», sin «/» final).
     * El cliente no asume puerto ni «base + servicio»: cada servidor (Java, .NET, IIS) publica una ruta distinta.
     */
    public static final String URL_POR_DEFECTO = "http://localhost:8080/CAMBIAR_CONTEXTO/CAMBIAR_SERVICIO";

    /** Tiempo máximo de conexión y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }
}
