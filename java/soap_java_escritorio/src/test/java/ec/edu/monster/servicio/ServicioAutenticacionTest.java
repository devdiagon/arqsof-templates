package ec.edu.monster.servicio;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.dto.RespuestaBaseDto;
import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.ws.ClienteServicio;
import jakarta.xml.ws.WebServiceException;
import org.junit.jupiter.api.Test;

class ServicioAutenticacionTest {

    private int llamadas;
    private RespuestaLoginDto respuesta;
    private RuntimeException fallo;
    private final Sesion sesion = new Sesion();

    private final ClienteServicio falso = (usuario, contrasenia) -> {
        llamadas++;
        if (fallo != null) {
            throw fallo;
        }
        return respuesta;
    };

    private final ServicioAutenticacion servicio = new ServicioAutenticacion(falso, sesion);

    private static RespuestaLoginDto login(String estado, String codigo, String mensaje, String token) {
        RespuestaLoginDto r = new RespuestaLoginDto();
        r.setEstado(estado);
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
        r.setToken(token);
        return r;
    }

    @Test
    void camposVaciosNoLlamanAlServidor() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login(" ", ""));
        assertEquals("Ingrese usuario y contraseña", e.getMessage());
        assertEquals(0, llamadas);
    }

    @Test
    void loginCorrectoGuardaElToken() {
        respuesta = login("OK", "200", "ok", "T1");
        servicio.login("u", "c");
        assertEquals("T1", sesion.getToken());
        assertTrue(servicio.haySesion());
    }

    @Test
    void errorDeNegocioLanzaConCodigoYMensajeDelServidor() {
        respuesta = login("ERROR", "AUTH-001", "Credenciales inválidas", null);
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("AUTH-001", e.getCodigo());
        assertEquals("Credenciales inválidas", e.getMessage());
        assertFalse(e.exigeLogin());
        assertFalse(servicio.haySesion());
    }

    @Test
    void fallosDeRedSeTraducenAlCodigoRed() {
        fallo = new WebServiceException("sin conexión");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals(ExcepcionServicio.RED, e.getCodigo());
    }

    @Test
    void respuestaNulaLanzaSrv000() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("SRV-000", e.getCodigo());
    }

    @Test
    void cerrarSesionDescartaElToken() {
        sesion.setToken("T1");
        servicio.cerrarSesion();
        assertFalse(servicio.haySesion());
    }

    /** Un servicio del dominio hereda token() y verificar(): sin sesión no llama al servidor; AUTH-003 descarta la sesión. */
    @Test
    void serviciosDeDominioHeredanLasReglasDeSesion() {
        class ServicioDePrueba extends ServicioBase {
            ServicioDePrueba(Sesion s) {
                super(s);
            }

            String pedirToken() {
                return token();
            }

            <T extends RespuestaBaseDto> T comprobar(T r) {
                return verificar(r);
            }
        }
        Sesion s = new Sesion();
        ServicioDePrueba derivado = new ServicioDePrueba(s);

        ExcepcionServicio sinSesion = assertThrows(ExcepcionServicio.class, derivado::pedirToken);
        assertEquals("AUTH-002", sinSesion.getCodigo());
        assertTrue(sinSesion.exigeLogin());

        s.setToken("T1");
        assertEquals("T1", derivado.pedirToken());
        ExcepcionServicio vencida = assertThrows(ExcepcionServicio.class,
                () -> derivado.comprobar(login("ERROR", "AUTH-003", "Sesión vencida", null)));
        assertTrue(vencida.exigeLogin());
        assertNull(s.getToken(), "un token vencido se descarta");
    }
}
