package ec.edu.monster.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Implementación con {@code java.net.http.HttpClient}. Única clase que construye peticiones:
 * pone {@code Accept}, {@code Content-Type} y {@code Authorization}.
 *
 * @author: Frederick Tipán
 */
public final class ClienteApiHttp implements ClienteApi {

    /** Cambiar al contexto del proyecto, o sobrescribir con -Dapi.base.url / API_BASE_URL. */
    public static final String BASE_POR_DEFECTO = "http://localhost:8080/WR_ConUni_Java_REST/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String baseUrl;
    private final HttpClient cliente;

    public ClienteApiHttp() {
        this(baseConfigurada());
    }

    public ClienteApiHttp(String baseUrl) {
        this.baseUrl = quitarBarraFinal(baseUrl);
        this.cliente = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    }

    public static String baseConfigurada() {
        String base = System.getProperty("api.base.url");
        if (base == null || base.isBlank()) {
            base = System.getenv("API_BASE_URL");
        }
        if (base == null || base.isBlank()) {
            base = BASE_POR_DEFECTO;
        }
        return quitarBarraFinal(base);
    }

    static String quitarBarraFinal(String base) {
        return base.trim().replaceAll("/+$", "");
    }

    /** Codifica un segmento de ruta (los espacios salen como %20, no como +). */
    public static String codificarSegmento(String segmento) {
        return URLEncoder.encode(segmento, StandardCharsets.UTF_8).replace("+", "%20");
    }

    @Override
    public RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token) throws IOException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(TIMEOUT)
                .header("Accept", "application/json");
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        if (cuerpoJson != null) {
            peticion.header("Content-Type", "application/json; charset=UTF-8")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpoJson, StandardCharsets.UTF_8));
        } else {
            peticion.method(metodo, HttpRequest.BodyPublishers.noBody());
        }
        try {
            HttpResponse<String> r = cliente.send(peticion.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new RespuestaHttp(r.statusCode(), r.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        }
    }
}
