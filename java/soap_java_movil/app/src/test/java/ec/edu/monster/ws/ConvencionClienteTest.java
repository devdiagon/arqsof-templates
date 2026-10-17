package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaLoginDto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Pruebas de CONFORMIDAD del cliente con CONVENCION_SOAP.md. Se levanta un servidor HTTP minimo (ServerSocket: el
 * android.jar de compilacion no trae com.sun.net.httpserver) que responde como
 * lo haria un servidor que cumple la convencion y se mide lo que el cliente ENVIA:
 *  - operacion y parametros calificados con el namespace del servicio (R3);
 *  - cabecera SOAPAction = namespace + servicio + "/" + operacion, entre comillas (R4);
 *  - se usa la URL completa recibida, sin agregarle nada (R9).
 * Y lo que ENTIENDE: respuesta con campos calificados (con prefijo o con namespace por defecto).
 * Si una falla, el servidor (Java o .NET) rechazaria o no entenderia a la app. Al agregar operaciones, repetir el patron.
 */
public class ConvencionClienteTest {

    private static final String TNS = ConfiguracionServicio.NAMESPACE;
    private static final String RUTA = "/ruta/libre/" + ConfiguracionServicio.NOMBRE_SERVICIO;

    private ServerSocket servidor;
    private Thread hilo;
    private String url;
    private volatile String rutaRecibida;
    private volatile String accionRecibida;
    private volatile String cuerpoRecibido;
    private volatile String respuesta;

    @Before
    public void levantar() throws Exception {
        servidor = new ServerSocket(0, 5, InetAddress.getLoopbackAddress());
        hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try (Socket conexion = servidor.accept()) {
                    atender(conexion);
                } catch (Exception e) {
                    // servidor cerrado al terminar la prueba
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        url = "http://localhost:" + servidor.getLocalPort() + RUTA;
        respuesta = envelope("ns2:loginResponse xmlns:ns2=\"" + TNS + "\"",
                "<ns2:respuesta><ns2:estado>OK</ns2:estado><ns2:codigo>200</ns2:codigo>"
                        + "<ns2:mensaje>ok</ns2:mensaje><ns2:token>TOKEN-1</ns2:token></ns2:respuesta>", "ns2:loginResponse");
    }

    @After
    public void detener() throws Exception {
        servidor.close();
    }

    /** Lee una peticion HTTP/1.1 (linea, cabeceras, cuerpo por Content-Length) y responde 200 con {@link #respuesta}. */
    private void atender(Socket conexion) throws Exception {
        InputStream in = conexion.getInputStream();
        String linea = leerLinea(in);
        rutaRecibida = linea.split(" ")[1];
        int largo = 0;
        accionRecibida = null;
        String cabecera;
        while (!(cabecera = leerLinea(in)).isEmpty()) {
            String minuscula = cabecera.toLowerCase();
            if (minuscula.startsWith("content-length:")) {
                largo = Integer.parseInt(cabecera.substring(cabecera.indexOf(':') + 1).trim());
            } else if (minuscula.startsWith("soapaction:")) {
                accionRecibida = cabecera.substring(cabecera.indexOf(':') + 1).trim();
            }
        }
        byte[] cuerpo = new byte[largo];
        int leidos = 0;
        while (leidos < largo) {
            int n = in.read(cuerpo, leidos, largo - leidos);
            if (n < 0) {
                break;
            }
            leidos += n;
        }
        cuerpoRecibido = new String(cuerpo, StandardCharsets.UTF_8);
        byte[] salida = respuesta.getBytes(StandardCharsets.UTF_8);
        OutputStream out = conexion.getOutputStream();
        out.write(("HTTP/1.1 200 OK\r\nContent-Type: text/xml; charset=utf-8\r\nContent-Length: " + salida.length
                + "\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(salida);
        out.flush();
    }

    private static String leerLinea(InputStream in) throws Exception {
        ByteArrayOutputStream linea = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1 && b != '\n') {
            if (b != '\r') {
                linea.write(b);
            }
        }
        return new String(linea.toByteArray(), StandardCharsets.UTF_8);
    }
    private static String envelope(String abre, String contenido, String cierra) {
        return "<?xml version='1.0' encoding='UTF-8'?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<" + abre + ">" + contenido + "</" + cierra + "></S:Body></S:Envelope>";
    }

    private static Element hijoElemento(Node padre, int posicion) {
        int i = 0;
        for (Node n = padre.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element && i++ == posicion) {
                return (Element) n;
            }
        }
        return null;
    }

    @Test
    public void laOperacionYLosParametrosVanCalificadosConElNamespaceDelServicio() throws Exception {
        new ClienteSoapHttp(url).login("u", "c");

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        Document doc = fabrica.newDocumentBuilder().parse(new ByteArrayInputStream(cuerpoRecibido.getBytes(StandardCharsets.UTF_8)));
        Element operacion = hijoElemento(hijoElemento(doc.getDocumentElement(), 0), 0);
        assertEquals("login", operacion.getLocalName());
        assertEquals("R3: la operacion va en el namespace del servicio", TNS, operacion.getNamespaceURI());
        assertEquals("R3: el parametro va calificado", TNS, hijoElemento(operacion, 0).getNamespaceURI());
        assertEquals("usuario", hijoElemento(operacion, 0).getLocalName());
        assertEquals("R3: el parametro va calificado", TNS, hijoElemento(operacion, 1).getNamespaceURI());
    }

    @Test
    public void laSoapActionEsLaUriDeLaOperacion() {
        new ClienteSoapHttp(url).login("u", "c");

        assertEquals("R4", "\"" + TNS + ConfiguracionServicio.NOMBRE_SERVICIO + "/login\"", accionRecibida);
    }

    @Test
    public void seUsaLaUrlCompletaTalComoSeRecibe() {
        new ClienteSoapHttp(url).login("u", "c");

        assertEquals("R9: sin agregar nombre de servicio ni puerto", RUTA, rutaRecibida);
    }

    @Test
    public void entiendeUnaRespuestaConCamposCalificados() {
        RespuestaLoginDto r = new ClienteSoapHttp(url).login("u", "c");

        assertNotNull(r);
        assertTrue(r.esOk());
        assertEquals("TOKEN-1", r.getToken());
    }

    @Test
    public void entiendeUnaRespuestaConNamespacePorDefectoYUnErrorDeNegocio() {
        respuesta = envelope("loginResponse xmlns=\"" + TNS + "\"",
                "<respuesta><estado>ERROR</estado><codigo>AUTH-001</codigo><mensaje>Credenciales invalidas</mensaje></respuesta>",
                "loginResponse");

        RespuestaLoginDto r = new ClienteSoapHttp(url).login("u", "c");

        assertFalse(r.esOk());
        assertEquals("AUTH-001", r.getCodigo());
        assertEquals("Credenciales invalidas", r.getMensaje());
    }
}
