package ec.edu.monster.ws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ec.edu.monster.excepciones.ExcepcionServicio;
import jakarta.xml.ws.ProtocolException;
import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;

class TraductorRedTest {

    @Test
    void unaLlamadaCorrectaDevuelveSuResultado() {
        assertEquals("listo", TraductorRed.llamar(() -> "listo"));
    }

    @Test
    void unFalloDeRedSeTraduceACodigoRedConElMensajeGenericoYConservaLaCausa() {
        WebServiceException causa = new WebServiceException("Connection refused");

        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw causa;
        }));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_RED, e.getMessage());
        assertSame(causa, e.getCause());
        assertTrue(e.servidorNoDisponible());
    }

    @Test
    void unFallaDeProtocoloTambienSeTraduceACodigoRed() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw new ProtocolException("Fault de la plataforma");
        }));

        assertEquals(ExcepcionServicio.RED, e.getCodigo());
    }

    @Test
    void elMensajeDeRedNoExponeDetallesTecnicos() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> TraductorRed.llamar(() -> {
            throw new WebServiceException("java.net.ConnectException: localhost:8080");
        }));

        assertTrue(!e.getMessage().contains("localhost"));
    }

    @Test
    void unErrorDeProgramacionQueNoEsDeRedNoSeDisfrazaDeRed() {
        assertThrows(IllegalStateException.class, () -> TraductorRed.llamar(() -> {
            throw new IllegalStateException("error de programación");
        }));
    }
}
