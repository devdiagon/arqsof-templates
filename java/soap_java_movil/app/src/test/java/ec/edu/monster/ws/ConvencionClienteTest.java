package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaBaseDto;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * EJEMPLO de conformidad con docs/CONVENCION_SOAP.md. Se levanta un servidor HTTP mínimo ({@link ServidorFalso}) que
 * responde como lo haría un servidor que cumple la convención y se mide lo que el cliente ENVÍA:
 *  - operación y parámetros calificados con el namespace del servicio (R3), que es el fijo del equipo (R2);
 *  - cabecera SOAPAction = TNS + servicio + "/" + operación, entre comillas (R4);
 *  - se usa la URL completa recibida, sin agregarle nada (R9).
 * Y lo que ENTIENDE: respuesta con campos calificados (con prefijo o con namespace por defecto).
 * Si una falla, el servidor (Java o .NET) rechazaría o no entendería a la app. Sirve de modelo para las del dominio, pero las
 * pruebas automáticas de conformidad están fuera de alcance (docs/CONVENCION_PRUEBAS.md §8): retirarlo al crear el proyecto.
 */
public class ConvencionClienteTest {

    private static final String TNS = ConfiguracionServicio.TNS;
    private static final String SERVICIO = "WSEjemplo";
    private static final String RUTA = "/ruta/libre/" + SERVICIO;

    private ServidorFalso servidor;
    private ClienteSoapHttp cliente;

    @Before
    public void levantar() throws Exception {
        servidor = new ServidorFalso();
        servidor.respuesta = ServidorFalso.sobre("<ns2:ecoResponse xmlns:ns2=\"" + TNS + "\"><ns2:respuesta>"
                + "<ns2:estado>OK</ns2:estado><ns2:codigo>200</ns2:codigo><ns2:mensaje>ok</ns2:mensaje>"
                + "<ns2:texto>hola</ns2:texto></ns2:respuesta></ns2:ecoResponse>");
        cliente = new ClienteSoapHttp(servidor.url(RUTA), SERVICIO);
    }

    @After
    public void detener() throws Exception {
        servidor.close();
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
    public void elTnsEsElFijoDelEquipo() {
        assertEquals("R2", "http://ws.monster.edu.ec/", TNS);
    }

    @Test
    public void laOperacionYLosParametrosVanCalificadosConElNamespaceDelServicio() throws Exception {
        cliente.enviar("eco", SoapXml.parametros("texto", "hola", "veces", "2"));

        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        Document doc = fabrica.newDocumentBuilder()
                .parse(new ByteArrayInputStream(servidor.cuerpoRecibido.getBytes(StandardCharsets.UTF_8)));
        Element operacion = hijoElemento(hijoElemento(doc.getDocumentElement(), 0), 0);
        assertEquals("eco", operacion.getLocalName());
        assertEquals("R3: la operación va en el namespace del servicio", TNS, operacion.getNamespaceURI());
        assertEquals("texto", hijoElemento(operacion, 0).getLocalName());
        assertEquals("R3: el parámetro va calificado", TNS, hijoElemento(operacion, 0).getNamespaceURI());
        assertEquals("R3: el parámetro va calificado", TNS, hijoElemento(operacion, 1).getNamespaceURI());
    }

    @Test
    public void laSoapActionEsLaUriDeLaOperacion() {
        cliente.enviar("eco", SoapXml.parametros("texto", "hola"));

        assertEquals("R4", "\"" + TNS + SERVICIO + "/eco\"", servidor.accionRecibida);
    }

    @Test
    public void seUsaLaUrlCompletaTalComoSeRecibe() {
        cliente.enviar("eco", SoapXml.parametros("texto", "hola"));

        assertEquals("R9: sin agregar nombre de servicio ni puerto", RUTA, servidor.rutaRecibida);
    }

    @Test
    public void entiendeUnaRespuestaConCamposCalificados() {
        Element r = cliente.enviar("eco", SoapXml.parametros("texto", "hola"));

        RespuestaBaseDto dto = new RespuestaBaseDto();
        SoapXml.llenarBase(dto, r);
        assertNotNull(r);
        assertTrue(dto.esOk());
        assertEquals("hola", SoapXml.texto(r, "texto"));
    }

    @Test
    public void entiendeUnaRespuestaConNamespacePorDefectoYUnErrorDeNegocio() {
        servidor.respuesta = ServidorFalso.sobre("<ecoResponse xmlns=\"" + TNS + "\"><respuesta><estado>ERROR</estado>"
                + "<codigo>DATO-001</codigo><mensaje>El texto es obligatorio</mensaje></respuesta></ecoResponse>");

        RespuestaBaseDto dto = new RespuestaBaseDto();
        SoapXml.llenarBase(dto, cliente.enviar("eco", SoapXml.parametros("texto", "")));

        assertFalse(dto.esOk());
        assertEquals("DATO-001", dto.getCodigo());
        assertEquals("El texto es obligatorio", dto.getMensaje());
    }
}
