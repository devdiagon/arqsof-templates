package ec.edu.monster.api;

/**
 * UNICO lugar donde se configura el servidor REST. Al reutilizar la plantilla en otro dominio,
 * cambiar solo estas constantes (y el network_security_config.xml si cambia el host).
 *
 * @author: Frederick Tipán
 */
public final class ConfiguracionApi {

    /**
     * DOMINIO: URL base de la API (contexto de la aplicacion + prefijo de la API, sin "/" final).
     * Emulador: 10.0.2.2 es la PC anfitriona. Telefono real: IP de la PC en la red local
     * (y agregarla en res/xml/network_security_config.xml).
     */
    public static final String BASE_POR_DEFECTO = "http://10.0.2.2:8080/Contexto/api";

    /** DOMINIO: ruta (relativa a la base) del inicio de sesion: POST con {"usuario","contrasenia"}. */
    public static final String RUTA_LOGIN = "/auth/login";

    /** Tiempo maximo de conexion y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionApi() {
    }
}
