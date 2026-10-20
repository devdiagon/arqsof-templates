package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import java.util.Map;

/**
 * Configuración genérica del proxy que genera {@code wsimport}: fija la URL completa del servicio y los timeouts.
 * No conoce el dominio: cada proyecto aporta su clase de servicio generada y su interfaz propia (ver README §4).
 *
 * <pre>
 * ClienteX cliente = new ClienteWSX(ClienteWS.configurar(new WSX_Service().getWSXPort()));
 * </pre>
 *
 * Convención (CONVENCION_SOAP.md):
 *  - R9: la URL del servicio se recibe COMPLETA por configuración y no se asume su forma. La ruta de un servidor Java
 *        (/Contexto/Servicio) y la de uno .NET (/RutaLibre) son distintas; cambiar de servidor es cambiar solo esta URL.
 *  - R10: el puerto se pide a la clase de servicio generada, nunca por el nombre del puerto, porque lo decide la plataforma
 *        del servidor (Metro: {@code <Servicio>Port}; CoreWCF: {@code <Binding>_<Contrato>}).
 *  - El WSDL va empaquetado en el classpath ({@code wsdlLocation} del proxy): NO se descarga al crear el puerto, así que crearlo
 *        funciona con el servidor apagado; el fallo llega al llamar a una operación, como {@code WebServiceException}.
 *
 * @author: Frederick Tipán
 */
public final class ClienteWS {

    /**
     * URL COMPLETA del servicio (sin "?wsdl"). CAMBIAR, o sobrescribir sin recompilar con -Dws.url=... o la variable
     * de entorno WS_URL. Ejemplos: Java http://localhost:8080/Contexto/Servicio · .NET http://localhost:5267/RutaServicio
     */
    public static final String URL_POR_DEFECTO = "http://localhost:8080/CAMBIAR_CONTEXTO/CAMBIAR_SERVICIO";
    private static final int TIMEOUT_MS = 10_000;

    private ClienteWS() {
    }

    /** Propiedad {@code ws.url}, si no la variable {@code WS_URL}, si no {@link #URL_POR_DEFECTO}. */
    public static String urlConfigurada() {
        String url = System.getProperty("ws.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("WS_URL");
        }
        if (url == null || url.isBlank()) {
            url = URL_POR_DEFECTO;
        }
        return normalizarUrl(url);
    }

    static String normalizarUrl(String url) {
        return url.trim().replaceAll("/+$", "");
    }

    /** Configura el puerto generado con la URL de {@link #urlConfigurada()}. */
    public static <P> P configurar(P puerto) {
        return configurar(puerto, urlConfigurada());
    }

    /**
     * Configura el puerto generado con la URL completa indicada y los timeouts.
     *
     * @param puerto      lo que devuelve {@code new X_Service().getXPort()} (implementa {@link BindingProvider})
     * @param urlServicio URL completa del servicio (Payara, .NET, IIS...); no se le añade ninguna ruta
     */
    public static <P> P configurar(P puerto, String urlServicio) {
        Map<String, Object> contexto = ((BindingProvider) puerto).getRequestContext();
        // La dirección del soap:address del WSDL se ignora: manda la URL configurada.
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, normalizarUrl(urlServicio));
        contexto.put("com.sun.xml.ws.connect.timeout", TIMEOUT_MS);
        contexto.put("com.sun.xml.ws.request.timeout", TIMEOUT_MS);
        return puerto;
    }
}
