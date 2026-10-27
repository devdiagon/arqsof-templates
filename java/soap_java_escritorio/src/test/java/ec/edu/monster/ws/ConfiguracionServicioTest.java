package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ec.edu.monster.ws.ejemplo.WSEjemplo_Service;
import jakarta.xml.ws.BindingProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class ConfiguracionServicioTest {

    @AfterEach
    void limpiar() {
        System.clearProperty("ws.url");
        System.clearProperty("ws.url.consulta");
            }

    @Test
    void quitaBarrasFinalesYEspacios() {
        assertEquals("http://h:8080/ctx/Servicio", ConfiguracionServicio.normalizarUrl(" http://h:8080/ctx/Servicio// "));
    }

    @Test
    void laPropiedadWsUrlTienePrioridadYSeNormaliza() {
        System.setProperty("ws.url", " http://otro:5267/RutaLibre/ ");

        assertEquals("http://otro:5267/RutaLibre", ConfiguracionServicio.urlConfigurada());
    }

    @Test
    void sinConfiguracionSeUsaLaUrlPorDefectoCompleta() {
        // Si hay una variable de entorno WS_URL en la máquina de pruebas, esta prueba no aplica.
        Assumptions.assumeTrue(System.getenv("WS_URL") == null);

        assertEquals(ConfiguracionServicio.URL_POR_DEFECTO, ConfiguracionServicio.urlConfigurada());
    }

    @Test
    void configurarFijaLaUrlCompletaSinAnadirRutas() {
        BindingProvider puerto = (BindingProvider) ConfiguracionServicio.configurar(new WSEjemplo_Service().getWSEjemploPort(), " http://h:1/Ruta// ");

        assertEquals("http://h:1/Ruta", puerto.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));
    }

    // ------------------------------------------------------------------ varios servicios (una URL completa por servicio)

    @Test
    void laPropiedadWsUrlConElNombreDelServicioTienePrioridadYSeNormaliza() {
        Assumptions.assumeTrue(System.getenv("WS_URL_CONSULTA") == null);
        System.setProperty("ws.url.consulta", " http://otro:5267/RutaLibre/ ");

        assertEquals("http://otro:5267/RutaLibre",
                ConfiguracionServicio.urlConfigurada("consulta", "http://h:8080/ctx/Consulta"));
    }

    @Test
    void sinConfiguracionDelServicioSeUsaSuUrlPorDefectoNormalizada() {
        Assumptions.assumeTrue(System.getenv("WS_URL_CONSULTA") == null);

        assertEquals("http://h:8080/ctx/Consulta",
                ConfiguracionServicio.urlConfigurada("consulta", " http://h:8080/ctx/Consulta/ "));
    }

    @Test
    void cadaServicioSeConfiguraPorSeparadoSinAfectarALosDemas() {
        Assumptions.assumeTrue(System.getenv("WS_URL_CONSULTA") == null && System.getenv("WS_URL_OPERACION") == null);
        System.setProperty("ws.url.consulta", "http://uno:1/A");

        assertEquals("http://uno:1/A", ConfiguracionServicio.urlConfigurada("consulta", "http://x/Consulta"));
        assertEquals("http://x/Operacion", ConfiguracionServicio.urlConfigurada("operacion", "http://x/Operacion"));
    }
}
