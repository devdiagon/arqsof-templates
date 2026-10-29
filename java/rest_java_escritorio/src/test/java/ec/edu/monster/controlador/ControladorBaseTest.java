package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.InicioVista;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class ControladorBaseTest {

    /** Controlador mínimo para probar el molde. */
    private static final class Controlador extends ControladorBase {
        Controlador(InicioVista vista) {
            super(vista);
        }
    }

    private static InicioVista crearVista() throws Exception {
        InicioVista[] vista = new InicioVista[1];
        SwingUtilities.invokeAndWait(() -> vista[0] = new InicioVista());
        return vista[0];
    }

    @Test
    void unaSegundaAccionSeIgnoraMientrasLaPrimeraSigueEnCurso() throws Exception {
        Controlador controlador = new Controlador(crearVista());
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch terminada = new CountDownLatch(1);
        AtomicInteger llamadas = new AtomicInteger();

        SwingUtilities.invokeAndWait(() -> {
            controlador.ejecutar("Procesando…", () -> {
                llamadas.incrementAndGet();
                esperar(liberar);
                return "ok";
            }, r -> terminada.countDown());
            assertTrue(controlador.estaOcupado());
            controlador.ejecutar("Procesando…", () -> {
                llamadas.incrementAndGet();
                return "segunda";
            }, r -> { });
        });
        liberar.countDown();

        assertTrue(terminada.await(5, TimeUnit.SECONDS));
        assertEquals(1, llamadas.get(), "la segunda acción no llega a ejecutarse");
    }

    @Test
    void unErrorSeMuestraConSuMensajeYLaPantallaQuedaLibre() throws Exception {
        InicioVista vista = crearVista();
        Controlador controlador = new Controlador(vista);

        SwingUtilities.invokeAndWait(() -> controlador.ejecutar("Procesando…", () -> {
            throw new ExcepcionServicio("CTA-004", "Saldo insuficiente");
        }, r -> { }));
        for (int i = 0; i < 100 && vista.getTextoEstado().isEmpty(); i++) {
            Thread.sleep(50);
        }

        assertEquals("Saldo insuficiente", vista.getTextoEstado(), "se muestra el mensaje, nunca el código");
        assertFalse(controlador.estaOcupado());
    }

    private static void esperar(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
