package ec.edu.monster.api;

import ec.edu.monster.excepciones.ExcepcionServicio;

/**
 * Transporte HTTP hacia la API. Es una interfaz de un solo método para que las pruebas la sustituyan
 * por un doble (una lambda) que devuelve {@link RespuestaHttp} simuladas, sin servidor.
 */
@FunctionalInterface
public interface ClienteApi {

    String GET = "GET";
    String POST = "POST";

    /**
     * @param metodo     {@link #GET} o {@link #POST}
     * @param ruta       ruta relativa a la URL base, ya codificada (empieza con «/»)
     * @param cuerpoJson cuerpo JSON o {@code null} si no hay
     * @param token      token para la cabecera {@code Authorization: Bearer} o {@code null} si no hay sesión
     * @throws ExcepcionServicio con {@link ExcepcionServicio#RED} si no hay respuesta (servidor caído, red, timeout)
     */
    RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token);
}
