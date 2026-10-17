package ec.edu.monster.utilidades;

/**
 * Constantes del servidor. Los textos de éxito y el código "200" son parte de la respuesta uniforme (regla R6):
 * si otro servidor (p. ej. .NET) implementa el mismo servicio, los textos deben ser idénticos.
 */
public final class ConfiguracionServidor {

    public static final String ESTADO_OK = "OK";
    public static final String ESTADO_ERROR = "ERROR";
    public static final String CODIGO_OK = "200";
    public static final String MENSAJE_OK = "Operación realizada correctamente";

    private ConfiguracionServidor() {}
}
