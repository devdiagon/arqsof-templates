package ec.edu.monster.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClienteApiHttpTest {

    @Test
    void quitaBarrasFinalesYEspacios() {
        assertEquals("http://h:8080/api", ClienteApiHttp.quitarBarraFinal(" http://h:8080/api// "));
    }

    @Test
    void codificaSegmentosConPorcientoVeinte() {
        assertEquals("a%20b%C3%B3", ClienteApiHttp.codificarSegmento("a bó"));
    }

    @Test
    void exitoSoloEnRango2xx() {
        assertTrue(new RespuestaHttp(200, "{}").esExito());
        assertTrue(!new RespuestaHttp(401, "{}").esExito());
    }
}
