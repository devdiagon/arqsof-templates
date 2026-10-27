package ec.edu.monster.ws;

/**
 * Datos fijos del transporte SOAP. No conoce el dominio.
 * La URL completa de cada servicio NO está aquí: es configuración y vive en res/values/config.xml
 * ({@code url_servicio}, o una {@code url_<servicio>} por servicio); si cambia el host, permitirlo también en
 * res/xml/network_security_config.xml. La implementación de cada cliente ({@code XClienteWS}) la recibe ya leída.
 *
 * @author: Frederick Tipán
 */
public final class ConfiguracionServicio {

    /** Namespace de destino de todos los servicios SOAP del equipo (R2 de docs/CONVENCION_SOAP.md, con barra final). */
    public static final String TNS = "http://ws.monster.edu.ec/";

    /** Tiempo máximo de conexión y de lectura, en milisegundos. */
    public static final int TIMEOUT_MS = 10_000;

    private ConfiguracionServicio() {
    }
}
