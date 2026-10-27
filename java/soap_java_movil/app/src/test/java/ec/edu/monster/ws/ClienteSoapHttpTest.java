package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.net.ServerSocket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Traducción de los fallos del transporte: sin respuesta o Fault → RED; respuesta que no se entiende → SRV-000. */
public class ClienteSoapHttpTest {

    private ServidorFalso servidor;
    private ClienteSoapHttp cliente;

    @Before
    public void levantar() throws Exception {
        servidor = new ServidorFalso();
        cliente = new ClienteSoapHttp(servidor.url("/Servicio"), "Servicio");
    }

    @After
    public void detener() throws Exception {
        servidor.close();
    }

    private ExcepcionServicio fallo() {
        return assertThrows(ExcepcionServicio.class, () -> cliente.enviar("op", SoapXml.parametros()));
    }

    @Test
    public void sinServidorEsCodigoRedConElMensajeGenerico() throws Exception {
        int puertoCerrado;
        try (ServerSocket libre = new ServerSocket(0)) {
            puertoCerrado = libre.getLocalPort();
        }
        ClienteSoapHttp sinServidor = new ClienteSoapHttp("http://localhost:" + puertoCerrado + "/Servicio", "Servicio");

        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> sinServidor.enviar("op", SoapXml.parametros()));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_RED, e.getMessage());
        assertTrue(e.servidorNoDisponible());
    }

    @Test
    public void unFaultEsCodigoRed() {
        servidor.estadoHttp = 500;
        servidor.respuesta = ServidorFalso.sobre("<S:Fault><faultcode>S:Server</faultcode><faultstring>boom</faultstring></S:Fault>");

        assertEquals(ExcepcionServicio.RED, fallo().getCodigo());
    }

    @Test
    public void unEstadoHttpInesperadoEsCodigoRed() {
        servidor.estadoHttp = 404;
        servidor.respuesta = "<html>No encontrado</html>";

        assertEquals(ExcepcionServicio.RED, fallo().getCodigo());
    }

    @Test
    public void unCuerpoQueNoEsXmlEsRespuestaIlegible() {
        servidor.respuesta = "<html><body>Portal cautivo</body></html";

        ExcepcionServicio e = fallo();

        assertEquals(ExcepcionServicio.SRV_000, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_SRV_000, e.getMessage());
        assertTrue(e.respuestaIlegible());
    }

    @Test
    public void unCuerpoVacioEsRespuestaIlegible() {
        servidor.respuesta = "";

        assertEquals(ExcepcionServicio.SRV_000, fallo().getCodigo());
    }
}
