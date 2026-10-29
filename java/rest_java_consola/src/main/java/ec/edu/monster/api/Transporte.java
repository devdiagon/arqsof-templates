package ec.edu.monster.api;

import java.io.IOException;

/**
 * Envía una petición HTTP y devuelve lo que responda el servidor, sea cual sea el código. Existe para separar el armado
 * y la lectura (probables sin red, ver {@link TraductorApi}) del envío real ({@link TransporteHttp}).
 */
public interface Transporte {

    /**
     * @param metodo       {@code GET} o {@code POST}
     * @param ruta         relativa a la URL base y sin barra inicial, con las variables ya codificadas
     * @param cuerpoJson   cuerpo JSON, o {@code null} si la petición no lleva
     * @param autorizacion valor completo de la cabecera {@code Authorization}, o {@code null} si no aplica
     * @throws IOException si no hay respuesta (servidor caído, red, tiempo agotado)
     */
    RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String autorizacion) throws IOException;
}
