package ec.edu.monster.ws;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.w3c.dom.Element;

/**
 * Transporte SOAP 1.1 por HTTP POST con sobres armados a mano ({@link SoapXml}); es lo que en JAX-WS hace el proxy generado,
 * que Android no tiene. No conoce el dominio: lo usa la implementación de cada cliente ({@code XClienteWS}), que
 * convierte el elemento {@code respuesta} en su DTO. Todo fallo de comunicación (red, timeout, HTTP inesperado, Fault)
 * sale como {@link ExcepcionServicio} con código {@code RED}; una respuesta ilegible, como {@code SRV-000}.
 * Debe invocarse fuera del hilo principal.
 *
 * <pre>
 * public class XClienteWS implements XCliente {
 *     private final ClienteSoapHttp soap;
 *     public XClienteWS(String urlServicio) { soap = new ClienteSoapHttp(urlServicio, "XServicio"); }
 *
 *     public RespuestaItemsDto obtenerItems(String codigo) {
 *         Element r = soap.enviar("obtenerItems", SoapXml.parametros("codigo", codigo));
 *         RespuestaItemsDto dto = new RespuestaItemsDto();
 *         SoapXml.llenarBase(dto, r);
 *         dto.setItems(SoapXml.lista(r, "items", "item"));
 *         return dto;
 *     }
 * }
 * </pre>
 *
 * @author: Frederick Tipán
 */
public class ClienteSoapHttp {

    private final String urlServicio;
    private final String servicio;

    /**
     * @param urlServicio URL COMPLETA del servicio, tal como la entrega res/values/config.xml; no se le añade nada (R9)
     * @param servicio    nombre del servicio (el {@code name} del {@code <wsdl:service>}); forma la SOAPAction (R4)
     */
    public ClienteSoapHttp(String urlServicio, String servicio) {
        this.urlServicio = urlServicio;
        this.servicio = servicio;
    }

    /** Envía la operación y devuelve el elemento {@code respuesta} de la contestación. */
    public Element enviar(String operacion, Map<String, String> parametros) {
        return SoapXml.extraerRespuesta(post(operacion, parametros));
    }

    /** POST del sobre; devuelve el cuerpo si el servidor responde 200 o 500 (Fault, lo interpreta SoapXml). */
    private String post(String operacion, Map<String, String> parametros) {
        byte[] cuerpo = SoapXml.construirPeticion(ConfiguracionServicio.TNS, operacion, parametros)
                .getBytes(StandardCharsets.UTF_8);
        String accion = SoapXml.accionSoap(ConfiguracionServicio.TNS, servicio, operacion);
        return TraductorRed.llamar(() -> {
            HttpURLConnection conexion = (HttpURLConnection) new URL(urlServicio).openConnection();
            try {
                conexion.setConnectTimeout(ConfiguracionServicio.TIMEOUT_MS);
                conexion.setReadTimeout(ConfiguracionServicio.TIMEOUT_MS);
                conexion.setRequestMethod("POST");
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
                conexion.setRequestProperty("SOAPAction", accion);
                conexion.setFixedLengthStreamingMode(cuerpo.length);
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(cuerpo);
                }
                int codigo = conexion.getResponseCode();
                if (codigo != HttpURLConnection.HTTP_OK && codigo != HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    throw new IOException("HTTP " + codigo);
                }
                InputStream flujo = codigo == HttpURLConnection.HTTP_OK ? conexion.getInputStream() : conexion.getErrorStream();
                return leerTodo(flujo);
            } finally {
                conexion.disconnect();
            }
        });
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
