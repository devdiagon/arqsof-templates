package ec.edu.monster.excepciones;

/**
 * Única excepción del cliente. Lleva el código (del servidor o propio del cliente) y el mensaje que se mostrará al usuario.
 *
 * @author: Frederick Tipán
 */
public class ExcepcionServicio extends RuntimeException {

    /** Códigos propios del cliente: nunca aparecen en el catálogo de errores del servidor. */
    public static final String RED = "RED";
    public static final String SRV_000 = "SRV-000";

    public static final String MENSAJE_RED = "Servidor no disponible, intente más tarde";
    public static final String MENSAJE_SRV_000 = "No se pudo interpretar la respuesta del servidor";

    private final String codigo;

    public ExcepcionServicio(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ExcepcionServicio(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    /** Sin respuesta del servidor: caído, sin red, tiempo agotado o Fault. */
    public boolean servidorNoDisponible() {
        return RED.equals(codigo);
    }

    /** Respuesta que no se puede interpretar (vacía, nula o que no es el XML esperado). */
    public boolean respuestaIlegible() {
        return SRV_000.equals(codigo);
    }
}
