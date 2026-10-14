package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

/** Crea el proxy JAX-WS del servicio a partir de {@link ConfiguracionServicio}. No contiene nada del dominio. */
public final class ClienteWS {

    private static final QName SERVICIO = new QName(ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_SERVICIO);
    private static final QName PUERTO = new QName(ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_PUERTO);

    private ClienteWS() {
    }

    /** Base configurada: propiedad {@code ws.base.url}, variable {@code WS_BASE_URL} o el valor por defecto. */
    public static String baseConfigurada() {
        String base = System.getProperty("ws.base.url");
        if (base == null || base.isBlank()) {
            base = System.getenv("WS_BASE_URL");
        }
        if (base == null || base.isBlank()) {
            base = ConfiguracionServicio.BASE_POR_DEFECTO;
        }
        return base.trim().replaceAll("/+$", "");
    }

    public static ContratoWS crear() {
        return crear(baseConfigurada());
    }

    /**
     * Descarga el WSDL (por eso conviene llamarlo fuera del hilo de eventos). Lanza una excepción de JAX-WS
     * ({@code WebServiceException}) si el servidor no responde.
     */
    public static ContratoWS crear(String baseUrl) {
        String endpoint = baseUrl + "/" + ConfiguracionServicio.NOMBRE_SERVICIO;
        try {
            URL wsdl = URI.create(endpoint + "?wsdl").toURL();
            ContratoWS puerto = Service.create(wsdl, SERVICIO).getPort(PUERTO, ContratoWS.class);
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
