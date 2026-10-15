package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.excepciones.ExcepcionServicio;
import org.junit.jupiter.api.Test;

class ClienteApiHttpTest {

    @Test
    void laBaseSeLimpiaDeBarrasFinales() {
        String anterior = System.getProperty("api.base.url");
        try {
            System.setProperty("api.base.url", " http://host:8080/App/api// ");
            assertEquals("http://host:8080/App/api", ClienteApiHttp.baseConfigurada());
        } finally {
            if (anterior == null) {
                System.clearProperty("api.base.url");
            } else {
                System.setProperty("api.base.url", anterior);
            }
        }
    }

    @Test
    void sinServidorLanzaServidorNoDisponible() {
        ClienteApi api = new ClienteApiHttp("http://127.0.0.1:1/api/");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> api.enviar(ClienteApi.GET, "/x", null, null));
        assertTrue(e.servidorNoDisponible());
    }
}
