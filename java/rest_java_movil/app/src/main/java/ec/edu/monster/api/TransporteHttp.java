package ec.edu.monster.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Transporte real con {@link HttpURLConnection} (Android no incluye {@code java.net.http}). Única clase que abre
 * conexiones: pone {@code Accept}, {@code Content-Type} y {@code Authorization} por petición (nunca como cabeceras por
 * defecto) y no interpreta la respuesta. Debe invocarse fuera del hilo principal.
 *
 * <p>Se lee el cuerpo también cuando el HTTP es de error (el servidor siempre manda la envoltura). El cuerpo se envía
 * sin modo de transmisión fija: el servidor responde 401 con {@code WWW-Authenticate} y, con el cuerpo en
 * transmisión, {@code HttpURLConnection} podría fallar al intentar reenviarlo en lugar de entregar la respuesta.
 */
public final class TransporteHttp implements Transporte {

    private static final int TIMEOUT_MS = 10_000;

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
        // Una URL mal escrita sale como MalformedURLException, que es una IOException: se traduce a RED
        HttpURLConnection conexion = (HttpURLConnection) new URL(urlBase + ruta).openConnection();
        try {
            conexion.setConnectTimeout(TIMEOUT_MS);
            conexion.setReadTimeout(TIMEOUT_MS);
            conexion.setUseCaches(false);
            conexion.setRequestMethod(metodo);
            conexion.setRequestProperty("Accept", "application/json");
            if (autorizacion != null) {
                conexion.setRequestProperty("Authorization", autorizacion);
            }
            if (cuerpoJson != null) {
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(cuerpoJson.getBytes(StandardCharsets.UTF_8));
                }
            }
            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? conexion.getErrorStream() : conexion.getInputStream();
            return new RespuestaHttp(codigo, leerTodo(flujo));
        } finally {
            conexion.disconnect();
        }
    }

    private static String leerTodo(InputStream flujo) throws IOException {
        if (flujo == null) {
            return "";
        }
        try (InputStream entrada = flujo) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] bloque = new byte[4096];
            int n;
            while ((n = entrada.read(bloque)) != -1) {
                buffer.write(bloque, 0, n);
            }
            return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
