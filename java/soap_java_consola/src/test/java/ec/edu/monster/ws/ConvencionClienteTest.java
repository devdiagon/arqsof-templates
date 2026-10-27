package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.ws.ejemplo.RespuestaEco;
import ec.edu.monster.ws.ejemplo.WSEjemplo;
import ec.edu.monster.ws.ejemplo.WSEjemplo_Service;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * Pruebas de CONFORMIDAD del cliente con docs/CONVENCION_SOAP.md. Se publica un servidor falso que sigue la convención
 * (como lo haría el servidor real) y se mide lo que el cliente ENVÍA y lo que ENTIENDE. El cliente es el proxy que
 * {@code wsimport-test} genera desde {@code src/test/resources/wsdl/WSEjemplo.wsdl} (paquete {@code ec.edu.monster.ws.ejemplo}),
 * configurado con {@link ConfiguracionServicio}:
 *  - los parámetros viajan calificados con el namespace del servicio (R3) y la SOAPAction es la URI de la operación (R4);
 *  - los campos de la respuesta (calificados) llegan con valor, no nulos;
 *  - el puerto se obtiene de la clase de servicio generada y la URL completa se acepta tal cual (R9, R10).
 * Si una falla, el servidor (Java o .NET) rechazaría o no entendería al cliente. Sirve de modelo para las pruebas del dominio.
 */
class ConvencionClienteTest {

    // Los mismos valores que src/test/resources/wsdl/WSEjemplo.wsdl
    private static final String TNS = "http://ws.monster.edu.ec/";
    private static final String SERVICIO = "WSEjemplo";

    private static Endpoint endpoint;
    private static String url;
    private static volatile Map<String, String> hijosRecibidos = new HashMap<>();
    private static volatile String accionRecibida;
    private static volatile String campoRespuesta;

    /**
     * Servidor falso: misma forma que un servidor real que cumple la convención. El {@code name} (portType) es el
     * mismo que el del WSDL del cliente.
     */
    @WebService(name = SERVICIO, serviceName = SERVICIO, targetNamespace = TNS)
    public static class ServidorFalso {
        @WebMethod(operationName = "eco", action = TNS + SERVICIO + "/eco")
        @WebResult(name = "respuesta", targetNamespace = TNS)
        public RespuestaEco eco(@WebParam(name = "texto", targetNamespace = TNS) String texto) {
            RespuestaEco r = new RespuestaEco();
            if (texto == null || texto.isBlank()) {
                r.setEstado("ERROR");
                r.setCodigo("DATO-001");
                r.setMensaje("El texto es obligatorio");
            } else {
                r.setEstado("OK");
                r.setCodigo("200");
                r.setMensaje("Operación realizada correctamente");
                r.setTexto(texto);
            }
            return r;
        }
    }

    /** Registra qué elementos hijos y qué SOAPAction llegaron al servidor. */
    static class Captura implements SOAPHandler<SOAPMessageContext> {
        @Override
        public boolean handleMessage(SOAPMessageContext ctx) {
            if (Boolean.TRUE.equals(ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
                registrarRespuesta(ctx);
                return true;
            }
            try {
                SOAPBody cuerpo = ctx.getMessage().getSOAPBody();
                Map<String, String> hijos = new HashMap<>();
                Node operacion = cuerpo.getFirstChild();
                while (operacion != null && operacion.getNodeType() != Node.ELEMENT_NODE) {
                    operacion = operacion.getNextSibling();
                }
                for (Node n = operacion == null ? null : operacion.getFirstChild(); n != null; n = n.getNextSibling()) {
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        hijos.put(n.getLocalName(), n.getNamespaceURI() == null ? "" : n.getNamespaceURI());
                    }
                }
                hijosRecibidos = hijos;
                @SuppressWarnings("unchecked")
                Map<String, List<String>> cabeceras = (Map<String, List<String>>) ctx.get(MessageContext.HTTP_REQUEST_HEADERS);
                accionRecibida = null;
                if (cabeceras != null) {
                    for (Map.Entry<String, List<String>> e : cabeceras.entrySet()) {
                        if ("soapaction".equalsIgnoreCase(e.getKey()) && !e.getValue().isEmpty()) {
                            accionRecibida = e.getValue().get(0);
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            return true;
        }

        /** Namespace con que sale el campo «estado» de la respuesta del servidor (R3). */
        private static void registrarRespuesta(SOAPMessageContext ctx) {
            try {
                Node operacion = primerElemento(ctx.getMessage().getSOAPBody());
                Node respuesta = operacion == null ? null : primerElemento(operacion);
                Node estado = respuesta == null ? null : primerElemento(respuesta);
                campoRespuesta = estado == null ? null : (estado.getNamespaceURI() == null ? "" : estado.getNamespaceURI());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private static Node primerElemento(Node padre) {
            for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    return n;
                }
            }
            return null;
        }

        @Override public boolean handleFault(SOAPMessageContext ctx) { return true; }
        @Override public void close(MessageContext ctx) { }
        @Override public Set<QName> getHeaders() { return Set.of(); }
    }

    @BeforeAll
    static void publicar() throws Exception {
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) {
            puerto = s.getLocalPort();
        }
        url = "http://localhost:" + puerto + "/prueba/" + SERVICIO;
        endpoint = Endpoint.create(new ServidorFalso());
        List<Handler> cadena = new ArrayList<>();
        cadena.add(new Captura());
        endpoint.getBinding().setHandlerChain(cadena);
        endpoint.publish(url);
    }

    @AfterAll
    static void detener() {
        if (endpoint != null) {
            endpoint.stop();
        }
    }

    private static WSEjemplo cliente() {
        return ConfiguracionServicio.configurar(new WSEjemplo_Service().getWSEjemploPort(), url);
    }

    @Test
    void elClienteCalificaLosParametrosYEnviaLaSoapActionConUri() {
        cliente().eco("hola");

        assertEquals(TNS, hijosRecibidos.get("texto"), "R3: el parámetro debe ir calificado con el namespace del servicio");
        assertEquals("\"" + TNS + SERVICIO + "/eco\"", accionRecibida, "R4: SOAPAction con la URI de la operación");
    }

    @Test
    void losCamposDeLaRespuestaViajanCalificadosYElClienteLosEntiende() {
        // Si el WSDL no declara los campos calificados, un servidor .NET los dejaria vacios (R3).
        cliente().eco("hola");
        assertEquals(TNS, campoRespuesta, "R3: los campos de la respuesta deben ir calificados con el namespace del servicio");
    }

    @Test
    void elClienteEntiendeUnaRespuestaConCamposCalificados() {
        RespuestaEco r = cliente().eco("hola");

        assertNotNull(r);
        assertEquals("OK", r.getEstado());
        assertEquals("200", r.getCodigo());
        assertEquals("hola", r.getTexto());
    }

    @Test
    void unaRespuestaDeErrorLlegaConSuCodigoYMensaje() {
        RespuestaEco r = cliente().eco("  ");

        assertFalse("OK".equals(r.getEstado()));
        assertEquals("DATO-001", r.getCodigo());
        assertEquals("El texto es obligatorio", r.getMensaje());
    }

    @Test
    void laUrlCompletaSeAceptaConBarrasFinales() {
        RespuestaEco r = ConfiguracionServicio.configurar(new WSEjemplo_Service().getWSEjemploPort(), url + "//").eco("x");

        assertTrue("OK".equals(r.getEstado()));
    }
}
