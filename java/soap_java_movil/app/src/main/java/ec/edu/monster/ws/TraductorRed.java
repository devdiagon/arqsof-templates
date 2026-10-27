package ec.edu.monster.ws;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.IOException;

/**
 * Traduce los fallos de red del transporte a {@link ExcepcionServicio} con código {@code RED}. {@code ClienteSoapHttp}
 * envuelve así su llamada HTTP, de modo que las capas superiores nunca ven una excepción de entrada/salida
 * (servidor caído, sin red, tiempo agotado):
 *
 * <pre>
 * return TraductorRed.llamar(() -> enviarPorHttp(cuerpo));
 * </pre>
 *
 * Los errores de negocio no pasan por aquí: llegan como respuesta con {@code estado = ERROR} y los trata {@code ServicioBase}.
 */
public final class TraductorRed {

    /** Llamada de red que puede fallar con una excepción de entrada/salida. */
    public interface Llamada<T> {
        T ejecutar() throws IOException;
    }

    private TraductorRed() {
    }

    public static <T> T llamar(Llamada<T> llamada) {
        try {
            return llamada.ejecutar();
        } catch (IOException e) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED, e);
        }
    }
}
