package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.ws.generado.RespuestaLogin;
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
 * Pruebas de CONFORMIDAD del cliente con CONVENCION_SOAP.md, sobre el proxy generado. Se publica un servidor
 * falso que sigue la convención y se mide lo que el cliente ENVÍA y lo que ENTIENDE:
 *  - los parámetros viajan calificados con el namespace del servicio (R3) y la SOAPAction es la URI de la operación (R4);
 *  - los campos de la respuesta (calificados) llegan con valor, no nulos (el proxy generado desde el WSDL);
 *  - el puerto se obtiene por la interfaz y la URL completa se acepta tal cual (R9, R10).
 * Si una falla, el servidor (Java o .NET) rechazaría o no entendería al cliente. Al agregar operaciones, repetir el patrón.
 */
class ConvencionClienteTest {

    // Los mismos valores que src/main/resources/wsdl/WSServicio.wsdl (DOMINIO: cambiarlos junto con el WSDL)
    private static final String TNS = "http://CAMBIAR.namespace/";
    private static final String SERVICIO = "WSServicio";

    private static Endpoint endpoint;
    private static String url;
    private static volatile Map<String, String> hijosRecibidos = new HashMap<>();
    private static volatile String accionRecibida;
    private static volatile String campoRespuesta;

    /** Servidor falso: misma forma que un servidor real que cumple la convención (mismo portType que el contrato del cliente). */
    @WebService(name = SERVICIO, serviceName = SERVICIO,
            targetNamespace = TNS)
    public static class ServidorFalso {
        @WebMethod(operationName = "login", action = TNS + SERVICIO + "/login")
        @WebResult(name = "respuesta", targetNamespace = TNS)
        public RespuestaLogin login(
                @WebParam(name = "usuario", targetNamespace = TNS) String usuario,
                @WebParam(name = "contrasenia", targetNamespace = TNS) String contrasenia) {
            RespuestaLogin r = new RespuestaLogin();
            if (usuario == null || usuario.isBlank() || contrasenia == null || contrasenia.isBlank()) {
                r.setEstado("ERROR");
                r.setCodigo("AUTH-001");
                r.setMensaje("Usuario y contraseña son obligatorios");
            } else {
                r.setEstado("OK");
                r.setCodigo("200");
                r.setMensaje("Ingreso realizado correctamente");
                r.setToken("TOKEN-1");
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

    @Test
    void elClienteCalificaLosParametrosYEnviaLaSoapActionConUri() {
        ClienteWS.crear(url).login("u", "c");

        assertEquals(TNS, hijosRecibidos.get("usuario"), "R3: el parámetro debe ir calificado con el namespace del servicio");
        assertEquals(TNS, hijosRecibidos.get("contrasenia"));
        assertEquals("\"" + TNS + SERVICIO + "/login" + "\"", accionRecibida, "R4: SOAPAction con la URI de la operación");
    }

    @Test
    void losCamposDeLaRespuestaViajanCalificadosYElClienteLosEntiende() {
        // Si el WSDL no declara los campos calificados, un servidor .NET los dejaria vacios (R3).
        ClienteWS.crear(url).login("u", "c");
        assertEquals(TNS, campoRespuesta, "R3: los campos de la respuesta deben ir calificados con el namespace del servicio");
    }

    @Test
    void elClienteEntiendeUnaRespuestaConCamposCalificados() {
        RespuestaLoginDto r = ClienteWS.crear(url).login("u", "c");

        assertNotNull(r);
        assertTrue(r.esOk());
        assertEquals("200", r.getCodigo());
        assertEquals("TOKEN-1", r.getToken());
    }

    @Test
    void unaRespuestaDeErrorLlegaConSuCodigoYMensaje() {
        RespuestaLoginDto r = ClienteWS.crear(url).login(" ", " ");

        assertFalse(r.esOk());
        assertEquals("AUTH-001", r.getCodigo());
        assertEquals("Usuario y contraseña son obligatorios", r.getMensaje());
    }

    @Test
    void laUrlCompletaSeAceptaConBarrasFinales() {
        assertTrue(ClienteWS.crear(url + "//").login("u", "c").esOk());
    }
}
