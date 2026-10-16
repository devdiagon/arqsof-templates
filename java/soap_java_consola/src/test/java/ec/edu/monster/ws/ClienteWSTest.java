package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClienteWSTest {

    @Test
    void quitaBarrasFinalesYEspacios() {
        assertEquals("http://h:8080/ctx", ClienteWS.quitarBarraFinal(" http://h:8080/ctx// "));
    }
}
