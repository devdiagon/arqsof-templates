package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.w3c.dom.Element;

/** Las respuestas son del formato que devuelven los servidores de esta familia: elementos calificados con el namespace del servicio. */
public class SoapXmlTest {

    private static final String TNS = ConfiguracionServicio.TNS;

    private static final String OK = ServidorFalso.sobre(
            "<ns2:obtenerItemsResponse xmlns:ns2=\"" + TNS + "\"><ns2:respuesta><ns2:estado>OK</ns2:estado>"
                    + "<ns2:codigo>200</ns2:codigo><ns2:mensaje>Consulta realizada correctamente</ns2:mensaje>"
                    + "<ns2:cantidad>3</ns2:cantidad><ns2:items><ns2:item>Uno</ns2:item><ns2:item>Dos</ns2:item>"
                    + "<ns2:item>Tres</ns2:item></ns2:items></ns2:respuesta></ns2:obtenerItemsResponse>");

    private static final String ERROR = ServidorFalso.sobre(
            "<ns2:obtenerItemsResponse xmlns:ns2=\"" + TNS + "\"><ns2:respuesta><ns2:estado>ERROR</ns2:estado>"
                    + "<ns2:codigo>DATO-001</ns2:codigo><ns2:mensaje>El código es obligatorio</ns2:mensaje>"
                    + "<ns2:items/></ns2:respuesta></ns2:obtenerItemsResponse>");

    private static final String FAULT = ServidorFalso.sobre(
            "<S:Fault><faultcode>S:Server</faultcode><faultstring>Error interno</faultstring></S:Fault>");

    private static RespuestaBaseDto base(String xml) {
        RespuestaBaseDto dto = new RespuestaBaseDto();
        SoapXml.llenarBase(dto, SoapXml.extraerRespuesta(xml));
        return dto;
    }

    @Test
    public void respuestaOkTraeEstadoCodigoYMensaje() {
        RespuestaBaseDto dto = base(OK);

        assertTrue(dto.esOk());
        assertEquals("200", dto.getCodigo());
        assertEquals("Consulta realizada correctamente", dto.getMensaje());
    }

    @Test
    public void respuestaConErrorDeNegocioNoEsOkYConservaSuCodigo() {
        RespuestaBaseDto dto = base(ERROR);

        assertFalse(dto.esOk());
        assertEquals("DATO-001", dto.getCodigo());
        assertEquals("El código es obligatorio", dto.getMensaje());
    }

    @Test
    public void textoLeeUnCampoPropioYDevuelveNuloSiFalta() {
        Element r = SoapXml.extraerRespuesta(OK);

        assertEquals("3", SoapXml.texto(r, "cantidad"));
        assertNull(SoapXml.texto(r, "noExiste"));
    }

    @Test
    public void listaTraeLosElementosEnOrden() {
        Element r = SoapXml.extraerRespuesta(OK);

        assertEquals(Arrays.asList("Uno", "Dos", "Tres"), SoapXml.lista(r, "items", "item"));
    }

    @Test
    public void listaVaciaOAusenteLlegaVaciaYNoNula() {
        assertEquals(Collections.emptyList(), SoapXml.lista(SoapXml.extraerRespuesta(ERROR), "items", "item"));
        assertEquals(Collections.emptyList(), SoapXml.lista(SoapXml.extraerRespuesta(OK), "otraLista", "item"));
    }

    @Test
    public void elementosTraeLosObjetosDeUnaListaEnOrdenYPermiteLeerSusCampos() {
        String xml = ServidorFalso.sobre("<opResponse xmlns=\"" + TNS + "\"><respuesta><estado>OK</estado>"
                + "<filas><fila><a>1</a><b>x</b></fila><fila><a>2</a></fila></filas></respuesta></opResponse>");

        List<Element> filas = SoapXml.elementos(SoapXml.extraerRespuesta(xml), "filas", "fila");

        assertEquals(2, filas.size());
        assertEquals("1", SoapXml.texto(filas.get(0), "a"));
        assertEquals("x", SoapXml.texto(filas.get(0), "b"));
        assertNull("un campo opcional ausente es nulo", SoapXml.texto(filas.get(1), "b"));
        assertEquals(Collections.emptyList(), SoapXml.elementos(SoapXml.extraerRespuesta(ERROR), "items", "item"));
    }

    @Test
    public void leeIgualConOtroPrefijoDeNamespace() {
        String xml = OK.replace("ns2:", "otro:").replace("xmlns:ns2", "xmlns:otro");

        assertTrue(base(xml).esOk());
    }

    @Test
    public void leeUnaRespuestaConCamposCalificadosEnNamespacePorDefecto() {
        String xml = ServidorFalso.sobre("<obtenerItemsResponse xmlns=\"" + TNS + "\"><respuesta><estado>OK</estado>"
                + "<codigo>200</codigo><mensaje>ok</mensaje></respuesta></obtenerItemsResponse>");

        assertTrue(base(xml).esOk());
    }

    @Test
    public void laSoapActionEsLaUriDeLaOperacionEntreComillas() {
        assertEquals("\"http://ws.monster.edu.ec/ServicioReporte/obtenerMovimientos\"",
                SoapXml.accionSoap(TNS, "ServicioReporte", "obtenerMovimientos"));
    }

    @Test
    public void faultEsFalloDeComunicacion() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> SoapXml.extraerRespuesta(FAULT));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_RED, e.getMessage());
    }

    @Test
    public void cuerpoVacioOInvalidoEsRespuestaIlegible() {
        for (String xml : new String[] {null, "", "   ", "<html>no es soap", "texto plano"}) {
            ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> SoapXml.extraerRespuesta(xml));
            assertEquals(ExcepcionServicio.SRV_000, e.getCodigo());
            assertEquals(ExcepcionServicio.MENSAJE_SRV_000, e.getMessage());
        }
    }

    @Test
    public void unSobreSinElElementoRespuestaEsRespuestaIlegible() {
        String sinRespuesta = ServidorFalso.sobre("<ns2:opResponse xmlns:ns2=\"" + TNS + "\"><otro/></ns2:opResponse>");

        assertEquals(ExcepcionServicio.SRV_000,
                assertThrows(ExcepcionServicio.class, () -> SoapXml.extraerRespuesta(sinRespuesta)).getCodigo());
    }

    @Test
    public void peticionRespetaElOrdenYCalificaLosParametros() {
        Map<String, String> p = new LinkedHashMap<>();
        p.put("cuentaOrigen", "00100001");
        p.put("importe", "100.50");

        String xml = SoapXml.construirPeticion(TNS, "realizarRetiro", p);

        assertTrue(xml.contains("<ns:realizarRetiro xmlns:ns=\"" + TNS + "\">"));
        assertTrue(xml.indexOf("<ns:cuentaOrigen>00100001</ns:cuentaOrigen>") < xml.indexOf("<ns:importe>100.50</ns:importe>"));
    }

    @Test
    public void parametrosConservaElOrdenDeLosPares() {
        assertEquals(Arrays.asList("b", "a"), Arrays.asList(SoapXml.parametros("b", "1", "a", "2").keySet().toArray()));
    }

    @Test
    public void peticionEscapaCaracteresReservadosYConservaTildes() {
        String xml = SoapXml.construirPeticion(TNS, "op", SoapXml.parametros("texto", "a<b & \"c\" ñandú"));

        assertTrue(xml.contains("<ns:texto>a&lt;b &amp; &quot;c&quot; ñandú</ns:texto>"));
    }
}
