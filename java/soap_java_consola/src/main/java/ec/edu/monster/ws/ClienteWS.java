package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 * Fábrica genérica del puerto JAX-WS: lee el WSDL, fija el endpoint y los timeouts.
 * Cada proyecto aporta su interfaz de contrato (con @WebService) y los nombres de servicio y puerto.
 *
 * @author: Frederick Tipán
 */
public final class ClienteWS {

    /** Cambiar al contexto del proyecto, o sobrescribir con -Dws.base.url / WS_BASE_URL. */
    public static final String BASE_POR_DEFECTO = "http://127.0.0.1:8080/CAMBIAR_CONTEXTO";
    private static final int TIMEOUT_MS = 10_000;

    private ClienteWS() {
    }

    public static String baseConfigurada() {
        String base = System.getProperty("ws.base.url");
        if (base == null || base.isBlank()) {
            base = System.getenv("WS_BASE_URL");
        }
        if (base == null || base.isBlank()) {
            base = BASE_POR_DEFECTO;
        }
        return quitarBarraFinal(base);
    }

    static String quitarBarraFinal(String base) {
        return base.trim().replaceAll("/+$", "");
    }

    /**
     * @param contrato       interfaz anotada con @WebService
     * @param espacioNombres targetNamespace del servicio
     * @param servicio       nombre del servicio en el WSDL; también es la ruta del endpoint bajo la base
     * @param puerto         nombre del puerto en el WSDL
     */
    public static <T> T crear(Class<T> contrato, String espacioNombres, String servicio, String puerto) {
        return crear(contrato, espacioNombres, servicio, puerto, baseConfigurada());
    }

    public static <T> T crear(Class<T> contrato, String espacioNombres, String servicio, String puerto, String baseUrl) {
        String endpoint = quitarBarraFinal(baseUrl) + "/" + servicio;
        try {
            URL wsdl = URI.create(endpoint + "?wsdl").toURL();
            T proxy = Service.create(wsdl, new QName(espacioNombres, servicio))
                    .getPort(new QName(espacioNombres, puerto), contrato);
            BindingProvider proveedor = (BindingProvider) proxy;
            proveedor.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
            proveedor.getRequestContext().put("com.sun.xml.ws.connect.timeout", TIMEOUT_MS);
            proveedor.getRequestContext().put("com.sun.xml.ws.request.timeout", TIMEOUT_MS);
            return proxy;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL del servicio invalida: " + endpoint, e);
        }
    }
}
