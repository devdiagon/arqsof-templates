package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.Textos;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class EjecutorAsincronoTest {

    @Test
    void entregaElResultadoEnElHiloDeEventos() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        AtomicReference<String> recibido = new AtomicReference<>();
        AtomicBoolean enHiloDeEventos = new AtomicBoolean();
        AtomicBoolean tareaEnHiloDeEventos = new AtomicBoolean(true);

        EjecutorAsincrono.ejecutar(
                () -> {
                    tareaEnHiloDeEventos.set(SwingUtilities.isEventDispatchThread());
                    return "ok";
                },
                r -> {
                    recibido.set(r);
                    enHiloDeEventos.set(SwingUtilities.isEventDispatchThread());
                    listo.countDown();
                },
                e -> listo.countDown());

        assertTrue(listo.await(5, TimeUnit.SECONDS));
        assertEquals("ok", recibido.get());
        assertTrue(enHiloDeEventos.get(), "el resultado debe llegar en el hilo de eventos");
        assertFalse(tareaEnHiloDeEventos.get(), "la tarea no debe ejecutarse en el hilo de eventos");
    }

    @Test
    void entregaElErrorDeNegocioTalCual() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        AtomicReference<ExcepcionServicio> error = new AtomicReference<>();
        AtomicBoolean enHiloDeEventos = new AtomicBoolean();

        EjecutorAsincrono.ejecutar(
                () -> {
                    throw new ExcepcionServicio("DATA-001", "El valor debe ser numerico");
                },
                r -> listo.countDown(),
                e -> {
                    error.set(e);
                    enHiloDeEventos.set(SwingUtilities.isEventDispatchThread());
                    listo.countDown();
                });

        assertTrue(listo.await(5, TimeUnit.SECONDS));
        assertEquals("DATA-001", error.get().getCodigo());
        assertEquals("El valor debe ser numerico", error.get().getMessage());
        assertTrue(enHiloDeEventos.get());
    }

    @Test
    void conviertePanicosInesperadosEnErrorControlado() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        AtomicReference<ExcepcionServicio> error = new AtomicReference<>();

        EjecutorAsincrono.ejecutar(
                () -> {
                    throw new IllegalStateException("fallo interno");
                },
                r -> listo.countDown(),
                e -> {
                    error.set(e);
                    listo.countDown();
                });

        assertTrue(listo.await(5, TimeUnit.SECONDS));
        assertEquals("ERROR", error.get().getCodigo());
        assertEquals(Textos.ERROR_INESPERADO, error.get().getMessage());
        assertInstanceOf(IllegalStateException.class, error.get().getCause());
    }
}
