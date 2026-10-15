package ec.edu.monster.servicio;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.RespuestaHttp;
import ec.edu.monster.dto.RespuestaListaDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicioAutenticacionTest {

    private int llamadas;
    private RespuestaHttp respuesta;
    private RuntimeException fallo;
    private final List<String> peticiones = new ArrayList<>();
    private final Sesion sesion = new Sesion();

    private final ClienteApi falso = (metodo, ruta, cuerpo, token) -> {
        llamadas++;
        peticiones.add(metodo + " " + ruta + " " + cuerpo + " " + token);
        if (fallo != null) {
            throw fallo;
        }
        return respuesta;
    };

    private final ServicioAutenticacion servicio = new ServicioAutenticacion(falso, sesion);

    @Test
    void camposVaciosNoLlamanAlServidor() {
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login(" ", ""));
        assertEquals("Ingrese usuario y contraseña", e.getMessage());
        assertEquals(0, llamadas);
    }

    @Test
    void loginCorrectoGuardaElTokenYEnviaElCuerpoJson() {
        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\",\"token\":\"T1\"}");
        servicio.login("u", "c");
        assertEquals("T1", sesion.getToken());
        assertTrue(servicio.haySesion());
        assertEquals("POST /auth/login {\"usuario\":\"u\",\"contrasenia\":\"c\"} null", peticiones.get(0));
    }

    @Test
    void error401EnLoginMuestraElMensajeDelServidorYNoDescartaNada() {
        respuesta = new RespuestaHttp(401,
                "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-001\",\"mensaje\":\"Credenciales inválidas\",\"token\":null}");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("AUTH-001", e.getCodigo());
        assertEquals("Credenciales inválidas", e.getMessage());
        assertFalse(e.exigeLogin());
        assertFalse(servicio.haySesion());
    }

    @Test
    void servidorNoDisponibleSePropagaConSuMensaje() {
        fallo = ExcepcionServicio.servidorNoDisponible(new java.net.ConnectException("rechazada"));
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertTrue(e.servidorNoDisponible());
        assertEquals("Servidor no disponible, intente más tarde", e.getMessage());
    }

    @Test
    void respuestaNulaOSinTokenLanzaSrv000() {
        ExcepcionServicio nula = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("SRV-000", nula.getCodigo());

        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\"}");
        ExcepcionServicio sinToken = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("SRV-000", sinToken.getCodigo());
        assertFalse(servicio.haySesion());
    }

    @Test
    void cuerpoQueNoEsJsonDaUnMensajeGenericoConElCodigoHttp() {
        respuesta = new RespuestaHttp(500, "<html>Error</html>");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> servicio.login("u", "c"));
        assertEquals("Error del servidor (HTTP 500)", e.getMessage());
        assertEquals("HTTP-500", e.getCodigo());
    }

    @Test
    void cerrarSesionDescartaElToken() {
        sesion.setToken("T1");
        servicio.cerrarSesion();
        assertFalse(servicio.haySesion());
    }

    /** Un servicio del dominio hereda token(), get() y la lectura de errores: sin sesión no llama al servidor. */
    @Test
    void serviciosDeDominioHeredanLasReglasDeSesion() {
        class ServicioDePrueba extends ServicioBase {
            ServicioDePrueba(ClienteApi a, Sesion s) {
                super(a, s);
            }

            RespuestaListaDto listar() {
                return get("/items/" + codificar("Longitud métrica"), RespuestaListaDto.class);
            }
        }
        Sesion s = new Sesion();
        ServicioDePrueba derivado = new ServicioDePrueba(falso, s);

        ExcepcionServicio sinSesion = assertThrows(ExcepcionServicio.class, derivado::listar);
        assertEquals("AUTH-002", sinSesion.getCodigo());
        assertTrue(sinSesion.exigeLogin());
        assertEquals(0, llamadas, "sin sesión no se consulta nada");

        s.setToken("T1");
        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\",\"elementos\":[\"a\",\"b\"]}");
        assertEquals(List.of("a", "b"), derivado.listar().getElementos());
        assertEquals("GET /items/Longitud%20m%C3%A9trica null T1", peticiones.get(0));

        respuesta = new RespuestaHttp(401, "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-003\",\"mensaje\":\"Sesión vencida\"}");
        ExcepcionServicio vencida = assertThrows(ExcepcionServicio.class, derivado::listar);
        assertTrue(vencida.exigeLogin());
        assertNull(s.getToken(), "un token vencido se descarta");

        s.setToken("T2");
        respuesta = new RespuestaHttp(401, "no es json");
        assertTrue(assertThrows(ExcepcionServicio.class, derivado::listar).exigeLogin(), "un 401 sin código también exige login");
        assertNull(s.getToken());

        s.setToken("T3");
        respuesta = new RespuestaHttp(404, "{\"estado\":\"ERROR\",\"codigo\":\"CAT-001\",\"mensaje\":\"No existe\"}");
        ExcepcionServicio negocio = assertThrows(ExcepcionServicio.class, derivado::listar);
        assertFalse(negocio.exigeLogin());
        assertEquals("T3", s.getToken(), "un error de negocio conserva la sesión");
    }
}
