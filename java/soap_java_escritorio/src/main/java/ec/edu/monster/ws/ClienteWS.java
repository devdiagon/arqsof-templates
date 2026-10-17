package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Crea el proxy JAX-WS del servicio a partir de {@link ConfiguracionServicio}. No contiene nada del dominio.
 * El puerto se obtiene por la INTERFAZ del contrato (no por el nombre del puerto, que cambia entre Java y .NET)
 * y la URL del servicio se recibe completa.
 */
public final class ClienteWS {

    private ClienteWS() {
    }

    /** URL configurada: propiedad {@code ws.url}, variable {@code WS_URL} o el valor por defecto. */
    public static String urlConfigurada() {
        String url = System.getProperty("ws.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("WS_URL");
        }
        if (url == null || url.isBlank()) {
            url = ConfiguracionServicio.URL_POR_DEFECTO;
        }
        return normalizarUrl(url);
    }

    static String normalizarUrl(String url) {
        return url.trim().replaceAll("/+$", "");
    }

    public static ContratoWS crear() {
        return crear(urlConfigurada());
    }

    /**
     * Descarga el WSDL (por eso conviene llamarlo fuera del hilo de eventos). Lanza una excepción de JAX-WS
     * ({@code WebServiceException}) si el servidor no responde.
     */
    public static ContratoWS crear(String urlServicio) {
        String endpoint = normalizarUrl(urlServicio);
        try {
            URL wsdl = URI.create(endpoint + "?wsdl").toURL();
            QName servicio = new QName(ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_SERVICIO);
            ContratoWS puerto = Service.create(wsdl, servicio).getPort(ContratoWS.class);
            BindingProvider proveedor = (BindingProvider) puerto;
            proveedor.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            proveedor.getRequestContext().put("com.sun.xml.ws.connect.timeout", ConfiguracionServicio.TIMEOUT_MS);
            proveedor.getRequestContext().put("com.sun.xml.ws.request.timeout", ConfiguracionServicio.TIMEOUT_MS);
            return puerto;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL del servicio inválida: " + endpoint, e);
        }
    }
}
