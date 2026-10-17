package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Fábrica genérica del puerto JAX-WS: lee el WSDL, fija el endpoint y los timeouts.
 * Cada proyecto aporta su interfaz de contrato (con @WebService) y el nombre del servicio.
 *
 * Convención (CONVENCION_SOAP.md):
 *  - R9: la URL del servicio se recibe COMPLETA por configuración y no se asume su forma. La ruta de un servidor Java
 *        (/Contexto/Servicio) y la de uno .NET (/RutaLibre) son distintas; cambiar de servidor es cambiar solo esta URL.
 *  - R10: el puerto se pide por la INTERFAZ ({@code getPort(contrato)}), nunca por el nombre del puerto, porque lo
 *        decide la plataforma del servidor (Metro: {@code <Servicio>Port}; CoreWCF: {@code <Binding>_<Contrato>}).
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

    /**
     * @param contrato       interfaz anotada con @WebService (con las anotaciones de la convención)
     * @param espacioNombres targetNamespace del servicio ({@link ec.edu.monster.dto.EspacioNombres#SERVICIO})
     * @param servicio       nombre del servicio en el WSDL ({@code <wsdl:service name="...">})
     */
    public static <T> T crear(Class<T> contrato, String espacioNombres, String servicio) {
        return crearConEndpoint(contrato, espacioNombres, servicio, urlConfigurada());
    }

    /** Igual que {@link #crear(Class, String, String)} pero con la URL completa del servicio indicada. */
    public static <T> T crearConEndpoint(Class<T> contrato, String espacioNombres, String servicio, String urlServicio) {
        String endpoint = normalizarUrl(urlServicio);
        try {
            URL wsdl = URI.create(endpoint + "?wsdl").toURL();
            T proxy = Service.create(wsdl, new QName(espacioNombres, servicio)).getPort(contrato);
            BindingProvider proveedor = (BindingProvider) proxy;
            // La dirección del soap:address del WSDL se ignora: manda la URL configurada.
            proveedor.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            proveedor.getRequestContext().put("com.sun.xml.ws.connect.timeout", TIMEOUT_MS);
            proveedor.getRequestContext().put("com.sun.xml.ws.request.timeout", TIMEOUT_MS);
            return proxy;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL del servicio invalida: " + endpoint, e);
        }
    }
}
