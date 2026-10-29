package ec.edu.monster.servicios;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import org.junit.Test;

public class ServicioBaseTest {

    /** Expone la verificación protegida de la base para poder probarla. */
    private static class ServicioDePrueba extends ServicioBase {
        <R extends RespuestaBaseDto> R comprobar(R respuesta) {
            return verificar(respuesta);
        }
    }

    private final ServicioDePrueba servicio = new ServicioDePrueba();

    private static RespuestaBaseDto respuesta(String estado, String codigo, String mensaje) {
        RespuestaBaseDto r = new RespuestaBaseDto();
        r.setEstado(estado);
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
        return r;
    }

    @Test
    public void unaRespuestaOkSeDevuelveSinCambios() {
        RespuestaBaseDto ok = respuesta("OK", "200", "Operación exitosa");

        assertSame(ok, servicio.comprobar(ok));
    }

    @Test
    public void unaRespuestaDeErrorLanzaExcepcionConElCodigoYElMensajeDelServidor() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class,
                () -> servicio.comprobar(respuesta("ERROR", "CTA-004", "Saldo insuficiente")));

        assertEquals("CTA-004", e.getCodigo());
        assertEquals("Saldo insuficiente", e.getMessage());
        assertFalse(e.servidorNoDisponible());
    }

    @Test
    public void unErrorTecnicoDelServidorConservaSuCodigoYSuMensajeGenerico() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class,
                () -> servicio.comprobar(respuesta("ERROR", "ERR-500", "Error interno del servidor")));

        assertEquals("ERR-500", e.getCodigo());
        assertEquals("Error interno del servidor", e.getMessage());
    }

    @Test
    public void unaRespuestaNulaSeTrataComoRespuestaIlegible() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.comprobar(null));

        assertEquals(ExcepcionServicio.SRV_000, e.getCodigo());
        assertEquals(ExcepcionServicio.MENSAJE_SRV_000, e.getMessage());
        assertTrue(e.respuestaIlegible());
    }

    @Test
    public void unEstadoDistintoDeOkSeTrataComoError() {
        assertThrows(ExcepcionServicio.class, () -> servicio.comprobar(respuesta(null, null, "sin estado")));
        assertThrows(ExcepcionServicio.class, () -> servicio.comprobar(respuesta("ok", "200", "minúsculas")));
    }
}
