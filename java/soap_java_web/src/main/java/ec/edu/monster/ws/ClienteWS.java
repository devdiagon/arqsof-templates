package ec.edu.monster.ws;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.function.Supplier;
import javax.xml.namespace.QName;

/**
 * Crea el proxy JAX-WS del servicio a partir de {@link ConfiguracionServicio}. No contiene nada del dominio.
 *
 * En la web el WAR se despliega aunque el servidor SOAP esté apagado, y el WSDL se descarga al crear el proxy:
 * por eso {@link #compartido()} entrega un contrato que crea el proxy en la primera llamada que funcione
 * (y reintenta mientras falle). Es uno solo para toda la aplicación: el proxy de JAX-WS admite llamadas concurrentes.
 */
public final class ClienteWS {

    private static final QName SERVICIO = new QName(ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_SERVICIO);
    private static final QName PUERTO = new QName(ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_PUERTO);

    private static final ContratoWS COMPARTIDO = perezoso(ClienteWS::crear);

    private ClienteWS() {
    }

    /** Contrato único de la aplicación; los servicios lo reciben por constructor. */
    public static ContratoWS compartido() {
        return COMPARTIDO;
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

    /** Descarga el WSDL. Lanza {@code WebServiceException} (JAX-WS) si el servidor no responde. */
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

    /** Contrato que obtiene su destino la primera vez que se usa; si la creación falla, lo reintenta en la siguiente llamada. */
    static ContratoWS perezoso(Supplier<ContratoWS> fabrica) {
        return (ContratoWS) Proxy.newProxyInstance(
                ContratoWS.class.getClassLoader(), new Class<?>[]{ContratoWS.class}, new Perezoso(fabrica));
    }

    private static final class Perezoso implements InvocationHandler {

        private final Supplier<ContratoWS> fabrica;
        private volatile ContratoWS destino;

        Perezoso(Supplier<ContratoWS> fabrica) {
            this.fabrica = fabrica;
        }

        @Override
        public Object invoke(Object proxy, Method metodo, Object[] args) throws Throwable {
            try {
                return metodo.invoke(destino(), args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private ContratoWS destino() {
            ContratoWS actual = destino;
            if (actual == null) {
                synchronized (this) {
                    actual = destino;
                    if (actual == null) {
                        actual = fabrica.get();
                        destino = actual;
                    }
                }
            }
            return actual;
        }
    }
}
