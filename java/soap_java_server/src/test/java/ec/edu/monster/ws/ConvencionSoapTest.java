package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ec.edu.monster.controlador.ElementoControlador;
import ec.edu.monster.modelo.Elemento;
import ec.edu.monster.servicios.ElementoServicio;
import ec.edu.monster.ws.contrato.ConvencionSoap;
import jakarta.xml.ws.Endpoint;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Pruebas de CONFORMIDAD con CONVENCION_SOAP.md. Publican el servicio real con Metro (sin Payara ni base de datos)
 * en un puerto libre y lo llaman por SOAP. Si una de estas pruebas falla, un cliente .NET dejaría de entender al
 * servidor. Al cambiar de servicio basta ajustar el nombre, las operaciones y sus campos.
 */
class ConvencionSoapTest {

    private static final String TNS = ConvencionSoap.TNS;
    private static final String SERVICIO = ConvencionSoap.SERVICIO_ELEMENTO;
    private static final String ACCION = ConvencionSoap.ACCION_ELEMENTO;
    private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static Endpoint endpoint;
    private static String url;

    @BeforeAll
    static void publicar() throws Exception {
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/prueba/" + SERVICIO;

        ElementoServicio servicio = mock(ElementoServicio.class);
        when(servicio.consultar("0001")).thenReturn(new Elemento("0001", "Primero", new BigDecimal("10.5")));
        when(servicio.listar()).thenReturn(List.of(
                new Elemento("0001", "Primero", new BigDecimal("10.5")),
                new Elemento("0002", "Segundo", new BigDecimal("20"))));
        endpoint = Endpoint.publish(url, new ElementoWS(new ElementoControlador(servicio)));
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
        String texto = get(url + "?wsdl");
        Element raiz = xml(texto).getDocumentElement();

        assertEquals(TNS, raiz.getAttribute("targetNamespace"));
        assertEquals(SERVICIO, raiz.getAttribute("name"));
        for (String operacion : new String[]{"consultarElemento", "listarElementos", "registrarElemento"}) {
            assertTrue(texto.contains("soapAction=\"" + ACCION + operacion + "\""), "SOAPAction con URI (R4): " + operacion);
        }
        assertTrue(texto.contains("style=\"document\"") && texto.contains("use=\"literal\""), "document/literal (R1)");
    }

    @Test
    void elEsquemaCalificaTodosLosElementosYFijaLosNombres() throws Exception {
        String xsd = get(url + "?xsd=1");

        assertTrue(xsd.contains("elementFormDefault=\"qualified\""), "R3: campos de los tipos calificados");
        assertTrue(xsd.contains("name=\"respuesta\""), "R5: retorno único llamado respuesta");
        assertTrue(xsd.contains("name=\"RespuestaBase\"") && xsd.contains("name=\"RespuestaElemento\""),
                "R6: tipos con nombre fijo");
    }

    // ---- mensajes en el cable ----
    @Test
    void laRespuestaTrae_TODOS_los_campos_en_el_namespace_del_servicio() throws Exception {
        Document r = xml(enviar("consultarElemento", "<ws:codigoElemento>0001</ws:codigoElemento>").body());

        for (String campo : new String[]{"respuesta", "estado", "codigo", "mensaje", "codigoElemento", "nombre", "valor"}) {
            assertEquals(1, r.getElementsByTagNameNS(TNS, campo).getLength(), "debe existir {TNS}" + campo);
        }
        assertEquals("OK", texto(r, "estado"));
        assertEquals("10.50", texto(r, "valor"));
    }

    @Test
    void laListaSeSerializaComoContenedorConElementoRepetido() throws Exception {
        Document r = xml(enviar("listarElementos", "").body());

        assertEquals(1, r.getElementsByTagNameNS(TNS, "elementos").getLength());
        assertEquals(2, r.getElementsByTagNameNS(TNS, "elemento").getLength());
    }

    @Test
    void unParametroSinNamespaceNoSeInterpreta_porEso_los_clientes_deben_calificarlos() throws Exception {
        // Si esta prueba falla (el servidor lo acepta), la convención R3 dejó de aplicarse en la capa JAX-WS.
        Document r = xml(enviar("consultarElemento", "<codigoElemento>0001</codigoElemento>").body());

        assertEquals("ERROR", texto(r, "estado"));
        assertEquals("DATA-001", texto(r, "codigo"));
    }

    @Test
    void unErrorDeNegocioEsHttp200ConEstadoERROR_noUnFault() throws Exception {
        HttpResponse<String> http = enviar("consultarElemento", "<ws:codigoElemento>12</ws:codigoElemento>");
        Document r = xml(http.body());

        assertEquals(200, http.statusCode());
        assertEquals("ERROR", texto(r, "estado"));
        assertEquals("DATA-001", texto(r, "codigo"));
        assertEquals(0, r.getElementsByTagNameNS(SOAP_ENV, "Fault").getLength());
    }

    // ---- ayudantes ----
    private static HttpResponse<String> enviar(String operacion, String hijos) throws Exception {
        String sobre = "<s:Envelope xmlns:s=\"" + SOAP_ENV + "\"><s:Body><ws:" + operacion + " xmlns:ws=\"" + TNS + "\">"
                + hijos + "</ws:" + operacion + "></s:Body></s:Envelope>";
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + ACCION + operacion + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String get(String direccion) throws Exception {
        return HTTP.send(HttpRequest.newBuilder(URI.create(direccion)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
    }

    private static String texto(Document documento, String campo) {
        return documento.getElementsByTagNameNS(TNS, campo).item(0).getTextContent();
    }

    private static Document xml(String texto) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(texto.getBytes(StandardCharsets.UTF_8)));
    }
}
