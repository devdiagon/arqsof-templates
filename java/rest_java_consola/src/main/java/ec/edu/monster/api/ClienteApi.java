package ec.edu.monster.api;

import java.io.IOException;

/**
 * Transporte HTTP hacia la API. Las pruebas lo sustituyen por un doble que devuelve {@link RespuestaHttp} simuladas.
 *
 * @author: Frederick Tipán
 */
public interface ClienteApi {

    /**
     * @param metodo      GET, POST…
     * @param ruta        relativa a la URL base, con el segmento variable ya codificado (p. ej. {@code /items})
     * @param cuerpoJson  cuerpo JSON, o {@code null} si la petición no lleva cuerpo
     * @param token       token Bearer, o {@code null} si no hay sesión
     * @throws IOException sin respuesta del servidor (conexión rechazada, timeout, sin red)
     */
    RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token) throws IOException;
}
