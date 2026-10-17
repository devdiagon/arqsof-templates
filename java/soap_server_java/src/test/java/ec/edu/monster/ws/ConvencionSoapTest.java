package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.controlador.EjemploControlador;
import ec.edu.monster.dto.EspacioNombres;
import ec.edu.monster.servicios.ServicioEjemplo;
import jakarta.xml.ws.Endpoint;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Pruebas de CONFORMIDAD con CONVENCION_SOAP.md. Publican el servicio real con Metro (sin Payara) en un puerto libre
 * y lo llaman por SOAP. Si una de estas pruebas falla, un cliente .NET dejaría de entender al servidor.
 * Al cambiar de servicio basta ajustar el nombre del servicio, las operaciones y sus campos.
 */
class ConvencionSoapTest {

    private static final String TNS = EspacioNombres.SERVICIO;
    private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";

    private static Endpoint endpoint;
    private static String url;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @BeforeAll
    static void publicar() throws Exception {
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/prueba/WSEjemplo";

        // Sin contenedor CDI: se inyecta a mano lo que Payara inyectaría con @Inject.
        WSEjemplo ws = new WSEjemplo();
        Field campo = WSEjemplo.class.getDeclaredField("controlador");
        campo.setAccessible(true);
        campo.set(ws, new EjemploControlador(new ServicioEjemplo()));
        endpoint = Endpoint.publish(url, ws);
    }

    @AfterAll
    static void detener() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    // ---- contrato publicado ----
    @Test
    void elWsdlDeclaraServicioOperacionesYSoapActionConUri() throws Exception {
        Document wsdl = xml(get(url + "?wsdl"));
        Element raiz = wsdl.getDocumentElement();

        assertEquals(TNS, raiz.getAttribute("targetNamespace"));
        assertEquals("WSEjemplo", raiz.getAttribute("name"));
        String texto = get(url + "?wsdl");
        assertTrue(texto.contains("soapAction=\"" + TNS + "WSEjemplo/eco\""), "SOAPAction con URI (R4)");
        assertTrue(texto.contains("soapAction=\"" + TNS + "WSEjemplo/listar\""), "SOAPAction con URI (R4)");
        assertTrue(texto.contains("style=\"document\"") && texto.contains("use=\"literal\""), "document/literal (R1)");
    }

    @Test
    void elEsquemaCalificaTodosLosElementos() throws Exception {
        String xsd = get(url + "?xsd=1");

        assertTrue(xsd.contains("elementFormDefault=\"qualified\""), "R3: campos de los tipos calificados");
        assertTrue(xsd.contains("name=\"respuesta\""), "R5: retorno único llamado respuesta");
        assertTrue(xsd.contains("name=\"RespuestaBase\"") && xsd.contains("name=\"RespuestaEco\""), "R6: tipos con nombre fijo");
    }

    // ---- mensajes en el cable ----
    @Test
    void laRespuestaTrae_TODOS_los_campos_en_el_namespace_del_servicio() throws Exception {
        Document r = xml(post("eco", TNS + "WSEjemplo/eco", "<ws:texto>hola</ws:texto>"));

        for (String campo : new String[]{"respuesta", "estado", "codigo", "mensaje", "texto"}) {
            assertEquals(1, r.getElementsByTagNameNS(TNS, campo).getLength(), "debe existir {TNS}" + campo);
        }
        assertEquals("OK", r.getElementsByTagNameNS(TNS, "estado").item(0).getTextContent());
        assertEquals("hola", r.getElementsByTagNameNS(TNS, "texto").item(0).getTextContent());
    }

    @Test
    void laListaSeSerializaComoContenedorConElementoRepetido() throws Exception {
        Document r = xml(post("listar", TNS + "WSEjemplo/listar", ""));

        assertEquals(1, r.getElementsByTagNameNS(TNS, "elementos").getLength());
        assertEquals(3, r.getElementsByTagNameNS(TNS, "elemento").getLength());
    }

    @Test
    void unParametroSinNamespaceNoSeInterpreta_porEso_los_clientes_deben_calificarlos() throws Exception {
        // Si esta prueba falla (el servidor lo acepta), la convención R3 dejó de aplicarse en la capa JAX-WS.
        Document r = xml(post("eco", TNS + "WSEjemplo/eco", null, "<texto>hola</texto>"));

        assertEquals("ERROR", r.getElementsByTagNameNS(TNS, "estado").item(0).getTextContent());
        assertEquals("DATO-001", r.getElementsByTagNameNS(TNS, "codigo").item(0).getTextContent());
    }

    @Test
    void unErrorDeNegocioEsHttp200ConEstadoERROR_noUnFault() throws Exception {
        HttpResponse<String> http = enviar("eco", TNS + "WSEjemplo/eco", "<ws:texto>   </ws:texto>");
        Document r = xml(http.body());

        assertEquals(200, http.statusCode());
        assertEquals("ERROR", r.getElementsByTagNameNS(TNS, "estado").item(0).getTextContent());
        assertEquals("DATO-001", r.getElementsByTagNameNS(TNS, "codigo").item(0).getTextContent());
        assertEquals(0, r.getElementsByTagNameNS(SOAP_ENV, "Fault").getLength());
    }

    @Test
    void metroToleraLaSoapActionVacia_ElServidorDotNetNo_porEso_losClientesLaEnvianConUri() throws Exception {
        HttpResponse<String> http = enviar("eco", "", "<ws:texto>hola</ws:texto>");

        assertEquals(200, http.statusCode());
        assertEquals("OK", xml(http.body()).getElementsByTagNameNS(TNS, "estado").item(0).getTextContent());
    }

    // ---- ayudantes ----
    private static String post(String operacion, String accion, String camposCalificados) throws Exception {
        return post(operacion, accion, camposCalificados, "");
    }

    /** camposCalificados usan el prefijo ws; camposSinNamespace van tal cual (para probar el caso incorrecto). */
    private static String post(String operacion, String accion, String camposCalificados, String camposSinNamespace) throws Exception {
        return enviar(operacion, accion, (camposCalificados == null ? "" : camposCalificados) + camposSinNamespace).body();
    }

    private static HttpResponse<String> enviar(String operacion, String accion, String hijos) throws Exception {
        String sobre = "<s:Envelope xmlns:s=\"" + SOAP_ENV + "\"><s:Body><ws:" + operacion + " xmlns:ws=\"" + TNS + "\">"
                + hijos + "</ws:" + operacion + "></s:Body></s:Envelope>";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + accion + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String get(String direccion) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(direccion)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }

    private static Document xml(String texto) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
    }
}
