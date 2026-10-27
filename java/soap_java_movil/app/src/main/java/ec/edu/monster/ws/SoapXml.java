package ec.edu.monster.ws;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Armado de peticiones SOAP 1.1 y lectura de respuestas. Funciones puras (sin android.*), por lo que se
 * prueban con JUnit local. Los elementos se leen por nombre local, ignorando prefijos de namespace.
 *
 * Convención de respuesta que asume (la de los servidores de esta familia): dentro del Body viene
 * &lt;{operacion}Response&gt; con un hijo &lt;respuesta&gt; que contiene estado, codigo, mensaje y los datos de la
 * operación. Un Fault es un fallo de comunicación ({@code RED}); un cuerpo vacío, un XML inválido o una respuesta sin
 * el elemento esperado son una respuesta ilegible ({@code SRV-000}).
 *
 * @author: Frederick Tipán
 */
public final class SoapXml {

    /** Nombre del elemento que envuelve la respuesta de cada operación. */
    public static final String ELEMENTO_RESPUESTA = "respuesta";

    private SoapXml() {
    }

    // ------------------------------------------------------------------ petición

    /** Pares clave/valor en el orden en que el servidor espera los parámetros: {@code parametros("a", x, "b", y)}. */
    public static Map<String, String> parametros(String... paresClaveValor) {
        Map<String, String> mapa = new LinkedHashMap<>();
        for (int i = 0; i + 1 < paresClaveValor.length; i += 2) {
            mapa.put(paresClaveValor[i], paresClaveValor[i + 1]);
        }
        return mapa;
    }

    /**
     * Arma el sobre de una operación; los parámetros salen en el orden del mapa y con el texto escapado.
     * Operación y parámetros van CALIFICADOS con el namespace del servicio (prefijo ns): lo exige la convención
     * (docs/CONVENCION_SOAP.md, R3) y lo esperan por igual los servidores Java y .NET.
     */
    public static String construirPeticion(String namespace, String operacion, Map<String, String> parametros) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>")
                .append("<ns:").append(operacion).append(" xmlns:ns=\"").append(namespace).append("\">");
        for (Map.Entry<String, String> p : parametros.entrySet()) {
            sb.append("<ns:").append(p.getKey()).append('>')
                    .append(escapar(p.getValue()))
                    .append("</ns:").append(p.getKey()).append('>');
        }
        sb.append("</ns:").append(operacion).append("></S:Body></S:Envelope>");
        return sb.toString();
    }

    /**
     * Valor de la cabecera SOAPAction de una operación: la URI {@code namespace + servicio + "/" + operacion}, entre
     * comillas (R4). Un servidor .NET rechaza la cabecera vacía con HTTP 500 (ActionNotSupported).
     */
    public static String accionSoap(String namespace, String servicio, String operacion) {
        return "\"" + namespace + servicio + "/" + operacion + "\"";
    }

    // ------------------------------------------------------------------ lectura de respuestas

    /**
     * Devuelve el elemento "respuesta" de la operación. Un S:Fault es un fallo de comunicación ({@code RED}); un cuerpo
     * vacío, un XML inválido o una respuesta sin el elemento esperado son ilegibles ({@code SRV-000}).
     */
    public static Element extraerRespuesta(String xml) {
        Document doc = parsear(xml);
        Element cuerpo = hijo(doc.getDocumentElement(), "Body");
        if (cuerpo != null && hijo(cuerpo, "Fault") != null) {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED);
        }
        Element operacion = cuerpo == null ? null : primerElemento(cuerpo);
        Element respuesta = operacion == null ? null : hijo(operacion, ELEMENTO_RESPUESTA);
        if (respuesta == null) {
            throw new ExcepcionServicio(ExcepcionServicio.SRV_000, ExcepcionServicio.MENSAJE_SRV_000);
        }
        return respuesta;
    }

    /** Copia estado, código y mensaje de la respuesta al DTO. */
    public static void llenarBase(RespuestaBaseDto dto, Element respuesta) {
        dto.setEstado(texto(respuesta, "estado"));
        dto.setCodigo(texto(respuesta, "codigo"));
        dto.setMensaje(texto(respuesta, "mensaje"));
    }

    /** Texto del hijo directo con ese nombre local, o null si no existe (todos los campos son opcionales). */
    public static String texto(Element padre, String nombre) {
        Element e = hijo(padre, nombre);
        return e == null ? null : e.getTextContent();
    }

    /** Hijo directo con ese nombre local, o null. */
    public static Element hijo(Element padre, String nombre) {
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node n = hijos.item(i);
            if (n instanceof Element && nombre.equals(nombreLocal(n))) {
                return (Element) n;
            }
        }
        return null;
    }

    /** Textos de los hijos "item" dentro del hijo "contenedor"; lista vacía si el contenedor falta o viene vacío. */
    public static List<String> lista(Element padre, String contenedor, String item) {
        List<String> valores = new ArrayList<>();
        Element c = hijo(padre, contenedor);
        if (c != null) {
            NodeList hijos = c.getChildNodes();
            for (int i = 0; i < hijos.getLength(); i++) {
                Node n = hijos.item(i);
                if (n instanceof Element && item.equals(nombreLocal(n))) {
                    valores.add(n.getTextContent());
                }
            }
        }
        return valores;
    }

    // ------------------------------------------------------------------ internos

    /** Escapa los cinco caracteres reservados de XML y descarta los caracteres de control no válidos. */
    static String escapar(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default:
                    if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private static Document parsear(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new ExcepcionServicio(ExcepcionServicio.SRV_000, ExcepcionServicio.MENSAJE_SRV_000);
        }
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setNamespaceAware(true);
            try {
                // Evita entidades externas; algunas plataformas no soportan la propiedad.
                fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            } catch (Exception ignorada) {
                // sin protección adicional en esa plataforma
            }
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            return constructor.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new ExcepcionServicio(ExcepcionServicio.SRV_000, ExcepcionServicio.MENSAJE_SRV_000, e);
        }
    }

    private static Element primerElemento(Element padre) {
        NodeList hijos = padre.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            if (hijos.item(i) instanceof Element) {
                return (Element) hijos.item(i);
            }
        }
        return null;
    }

    private static String nombreLocal(Node n) {
        String local = n.getLocalName();
        return local != null ? local : n.getNodeName();
    }
}
