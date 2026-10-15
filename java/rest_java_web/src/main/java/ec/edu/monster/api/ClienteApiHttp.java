package ec.edu.monster.api;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Implementación con {@link HttpClient} del JDK. Única clase que construye peticiones: pone {@code Accept},
 * {@code Content-Type} (si hay cuerpo) y {@code Authorization: Bearer} (si hay token). No contiene nada del dominio.
 */
public class ClienteApiHttp implements ClienteApi {

    private final String base;
    private final HttpClient cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(ConfiguracionApi.TIMEOUT_MS))
            .build();

    public ClienteApiHttp() {
        this(baseConfigurada());
    }

    public ClienteApiHttp(String base) {
        this.base = base.trim().replaceAll("/+$", "");
    }

    /** Base configurada: propiedad {@code api.base.url}, variable {@code API_BASE_URL} o el valor por defecto. */
    public static String baseConfigurada() {
        String base = System.getProperty("api.base.url");
        if (base == null || base.isBlank()) {
            base = System.getenv("API_BASE_URL");
        }
        if (base == null || base.isBlank()) {
            base = ConfiguracionApi.BASE_POR_DEFECTO;
        }
        return base.trim().replaceAll("/+$", "");
    }

    @Override
    public RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(base + ruta))
                .timeout(Duration.ofMillis(ConfiguracionApi.TIMEOUT_MS))
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
            HttpResponse<String> respuesta = cliente.send(peticion.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new RespuestaHttp(respuesta.statusCode(), respuesta.body());
        } catch (IOException e) {   // incluye ConnectException y los timeouts de conexión y de lectura
            throw ExcepcionServicio.servidorNoDisponible(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ExcepcionServicio.servidorNoDisponible(e);
        }
    }
}
