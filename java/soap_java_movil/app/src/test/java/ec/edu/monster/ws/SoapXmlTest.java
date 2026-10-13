package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaListaDto;
import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/** Las respuestas son del formato real que devuelven los servidores de esta familia (Payara, JAX-WS). */
public class SoapXmlTest {

    private static final String CAB = "<?xml version='1.0' encoding='UTF-8'?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>";
    private static final String PIE = "</S:Body></S:Envelope>";

    private static final String LOGIN_OK = CAB
            + "<ns2:loginResponse xmlns:ns2=\"http://ws.monster.edu.ec/\"><respuesta><estado>OK</estado><codigo>200</codigo>"
            + "<mensaje>Ingreso realizado correctamente</mensaje><token>99537fe655084389acc1de0e2829f0af</token></respuesta></ns2:loginResponse>" + PIE;

    private static final String LOGIN_ERROR = CAB
            + "<ns2:loginResponse xmlns:ns2=\"http://ws.monster.edu.ec/\"><respuesta><estado>ERROR</estado><codigo>AUTH-001</codigo>"
            + "<mensaje>Usuario o contrasenia incorrectos</mensaje></respuesta></ns2:loginResponse>" + PIE;

    private static final String LISTA_OK = CAB
            + "<ns2:obtenerItemsResponse xmlns:ns2=\"http://ws.monster.edu.ec/\"><respuesta><estado>OK</estado><codigo>200</codigo>"
            + "<mensaje>Consulta realizada correctamente</mensaje><elementos><elemento>Uno</elemento><elemento>Dos</elemento>"
            + "<elemento>Tres</elemento></elementos></respuesta></ns2:obtenerItemsResponse>" + PIE;

    private static final String LISTA_ERROR = CAB
            + "<ns2:obtenerItemsResponse xmlns:ns2=\"http://ws.monster.edu.ec/\"><respuesta><estado>ERROR</estado><codigo>AUTH-002</codigo>"
            + "<mensaje>Token invalido, acceso no autorizado</mensaje><elementos/></respuesta></ns2:obtenerItemsResponse>" + PIE;

    private static final String FAULT = CAB
            + "<S:Fault xmlns:ns4=\"http://www.w3.org/2003/05/soap-envelope\"><faultcode>S:Server</faultcode>"
            + "<faultstring>java.lang.NullPointerException</faultstring></S:Fault>" + PIE;

    @Test
    public void loginCorrectoTraeElToken() {
        RespuestaLoginDto r = SoapXml.leerLogin(LOGIN_OK);
        assertTrue(r.esOk());
        assertEquals("200", r.getCodigo());
        assertEquals("99537fe655084389acc1de0e2829f0af", r.getToken());
    }

    @Test
    public void loginConErrorNoTraeToken() {
        RespuestaLoginDto r = SoapXml.leerLogin(LOGIN_ERROR);
        assertFalse(r.esOk());
        assertEquals("AUTH-001", r.getCodigo());
        assertEquals("Usuario o contrasenia incorrectos", r.getMensaje());
        assertNull(r.getToken());
    }

    @Test
    public void listaTraeLosElementosEnOrden() {
        RespuestaListaDto r = SoapXml.leerLista(LISTA_OK);
        assertTrue(r.esOk());
        assertEquals(Arrays.asList("Uno", "Dos", "Tres"), r.getElementos());
    }

    @Test
    public void listaConErrorLlegaVaciaYNoNula() {
        RespuestaListaDto r = SoapXml.leerLista(LISTA_ERROR);
        assertEquals("AUTH-002", r.getCodigo());
        assertTrue(r.getElementos().isEmpty());
    }

    @Test
    public void leeIgualConOtroPrefijoDeNamespace() {
        assertEquals("99537fe655084389acc1de0e2829f0af", SoapXml.leerLogin(LOGIN_OK.replace("ns2", "zz")).getToken());
    }

    @Test
    public void faultEsFalloDeComunicacion() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> SoapXml.leerLogin(FAULT));
        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertFalse(e.getMessage().contains("NullPointer"));
    }

    @Test
    public void cuerpoVacioOInvalidoEsFalloDeComunicacion() {
        assertEquals(ExcepcionServicio.RED, assertThrows(ExcepcionServicio.class, () -> SoapXml.leerLista("")).getCodigo());
        assertEquals(ExcepcionServicio.RED, assertThrows(ExcepcionServicio.class, () -> SoapXml.leerLista("<html>502")).getCodigo());
        assertEquals(ExcepcionServicio.RED, assertThrows(ExcepcionServicio.class, () -> SoapXml.leerLista(CAB + PIE)).getCodigo());
    }

    @Test
    public void peticionRespetaElOrdenYElNamespace() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("usuario", "admin");
        p.put("contrasenia", "clave");
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>"
                + "<ns:login xmlns:ns=\"http://ws.monster.edu.ec/\"><usuario>admin</usuario><contrasenia>clave</contrasenia></ns:login>"
                + "</S:Body></S:Envelope>", SoapXml.construirPeticion("http://ws.monster.edu.ec/", "login", p));
    }

    @Test
    public void peticionUsaElNamespaceIndicado() {
        String xml = SoapXml.construirPeticion("http://ws.otro.dominio/", "operacion", new LinkedHashMap<String, String>());
        assertTrue(xml.contains("<ns:operacion xmlns:ns=\"http://ws.otro.dominio/\"></ns:operacion>"));
    }

    @Test
    public void peticionEscapaCaracteresReservadosYConservaTildes() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("contrasenia", "a&b<c>\"d'");
        p.put("nombre", "Kilómetro");
        String xml = SoapXml.construirPeticion("http://ws.monster.edu.ec/", "login", p);
        assertTrue(xml.contains("<contrasenia>a&amp;b&lt;c&gt;&quot;d&apos;</contrasenia>"));
        assertTrue(xml.contains("<nombre>Kilómetro</nombre>"));
    }
}
