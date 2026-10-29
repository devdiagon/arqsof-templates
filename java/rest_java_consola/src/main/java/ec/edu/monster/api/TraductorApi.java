package ec.edu.monster.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Arma las peticiones y lee las respuestas, sin tocar la red (se prueba con un {@link Transporte} falso). Es el
 * equivalente REST de {@code TraductorRed}: ninguna capa superior ve una excepción de HTTP o de JSON.
 * <ul>
 *   <li>Sin respuesta (servidor caído, red, tiempo agotado) → {@code RED}.</li>
 *   <li>Cuerpo vacío, que no es JSON o sin {@code estado} (página de un proxy, {@code 502}) → {@code SRV-000}.</li>
 *   <li>Se lee la envoltura también cuando el HTTP es de error: el {@code mensaje} del servidor nunca se descarta
 *       y decide {@code ServicioBase}.</li>
 * </ul>
 */
final class TraductorApi {

    private static final Gson GSON = new Gson();

    private TraductorApi() {
    }

    /** Codifica una variable de ruta (los espacios salen como %20, no como +). */
    static String codificar(String segmento) {
        return URLEncoder.encode(segmento, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Valor de la cabecera {@code Authorization} con las credenciales del empleado (R8: por petición). */
    static String autorizacionBasic(String usuario, String clave) {
        String credenciales = usuario + ":" + clave;
        return "Basic " + Base64.getEncoder().encodeToString(credenciales.getBytes(StandardCharsets.UTF_8));
    }

    /** Cuerpo JSON de un objeto con campos de texto: {@code cuerpo("importe", "100.50")}. Pares nombre, valor. */
    static String cuerpo(String... paresNombreValor) {
        JsonObject objeto = new JsonObject();
        for (int i = 0; i < paresNombreValor.length; i += 2) {
            objeto.addProperty(paresNombreValor[i], paresNombreValor[i + 1]);
        }
        return GSON.toJson(objeto);
    }

    /** Envía y lee: la operación completa de un método de la interfaz del cliente. */
    static <T extends RespuestaBaseDto> T llamar(Transporte transporte, String metodo, String ruta, String cuerpoJson,
            String autorizacion, Class<T> tipo) {
        RespuestaHttp respuesta;
        try {
            respuesta = transporte.enviar(metodo, ruta, cuerpoJson, autorizacion);
        } catch (IOException e) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED, e);
        }
        return leer(respuesta, tipo);
    }

    /** Convierte la respuesta HTTP en el DTO de la operación, sin decidir si es éxito o error de negocio. */
    static <T extends RespuestaBaseDto> T leer(RespuestaHttp respuesta, Class<T> tipo) {
        T dto = null;
        String cuerpo = respuesta.cuerpo();
        if (cuerpo != null && !cuerpo.isBlank()) {
            try {
                dto = GSON.fromJson(cuerpo, tipo);
            } catch (JsonParseException e) {
                dto = null;
            }
        }
        if (dto == null || dto.getEstado() == null) {
            throw new ExcepcionServicio(ExcepcionServicio.SRV_000,
                    ExcepcionServicio.MENSAJE_SRV_000 + " (HTTP " + respuesta.codigo() + ")");
        }
        return dto;
    }
}
