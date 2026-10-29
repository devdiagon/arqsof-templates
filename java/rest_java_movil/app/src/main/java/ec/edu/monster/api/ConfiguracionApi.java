package ec.edu.monster.api;

/**
 * URL base completa de la API REST (R1 de docs/CONVENCION_REST.md): host, puerto, contexto y prefijo {@code api}, con
 * «/» final. Las rutas de las operaciones se le añaden relativas y sin barra inicial.
 *
 * <p>En Android no hay propiedades de sistema ni variables de entorno: el valor vive en {@code res/values/config.xml}
 * (único lugar donde se escribe) y {@code AplicacionCliente} lo entrega una sola vez con {@link #fijarUrlBase(String)}
 * antes de crear los clientes. Esta clase es Java puro para que se pueda probar sin Android.
 */
public final class ConfiguracionApi {

    private static volatile String urlBase;

    private ConfiguracionApi() {
    }

    /** Fija la URL base (la lee de {@code config.xml} quien compone la aplicación). */
    public static void fijarUrlBase(String url) {
        urlBase = normalizarUrl(url);
    }

    /** URL base normalizada; falla si la aplicación no la fijó (error de composición, no del usuario). */
    public static String urlBase() {
        String url = urlBase;
        if (url == null) {
            throw new IllegalStateException("La URL base de la API no fue fijada (ConfiguracionApi.fijarUrlBase)");
        }
        return url;
    }

    /** Sin espacios y con una sola «/» final. */
    static String normalizarUrl(String url) {
        return url.trim().replaceAll("/+$", "") + "/";
    }

    /** Solo para las pruebas: deja la configuración como al arrancar. */
    static void restablecer() {
        urlBase = null;
    }
}
