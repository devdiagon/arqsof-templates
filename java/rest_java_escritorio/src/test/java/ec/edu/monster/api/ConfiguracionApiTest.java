package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ConfiguracionApiTest {

    @AfterEach
    void limpiar() {
        System.clearProperty("api.base.url");
    }

    @Test
    void normalizaConUnaSolaBarraFinalYSinEspacios() {
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("  http://h:8080/ctx/api  "));
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("http://h:8080/ctx/api///"));
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("http://h:8080/ctx/api/"));
    }

    @Test
    void laPropiedadSobrescribeLaUrlPorDefecto() {
        System.setProperty("api.base.url", "http://otro:9090/X/api");

        assertEquals("http://otro:9090/X/api/", ConfiguracionApi.urlBase());
    }

    @Test
    void sinPropiedadLaUrlTerminaEnBarraYLlevaElPrefijoApi() {
        System.setProperty("api.base.url", " ");

        String url = ConfiguracionApi.urlBase();

        assertTrue(url.endsWith("/"));
        assertTrue(url.endsWith("/api/"));
    }
}
