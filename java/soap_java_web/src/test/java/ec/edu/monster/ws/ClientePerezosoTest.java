package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ClientePerezosoTest {

    /** Cliente de ejemplo: una sola operación. */
    interface Eco {
        String repetir(String texto);
    }

    @Test
    void creaElClienteUnaSolaVezYLoReutiliza() {
        AtomicInteger creaciones = new AtomicInteger();
        Eco cliente = ClientePerezoso.de(Eco.class, () -> {
            creaciones.incrementAndGet();
            return texto -> texto;
        });

        assertEquals(0, creaciones.get(), "no se crea hasta la primera llamada");
        cliente.repetir("a");
        cliente.repetir("b");

        assertEquals(1, creaciones.get());
    }

    @Test
    void siLaCreacionFallaReintentaEnLaSiguienteLlamada() {
        AtomicInteger intentos = new AtomicInteger();
        Eco cliente = ClientePerezoso.de(Eco.class, () -> {
            if (intentos.incrementAndGet() == 1) {
                throw new IllegalStateException("servidor apagado");
            }
            return texto -> "ok " + texto;
        });

        assertThrows(IllegalStateException.class, () -> cliente.repetir("a"));
        assertEquals("ok a", cliente.repetir("a"));
        assertEquals(2, intentos.get());
    }

    @Test
    void laExcepcionDelClienteLlegaSinEnvolver() {
        IllegalArgumentException esperada = new IllegalArgumentException("fallo del cliente");
        Eco cliente = ClientePerezoso.de(Eco.class, () -> texto -> {
            throw esperada;
        });

        assertSame(esperada, assertThrows(IllegalArgumentException.class, () -> cliente.repetir("a")));
    }
}
