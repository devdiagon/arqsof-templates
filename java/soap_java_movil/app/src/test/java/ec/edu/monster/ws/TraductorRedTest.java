package ec.edu.monster.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.excepciones.ExcepcionServicio;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.junit.Test;

public class TraductorRedTest {

    @Test
    public void unaLlamadaCorrectaDevuelveSuResultado() {
        assertEquals("listo", TraductorRed.llamar(() -> "listo"));
    }

    @Test
    public void unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa() {
        IOException causa = new IOException("Connection refused");

        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw causa;
        }));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_RED, e.getMessage());
        assertSame(causa, e.getCause());
        assertTrue(e.servidorNoDisponible());
    }

    @Test
    public void unTiempoAgotadoTambienSeTraduceACodigoRed() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw new SocketTimeoutException("Read timed out");
        }));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
    }

    @Test
    public void elMensajeDeRedNoExponeDetallesTecnicos() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw new IOException("java.net.ConnectException: localhost:8080");
        }));

        assertFalse(e.getMessage().contains("localhost"));
    }

    @Test
    public void unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed() {
        assertThrows(IllegalStateException.class, () -> TraductorRed.llamar(() -> {
            throw new IllegalStateException("error de programación");
        }));
    }
}
