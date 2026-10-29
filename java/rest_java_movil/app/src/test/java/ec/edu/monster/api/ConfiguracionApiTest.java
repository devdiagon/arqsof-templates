package ec.edu.monster.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

public class ConfiguracionApiTest {

    @After
    public void limpiar() {
        ConfiguracionApi.restablecer();
    }

    @Test
    public void normalizaConUnaSolaBarraFinalYSinEspacios() {
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("  http://h:8080/ctx/api  "));
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("http://h:8080/ctx/api///"));
        assertEquals("http://h:8080/ctx/api/", ConfiguracionApi.normalizarUrl("http://h:8080/ctx/api/"));
    }

    @Test
    public void laUrlFijadaSeEntregaNormalizada() {
        ConfiguracionApi.fijarUrlBase(" http://10.0.2.2:8080/X/api ");

        String url = ConfiguracionApi.urlBase();

        assertEquals("http://10.0.2.2:8080/X/api/", url);
        assertTrue(url.endsWith("/api/"));
    }

    @Test
    public void sinUrlFijadaFallaEnLugarDeUsarUnaPorDefecto() {
        assertThrows(IllegalStateException.class, ConfiguracionApi::urlBase);
    }
}
