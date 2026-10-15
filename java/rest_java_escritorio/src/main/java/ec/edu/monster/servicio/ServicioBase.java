package ec.edu.monster.servicio;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.RespuestaHttp;
import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Base de todos los servicios del cliente: comparten la {@link Sesion} (token), el transporte y la interpretación de las respuestas.
 * <ul>
 *   <li>Decide por el <b>código HTTP</b>: 2xx devuelve el DTO; cualquier otro lanza {@link ExcepcionServicio} con el código y el
 *       mensaje del cuerpo JSON (o uno genérico si el cuerpo no es JSON).</li>
 *   <li>Un {@code 401} en una operación protegida, o AUTH-002 / AUTH-003, descarta la sesión ({@code exigeLogin()}).</li>
 *   <li>Sin respuesta del servidor el transporte lanza {@link ExcepcionServicio#RED} ({@code servidorNoDisponible()}).</li>
 * </ul>
 *
 * DOMINIO: crear un servicio por área funcional que extienda esta clase y use {@link #get}, {@link #post} y {@link #codificar}.
 */
public abstract class ServicioBase {

    private static final Gson GSON = new Gson();

    protected final ClienteApi api;
    protected final Sesion sesion;

    protected ServicioBase(ClienteApi api, Sesion sesion) {
        this.api = api;
        this.sesion = sesion;
    }

    public boolean haySesion() {
        return sesion.getToken() != null;
    }

    public void cerrarSesion() {
        sesion.setToken(null);
    }

    /** Token de la sesión; sin sesión lanza AUTH-002 sin llamar al servidor. */
    protected String token() {
        if (sesion.getToken() == null) {
            throw new ExcepcionServicio("AUTH-002", "Debe iniciar sesión");
        }
        return sesion.getToken();
    }

    /** {@code GET} protegido (con token). */
    protected <T extends RespuestaBaseDto> T get(String ruta, Class<T> tipo) {
        return interpretar(api.enviar(ClienteApi.GET, ruta, null, token()), tipo, true);
    }

    /** {@code POST} protegido (con token). */
    protected <T extends RespuestaBaseDto> T post(String ruta, Object cuerpo, Class<T> tipo) {
        return interpretar(api.enviar(ClienteApi.POST, ruta, GSON.toJson(cuerpo), token()), tipo, true);
    }

    /** {@code POST} sin token (inicio de sesión). */
    protected <T extends RespuestaBaseDto> T postPublico(String ruta, Object cuerpo, Class<T> tipo) {
        return interpretar(api.enviar(ClienteApi.POST, ruta, GSON.toJson(cuerpo), null), tipo, false);
    }

    /** Codifica un segmento de la ruta (categorías, nombres, etc. pueden llevar tildes o espacios). */
    protected static String codificar(String segmento) {
        return URLEncoder.encode(segmento, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private <T extends RespuestaBaseDto> T interpretar(RespuestaHttp respuesta, Class<T> tipo, boolean protegida) {
        if (respuesta == null) {
            throw new ExcepcionServicio("SRV-000", "El servidor no devolvió respuesta");
        }
        if (respuesta.esExito()) {
            T dto = leer(respuesta.cuerpo(), tipo);
            if (dto == null) {
                throw new ExcepcionServicio("SRV-000", "El servidor no devolvió respuesta");
            }
            return dto;
        }
        RespuestaBaseDto error = leer(respuesta.cuerpo(), RespuestaBaseDto.class);
        boolean conMensaje = error != null && !esVacio(error.getMensaje());
        String codigo = error != null && !esVacio(error.getCodigo()) ? error.getCodigo() : "HTTP-" + respuesta.codigo();
        String mensaje = conMensaje ? error.getMensaje() : "Error del servidor (HTTP " + respuesta.codigo() + ")";
        ExcepcionServicio e = new ExcepcionServicio(codigo, mensaje);
        if (protegida && respuesta.codigo() == 401 && !e.exigeLogin()) {
            e = new ExcepcionServicio("AUTH-002", mensaje);
        }
        if (e.exigeLogin()) {
            sesion.setToken(null);
        }
        throw e;
    }

    /** Lee el JSON como {@code tipo}; devuelve {@code null} si el cuerpo está vacío o no es un objeto JSON. */
    private static <T> T leer(String cuerpo, Class<T> tipo) {
        if (esVacio(cuerpo)) {
            return null;
        }
        try {
            JsonElement json = GSON.fromJson(cuerpo, JsonElement.class);
            return json != null && json.isJsonObject() ? GSON.fromJson(json, tipo) : null;
        } catch (JsonParseException e) {
            return null;
        }
    }

    protected static boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
