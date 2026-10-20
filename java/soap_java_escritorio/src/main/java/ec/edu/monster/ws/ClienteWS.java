package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.ws.generado.RespuestaBase;
import ec.edu.monster.ws.generado.RespuestaLogin;
import ec.edu.monster.ws.generado.WSServicio;
import ec.edu.monster.ws.generado.WSServicio_Service;
import jakarta.xml.ws.BindingProvider;
import java.util.Map;

/**
 * Envoltorio del proxy que genera {@code wsimport} a partir de {@code src/main/resources/wsdl/WSServicio.wsdl}:
 * crea el puerto, fija la URL completa del servicio y los timeouts, y copia las respuestas generadas a los DTO propios del cliente.
 * No contiene nada del dominio salvo la lista de operaciones.
 *
 * El proxy usa el WSDL empaquetado en el classpath: NO se descarga al crearlo, así que {@link #crear(String)} funciona con el
 * servidor apagado (el fallo, si lo hay, llega al llamar a una operación, como {@code WebServiceException}).
 * La URL se recibe COMPLETA (R9) y el puerto se pide a la clase de servicio generada, sin depender de su nombre (R10).
 *
 * DOMINIO: al cambiar el WSDL, regenerar con {@code mvn generate-sources} y ajustar los nombres de las clases generadas
 * ({@code WSServicio}, {@code WSServicio_Service}, {@code RespuestaLogin}) y las operaciones de este envoltorio.
 */
public final class ClienteWS implements ClienteServicio {

    private final WSServicio puerto;

    private ClienteWS(WSServicio puerto) {
        this.puerto = puerto;
    }

    /** URL configurada: propiedad {@code ws.url}, variable {@code WS_URL} o el valor por defecto. */
    public static String urlConfigurada() {
        String url = System.getProperty("ws.url");
        if (url == null || url.isBlank()) {
            url = System.getenv("WS_URL");
        }
        if (url == null || url.isBlank()) {
            url = ConfiguracionServicio.URL_POR_DEFECTO;
        }
        return normalizarUrl(url);
    }

    static String normalizarUrl(String url) {
        return url.trim().replaceAll("/+$", "");
    }

    public static ClienteServicio crear() {
        return crear(urlConfigurada());
    }

    /** Recibe la URL COMPLETA del servicio (Payara, .NET, IIS...); no le añade ninguna ruta. */
    public static ClienteServicio crear(String urlServicio) {
        WSServicio puerto = new WSServicio_Service().getWSServicioPort();
        Map<String, Object> contexto = ((BindingProvider) puerto).getRequestContext();
        // La dirección del soap:address del WSDL se ignora: manda la URL configurada.
        contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, normalizarUrl(urlServicio));
        contexto.put("com.sun.xml.ws.connect.timeout", ConfiguracionServicio.TIMEOUT_MS);
        contexto.put("com.sun.xml.ws.request.timeout", ConfiguracionServicio.TIMEOUT_MS);
        return new ClienteWS(puerto);
    }

    @Override
    public RespuestaLoginDto login(String usuario, String contrasenia) {
        RespuestaLogin r = puerto.login(usuario, contrasenia);
        if (r == null) {
            return null;
        }
        RespuestaLoginDto dto = base(r, new RespuestaLoginDto());
        dto.setToken(r.getToken());
        return dto;
    }

    // DOMINIO: una operación por cada método de ClienteServicio. Las listas (RespuestaLista) se copian así:
    //   RespuestaListaDto dto = base(r, new RespuestaListaDto());
    //   if (r.getElementos() != null) { dto.getElementos().addAll(r.getElementos().getElemento()); }

    private static <T extends RespuestaBaseDto> T base(RespuestaBase origen, T destino) {
        destino.setEstado(origen.getEstado());
        destino.setCodigo(origen.getCodigo());
        destino.setMensaje(origen.getMensaje());
        return destino;
    }
}
