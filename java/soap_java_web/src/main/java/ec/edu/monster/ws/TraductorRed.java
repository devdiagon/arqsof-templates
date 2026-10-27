package ec.edu.monster.ws;

import ec.edu.monster.excepciones.ExcepcionServicio;
import jakarta.xml.ws.WebServiceException;
import java.util.function.Supplier;

/**
 * Traduce los fallos de red y de protocolo del proxy generado a {@link ExcepcionServicio} con código {@code RED}.
 * Cada implementación de cliente ({@code XClienteWS}) envuelve así sus llamadas, de modo que las capas superiores
 * nunca ven una excepción de JAX-WS (red caída, tiempo agotado, Fault, respuesta que no es SOAP):
 *
 * <pre>
 * return TraductorRed.llamar(() -> convertir(puerto.operacion(parametro)));
 * </pre>
 *
 * Los errores de negocio no pasan por aquí: llegan como respuesta con {@code estado = ERROR} y los trata {@code ServicioBase}.
 */
public final class TraductorRed {

    private TraductorRed() {
    }

    public static <T> T llamar(Supplier<T> llamada) {
        try {
            return llamada.get();
        } catch (WebServiceException e) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED, e);
        }
    }
}
