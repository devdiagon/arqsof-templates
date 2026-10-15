package ec.edu.monster.api;

/**
 * ÚNICO lugar donde se configura el servidor REST. Al reutilizar la plantilla en otro dominio,
 * cambiar solo estas constantes.
 *
 * También se puede cambiar la URL base sin recompilar: {@code -Dapi.base.url=...} o la variable de entorno {@code API_BASE_URL}.
 */
public final class ConfiguracionApi {

    /** DOMINIO: URL base de la API (contexto de la aplicación + prefijo de la API, sin «/» final). */
    public static final String BASE_POR_DEFECTO = "http://127.0.0.1:8080/Contexto/api";

    /** DOMINIO: ruta (relativa a la base) del inicio de sesión: {@code POST} con {@code {"usuario","contrasenia"}}. */
    public static final String RUTA_LOGIN = "/auth/login";

    /** Tiempo máximo de conexión y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionApi() {
    }
}
