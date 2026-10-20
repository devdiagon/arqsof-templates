package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.ws.ejemplo.WSEjemplo_Service;
import jakarta.xml.ws.BindingProvider;
import org.junit.jupiter.api.AfterEach;
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
        // Si hay una variable de entorno WS_URL en la máquina de pruebas, esta prueba no aplica.
        org.junit.jupiter.api.Assumptions.assumeTrue(System.getenv("WS_URL") == null);

        assertEquals(ClienteWS.URL_POR_DEFECTO, ClienteWS.urlConfigurada());
    }

    @Test
    void configurarFijaLaUrlCompletaSinAnadirRutas() {
        BindingProvider puerto = (BindingProvider) ClienteWS.configurar(new WSEjemplo_Service().getWSEjemploPort(), " http://h:1/Ruta// ");

        assertEquals("http://h:1/Ruta", puerto.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
    }
}
