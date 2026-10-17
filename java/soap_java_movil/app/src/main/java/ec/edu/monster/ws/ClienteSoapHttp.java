package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementacion del contrato por HTTP POST con sobres SOAP 1.1 armados a mano (SoapXml).
 * Todo fallo de comunicacion (red, timeout, HTTP inesperado, Fault) sale como ExcepcionServicio con codigo RED.
 * Debe invocarse fuera del hilo principal.
 *
 * DOMINIO: cada operacion nueva es un metodo de tres lineas: armar parametros, enviar y leer con SoapXml.
 *
 * @author: Frederick Tipán
 */
public class ClienteSoapHttp implements ContratoWS {

    private static final String MENSAJE_RED = "No fue posible comunicarse con el servidor";

    private final String urlServicio;
    private final String namespace;
    private final String servicio;

    /** URL COMPLETA del servicio (la entrega res/values/config.xml: {@code getString(R.string.url_servicio)}). */
    public ClienteSoapHttp(String urlServicio) {
        this(urlServicio, ConfiguracionServicio.NAMESPACE, ConfiguracionServicio.NOMBRE_SERVICIO);
    }

    public ClienteSoapHttp(String urlServicio, String namespace, String servicio) {
        this.urlServicio = urlServicio;
        this.namespace = namespace;
        this.servicio = servicio;
    }

    @Override
    public RespuestaLoginDto login(String usuario, String contrasenia) {
        return SoapXml.leerLogin(enviar("login", parametros("usuario", usuario, "contrasenia", contrasenia)));
    }

    // DOMINIO: ejemplo de una operacion nueva (declararla tambien en ContratoWS):
    //
    // @Override
    // public RespuestaListaDto obtenerItems(String token) {
    //     return SoapXml.leerLista(enviar("obtenerItems", parametros("token", token)));
    // }

    /** Pares clave/valor en el orden en que el servidor espera los parametros: parametros("a", x, "b", y). */
    protected static Map<String, String> parametros(String... paresClaveValor) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < paresClaveValor.length; i += 2) {
            mapa.put(paresClaveValor[i], paresClaveValor[i + 1]);
        }
        return mapa;
    }

    /** POST del sobre; devuelve el cuerpo si el servidor responde 200 o 500 (Fault, lo interpreta SoapXml). */
    protected String enviar(String operacion, Map<String, String> parametros) {
        byte[] cuerpo = SoapXml.construirPeticion(namespace, operacion, parametros).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(urlServicio).openConnection();
            conexion.setConnectTimeout(ConfiguracionServicio.TIMEOUT_MS);
            conexion.setReadTimeout(ConfiguracionServicio.TIMEOUT_MS);
            conexion.setRequestMethod("POST");
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conexion.setRequestProperty("SOAPAction", SoapXml.accionSoap(namespace, servicio, operacion));
            conexion.setFixedLengthStreamingMode(cuerpo.length);
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(cuerpo);
            }
            int codigo = conexion.getResponseCode();
            if (codigo != HttpURLConnection.HTTP_OK && codigo != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                throw new ExcepcionServicio(ExcepcionServicio.RED, MENSAJE_RED);
            }
            InputStream flujo = codigo == HttpURLConnection.HTTP_OK ? conexion.getInputStream() : conexion.getErrorStream();
            return leerTodo(flujo);
        } catch (IOException e) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, MENSAJE_RED, e);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
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
