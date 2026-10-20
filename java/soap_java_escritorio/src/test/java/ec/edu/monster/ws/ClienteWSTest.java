package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ClienteWSTest {

    @AfterEach
    void limpiar() {
        System.clearProperty("ws.url");
    }

    @Test
    void quitaBarrasFinalesYEspacios() {
        assertEquals("http://h:8080/ctx/Servicio", ClienteWS.normalizarUrl(" http://h:8080/ctx/Servicio// "));
    }

    @Test
    void laPropiedadWsUrlTienePrioridadYSeNormaliza() {
        System.setProperty("ws.url", " http://otro:5267/RutaLibre/ ");

        assertEquals("http://otro:5267/RutaLibre", ClienteWS.urlConfigurada());
    }

    @Test
    void sinConfiguracionSeUsaLaUrlPorDefectoCompleta() {
        Assumptions.assumeTrue(System.getenv("WS_URL") == null);

        assertEquals(ConfiguracionServicio.URL_POR_DEFECTO, ClienteWS.urlConfigurada());
    }

    @Test
    void crearNoNecesitaElServidorYElFalloLlegaAlLlamar() {
        // El WSDL va empaquetado: crear el cliente no abre ninguna conexión. Con el servidor apagado falla la llamada.
        ClienteServicio cliente = ClienteWS.crear("http://localhost:1/inexistente");

        assertThrows(WebServiceException.class, () -> cliente.login("u", "c"));
    }
}
