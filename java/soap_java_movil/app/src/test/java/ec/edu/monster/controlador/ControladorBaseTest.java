package ec.edu.monster.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.Textos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.Rule;
import org.junit.Test;

/** Las invariantes del molde (ARQUITECTURA_CLIENTE.md §4.6): sin emulador ni servidor, con ejecutor controlado. */
public class ControladorBaseTest {

    @Rule
    public InstantTaskExecutorRule reglaLiveData = new InstantTaskExecutorRule();

    /** Ejecutor que guarda las tareas para correrlas cuando la prueba lo decida (simula una llamada lenta). */
    private static class EjecutorManual implements Executor {
        final List<Runnable> pendientes = new ArrayList<>();

        @Override
        public void execute(Runnable tarea) {
            pendientes.add(tarea);
        }

        void correrTodo() {
            List<Runnable> tareas = new ArrayList<>(pendientes);
            pendientes.clear();
            tareas.forEach(Runnable::run);
        }
    }

    /** Controlador mínimo: los datos son el último texto recibido. */
    private static class ControladorDePrueba extends ControladorBase<String> {
        int llamadas;

        ControladorDePrueba(Executor ejecutor) {
            super(ejecutor, "inicial");
        }

        void avisar(String mensaje) {
            informar(mensaje);
        }

        void consultar(String codigo, java.util.function.Supplier<String> servicio) {
            if (vacio(codigo)) {
                rechazar(Textos.campoObligatorio("Código de cuenta"));
                return;
            }
            ejecutar(Textos.CONSULTANDO, () -> {
                llamadas++;
                return servicio.get();
            }, resultado -> exito(resultado, "Listo"));
        }
    }

    private static ControladorBase.Estado<String> estado(ControladorDePrueba c) {
        return c.getEstado().getValue();
    }

    @Test
    public void elEstadoInicialEstaLibreYTraeLosDatosIniciales() {
        ControladorBase.Estado<String> e = estado(new ControladorDePrueba(Runnable::run));

        assertFalse(e.ocupado);
        assertNull(e.mensaje);
        assertFalse(e.error);
        assertEquals("inicial", e.datos);
    }

    @Test
    public void unCampoVacioMuestraElMensajeDeValidacionYNoLlamaAlServicio() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.consultar("  ", () -> "x");

        assertEquals(0, c.llamadas);
        assertEquals("El campo código de cuenta es obligatorio", estado(c).mensaje);
        assertTrue(estado(c).error);
        assertEquals("inicial", estado(c).datos);
    }

    @Test
    public void informarMuestraUnMensajeQueNoEsErrorYConservaLosDatos() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.avisar("Sesión cerrada");

        assertEquals("Sesión cerrada", estado(c).mensaje);
        assertFalse(estado(c).error);
        assertEquals("inicial", estado(c).datos);
    }

    @Test
    public void unaValidacionOUnAvisoNoInterrumpenUnaLlamadaEnCurso() {
        EjecutorManual ejecutor = new EjecutorManual();
        ControladorDePrueba c = new ControladorDePrueba(ejecutor);
        c.consultar("00100001", () -> "saldo");

        c.consultar("", () -> "x");
        c.avisar("aviso");

        assertTrue(estado(c).ocupado);
        assertEquals(Textos.CONSULTANDO, estado(c).mensaje);
    }

    @Test
    public void unaLlamadaCorrectaPublicaElResultadoYLiberaElEstado() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.consultar("00100001", () -> "saldo");

        ControladorBase.Estado<String> e = estado(c);
        assertFalse(e.ocupado);
        assertFalse(e.error);
        assertEquals("Listo", e.mensaje);
        assertEquals("saldo", e.datos);
    }

    @Test
    public void mientrasEsperaEstaOcupadoConElTextoDelIndicadorYUnaSegundaAccionSeIgnora() {
        EjecutorManual ejecutor = new EjecutorManual();
        ControladorDePrueba c = new ControladorDePrueba(ejecutor);

        c.consultar("00100001", () -> "saldo");
        c.consultar("00100001", () -> "otro");

        assertTrue(estado(c).ocupado);
        assertEquals(Textos.CONSULTANDO, estado(c).mensaje);
        assertEquals("solo se envía una petición", 1, ejecutor.pendientes.size());

        ejecutor.correrTodo();

        assertEquals(1, c.llamadas);
        assertFalse(estado(c).ocupado);
        assertEquals("saldo", estado(c).datos);
    }

    @Test
    public void unErrorDelServicioMuestraSoloElMensajeYConservaLosDatos() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.consultar("00100001", () -> {
            throw new ExcepcionServicio("CTA-004", "Saldo insuficiente");
        });

        ControladorBase.Estado<String> e = estado(c);
        assertFalse(e.ocupado);
        assertTrue(e.error);
        assertEquals("Saldo insuficiente", e.mensaje);
        assertEquals("inicial", e.datos);
    }

    @Test
    public void unaCaidaDeRedMuestraElMensajeGenericoDeServidorNoDisponible() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.consultar("00100001", () -> {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED);
        });

        assertEquals("Servidor no disponible, intente más tarde", estado(c).mensaje);
    }

    @Test
    public void unErrorInesperadoMuestraUnMensajeGenericoSinDetallesTecnicos() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);

        c.consultar("00100001", () -> {
            throw new IllegalStateException("fallo interno en la línea 42");
        });

        assertTrue(estado(c).error);
        assertEquals(Textos.ERROR_INESPERADO, estado(c).mensaje);
        assertFalse(estado(c).ocupado);
    }

    @Test
    public void despuesDeUnErrorSePuedeReintentar() {
        ControladorDePrueba c = new ControladorDePrueba(Runnable::run);
        c.consultar("00100001", () -> {
            throw new ExcepcionServicio(ExcepcionServicio.RED, ExcepcionServicio.MENSAJE_RED);
        });

        c.consultar("00100001", () -> "saldo");

        assertEquals(2, c.llamadas);
        assertEquals("saldo", estado(c).datos);
        assertFalse(estado(c).error);
    }
}
