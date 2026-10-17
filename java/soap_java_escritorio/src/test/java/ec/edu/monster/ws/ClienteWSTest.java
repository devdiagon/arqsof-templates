package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void laSoapActionSigueLaConvencion() {
        assertEquals(ConfiguracionServicio.NAMESPACE + ConfiguracionServicio.NOMBRE_SERVICIO + "/login",
                ConfiguracionServicio.accion("login"));
    }
}
