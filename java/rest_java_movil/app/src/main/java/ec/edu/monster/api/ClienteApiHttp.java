package ec.edu.monster.api;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Implementacion con {@link HttpURLConnection} (Android no incluye java.net.http). Unica clase que arma peticiones:
 * pone {@code Accept}, {@code Content-Type} (si hay cuerpo) y {@code Authorization: Bearer} (si hay token).
 * No contiene nada del dominio. Debe invocarse fuera del hilo principal.
 *
 * No se usa el modo "streaming" de la conexion: con el, un 401 en un POST lanza HttpRetryException en lugar de
 * devolver el codigo y el cuerpo.
 *
 * @author: Frederick Tipán
 */
public class ClienteApiHttp implements ClienteApi {

    private final String base;

    public ClienteApiHttp() {
        this(ConfiguracionApi.BASE_POR_DEFECTO);
    }

    public ClienteApiHttp(String base) {
        this.base = base.trim().replaceAll("/+$", "");
    }

    @Override
    public RespuestaHttp enviar(String metodo, String ruta, String cuerpoJson, String token) {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(base + ruta).openConnection();
            conexion.setConnectTimeout(ConfiguracionApi.TIMEOUT_MS);
            conexion.setReadTimeout(ConfiguracionApi.TIMEOUT_MS);
            conexion.setRequestMethod(metodo);
            conexion.setRequestProperty("Accept", "application/json");
            if (token != null) {
                conexion.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (cuerpoJson != null) {
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(cuerpoJson.getBytes(StandardCharsets.UTF_8));
                }
            }
            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            return new RespuestaHttp(codigo, leerTodo(flujo));
        } catch (IOException e) {   // incluye ConnectException y los timeouts de conexion y de lectura
            if (esDesafioDeAutenticacionAusente(e)) {
                // Android lanza esto ante un 401 sin cabecera WWW-Authenticate: el cuerpo no se puede leer.
                return new RespuestaHttp(HttpURLConnection.HTTP_UNAUTHORIZED, "");
            }
            throw ExcepcionServicio.servidorNoDisponible(e);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static boolean esDesafioDeAutenticacionAusente(IOException e) {
        return e.getMessage() != null && e.getMessage().contains("No authentication challenges found");
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
