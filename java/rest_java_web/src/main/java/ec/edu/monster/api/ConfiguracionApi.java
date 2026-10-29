package ec.edu.monster.api;

/**
 * URL base completa de la API REST (R1 de docs/CONVENCION_REST.md): host, puerto, contexto y prefijo {@code api}, con
 * «/» final. Las rutas de las operaciones se le añaden relativas y sin barra inicial. Es el único lugar donde vive.
 */
public final class ConfiguracionApi {

    /** CAMBIAR, o sobrescribir sin recompilar con -Dapi.base.url=... o la variable de entorno API_BASE_URL. */
    public static final String URL_BASE_POR_DEFECTO = "http://localhost:8080/CAMBIAR_CONTEXTO/api/";

    private ConfiguracionApi() {
    }

    /** Propiedad {@code api.base.url}, si no la variable {@code API_BASE_URL}, si no {@link #URL_BASE_POR_DEFECTO}. */
    public static String urlBase() {
        String url = System.getProperty("api.base.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("API_BASE_URL");
        }
        if (url == null || url.isBlank()) {
            url = URL_BASE_POR_DEFECTO;
        }
        return normalizarUrl(url);
    }

    /** Sin espacios y con una sola «/» final. */
    static String normalizarUrl(String url) {
        return url.trim().replaceAll("/+$", "") + "/";
    }
}
