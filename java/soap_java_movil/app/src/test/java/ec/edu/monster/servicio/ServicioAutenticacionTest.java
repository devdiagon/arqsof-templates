package ec.edu.monster.servicio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.ws.ContratoFalso;
import org.junit.Test;

public class ServicioAutenticacionTest {

    /** Servicio minimo para probar las reglas que heredan todos los servicios del dominio. */
    private static class ServicioDePrueba extends ServicioBase {
        ServicioDePrueba(Sesion sesion) {
            super(sesion);
        }

        <T extends RespuestaBaseDto> T probar(T respuesta) {
            return verificar(respuesta);
        }

        String probarToken() {
            return token();
        }
    }

    private static ServicioAutenticacion servicio() {
        return new ServicioAutenticacion(new ContratoFalso(), new Sesion());
    }

    @Test
    public void loginCorrectoGuardaSesion() {
        ServicioAutenticacion s = servicio();
        s.login("admin", "clave");
        assertTrue(s.haySesion());
    }

    @Test
    public void loginIncorrectoLanzaErrorDelServidor() {
        ServicioAutenticacion s = servicio();
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.login("otro", "x"));
        assertEquals("AUTH-001", e.getCodigo());
        assertFalse(s.haySesion());
    }

    @Test
    public void camposVaciosNoLlamanAlServidor() {
        ServicioAutenticacion s = servicio();
        assertEquals("AUTH-001", assertThrows(ExcepcionServicio.class, () -> s.login(" ", "")).getCodigo());
    }

    @Test
    public void cerrarSesionDescartaElToken() {
        ServicioAutenticacion s = servicio();
        s.login("admin", "clave");
        s.cerrarSesion();
        assertFalse(s.haySesion());
    }

    @Test
    public void sinSesionNoSeObtieneElToken() {
        ServicioDePrueba s = new ServicioDePrueba(new Sesion());
        assertEquals("AUTH-002", assertThrows(ExcepcionServicio.class, s::probarToken).getCodigo());
    }

    @Test
    public void respuestaOkSeDevuelveTalCual() {
        ServicioDePrueba s = new ServicioDePrueba(new Sesion("T1"));
        RespuestaBaseDto ok = ContratoFalso.respuesta(new RespuestaBaseDto(), "OK", "200", "ok");
        assertSame(ok, s.probar(ok));
        assertEquals("T1", s.probarToken());
    }

    @Test
    public void errorDeNegocioMantieneLaSesion() {
        ServicioDePrueba s = new ServicioDePrueba(new Sesion("T1"));
        RespuestaBaseDto error = ContratoFalso.respuesta(new RespuestaBaseDto(), "ERROR", "DATA-001", "dato invalido");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.probar(error));
        assertEquals("DATA-001", e.getCodigo());
        assertEquals("dato invalido", e.getMessage());
        assertTrue(s.haySesion());
    }

    @Test
    public void tokenVencidoOInvalidoDescartaLaSesion() {
        for (String codigo : new String[]{"AUTH-002", "AUTH-003"}) {
            ServicioDePrueba s = new ServicioDePrueba(new Sesion("T1"));
            RespuestaBaseDto error = ContratoFalso.respuesta(new RespuestaBaseDto(), "ERROR", codigo, "sesion");
            ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.probar(error));
            assertTrue(e.exigeLogin());
            assertFalse(s.haySesion());
        }
    }

    @Test
    public void respuestaNulaEsErrorDelServidor() {
        ServicioDePrueba s = new ServicioDePrueba(new Sesion("T1"));
        assertEquals("SRV-000", assertThrows(ExcepcionServicio.class, () -> s.probar(null)).getCodigo());
    }
}
