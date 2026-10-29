package ec.edu.monster.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Transporte real con {@code java.net.http.HttpClient}. Única clase que abre conexiones: pone {@code Accept},
 * {@code Content-Type} y {@code Authorization} por petición (nunca como cabeceras por defecto) y no interpreta la
 * respuesta. Un solo {@code HttpClient} compartido por todo el programa.
 */
public final class TransporteHttp implements Transporte {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient CLIENTE = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private final String urlBase;

    public TransporteHttp(String urlBase) {
        this.urlBase = ConfiguracionApi.normalizarUrl(urlBase);
    }

    /** Transporte hacia la URL de {@link ConfiguracionApi#urlBase()}. */
    public static Transporte crear() {
        return new TransporteHttp(ConfiguracionApi.urlBase());
    }

    @Override
    public RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String autorizacion) throws IOException {
        try {
            HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json");
            if (autorizacion != null) {
                peticion.header("Authorization", autorizacion);
            }
            if (cuerpoJson != null) {
                peticion.header("Content-Type", "application/json; charset=UTF-8")
                        .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpoJson, StandardCharsets.UTF_8));
            } else {
                peticion.method(metodo, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> r = CLIENTE.send(peticion.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new RespuestaHttp(r.statusCode(), r.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Petición interrumpida", e);
        } catch (IllegalArgumentException e) {
            // URL base mal configurada: no hay a quién llamar
            throw new IOException("URL no válida: " + urlBase + ruta, e);
        }
    }
}
