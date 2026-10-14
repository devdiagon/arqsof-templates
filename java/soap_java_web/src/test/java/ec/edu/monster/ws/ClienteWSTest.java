package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.dto.RespuestaLoginDto;
import jakarta.xml.ws.WebServiceException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ClienteWSTest {

    private static final ContratoWS PUERTO_FALSO = (usuario, contrasenia) -> new RespuestaLoginDto();

    @Test
    void creaElPuertoUnaSolaVezYLoReutiliza() {
        AtomicInteger creaciones = new AtomicInteger();
        ContratoWS contrato = ClienteWS.perezoso(() -> {
            creaciones.incrementAndGet();
            return PUERTO_FALSO;
        });
        assertEquals(0, creaciones.get(), "no debe crearse hasta la primera llamada");
        contrato.login("u", "c");
        contrato.login("u", "c");
        assertEquals(1, creaciones.get());
    }

    @Test
    void siLaCreacionFallaReintentaEnLaSiguienteLlamada() {
        AtomicInteger intentos = new AtomicInteger();
        ContratoWS contrato = ClienteWS.perezoso(() -> {
            if (intentos.incrementAndGet() == 1) {
                throw new WebServiceException("servidor apagado");
            }
            return PUERTO_FALSO;
        });
        assertThrows(WebServiceException.class, () -> contrato.login("u", "c"));
        assertNotNull(contrato.login("u", "c"));
        assertEquals(2, intentos.get());
    }

    @Test
    void laExcepcionDelPuertoLlegaSinEnvolver() {
        ContratoWS contrato = ClienteWS.perezoso(() -> (usuario, contrasenia) -> {
            throw new WebServiceException("sin red");
        });
        assertEquals("sin red", assertThrows(WebServiceException.class, () -> contrato.login("u", "c")).getMessage());
    }

    @Test
    void laBaseSeLimpiaDeBarrasFinales() {
        String anterior = System.getProperty("ws.base.url");
        try {
            System.setProperty("ws.base.url", " http://host:8080/App// ");
            assertEquals("http://host:8080/App", ClienteWS.baseConfigurada());
        } finally {
            if (anterior == null) {
                System.clearProperty("ws.base.url");
            } else {
                System.setProperty("ws.base.url", anterior);
            }
        }
    }
}
