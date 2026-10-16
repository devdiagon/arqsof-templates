package ec.edu.monster.servicio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.api.ApiFalsa;
import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.RespuestaHttp;
import ec.edu.monster.dto.RespuestaListaDto;
import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.modelo.Sesion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ServicioAutenticacionTest {

    private int llamadas;
    private RespuestaHttp respuesta;
    private RuntimeException fallo;
    private final List<String> peticiones = new ArrayList<>();

    /** Transporte programable: devuelve la respuesta fijada (o lanza el fallo) y registra cada peticion. */
    private final ClienteApi falso = (metodo, ruta, cuerpo, token) -> {
        llamadas++;
        peticiones.add(metodo + " " + ruta + " " + cuerpo + " " + token);
        if (fallo != null) {
            throw fallo;
        }
        return respuesta;
    };

    /** Servicio minimo para probar las reglas que heredan todos los servicios del dominio. */
    private static class ServicioDePrueba extends ServicioBase {
        ServicioDePrueba(ClienteApi api, Sesion sesion) {
            super(api, sesion);
        }

        RespuestaListaDto listar() {
            return get("/items/" + codificar("Longitud métrica"), RespuestaListaDto.class);
        }
    }

    @Test
    public void loginCorrectoGuardaSesionYEnviaElCuerpoJson() {
        Sesion sesion = new Sesion();
        ServicioAutenticacion s = new ServicioAutenticacion(falso, sesion);
        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\",\"token\":\"T1\"}");
        s.login("u", "c");
        assertTrue(s.haySesion());
        assertEquals("T1", sesion.getToken());
        assertEquals("POST /auth/login {\"usuario\":\"u\",\"contrasenia\":\"c\"} null", peticiones.get(0));
    }

    @Test
    public void loginConElDobleDeLaPlantilla() {
        ServicioAutenticacion s = new ServicioAutenticacion(new ApiFalsa(), new Sesion());
        s.login("admin", "clave");
        assertTrue(s.haySesion());
    }

    @Test
    public void error401EnLoginMuestraElMensajeDelServidor() {
        ServicioAutenticacion s = new ServicioAutenticacion(new ApiFalsa(), new Sesion());
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.login("otro", "x"));
        assertEquals("AUTH-001", e.getCodigo());
        assertEquals("Usuario o contrasenia incorrectos", e.getMessage());
        assertFalse(e.exigeLogin());
        assertFalse(s.haySesion());
    }

    @Test
    public void camposVaciosNoLlamanAlServidor() {
        ServicioAutenticacion s = new ServicioAutenticacion(falso, new Sesion());
        assertEquals("AUTH-001", assertThrows(ExcepcionServicio.class, () -> s.login(" ", "")).getCodigo());
        assertEquals(0, llamadas);
    }

    @Test
    public void servidorNoDisponibleSePropagaConSuMensaje() {
        ServicioAutenticacion s = new ServicioAutenticacion(falso, new Sesion());
        fallo = ExcepcionServicio.servidorNoDisponible(new java.net.ConnectException("rechazada"));
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.login("u", "c"));
        assertTrue(e.servidorNoDisponible());
        assertEquals("Servidor no disponible, intente más tarde", e.getMessage());
    }

    @Test
    public void respuestaNulaOSinTokenEsSrv000() {
        Sesion sesion = new Sesion();
        ServicioAutenticacion s = new ServicioAutenticacion(falso, sesion);
        assertEquals("SRV-000", assertThrows(ExcepcionServicio.class, () -> s.login("u", "c")).getCodigo());

        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\"}");
        assertEquals("SRV-000", assertThrows(ExcepcionServicio.class, () -> s.login("u", "c")).getCodigo());
        assertFalse(s.haySesion());
    }

    @Test
    public void cuerpoQueNoEsJsonDaUnMensajeGenericoConElCodigoHttp() {
        ServicioAutenticacion s = new ServicioAutenticacion(falso, new Sesion());
        respuesta = new RespuestaHttp(500, "<html>Error</html>");
        ExcepcionServicio e = assertThrows(ExcepcionServicio.class, () -> s.login("u", "c"));
        assertEquals("Error del servidor (HTTP 500)", e.getMessage());
        assertEquals("HTTP-500", e.getCodigo());
    }

    @Test
    public void cerrarSesionDescartaElToken() {
        ServicioAutenticacion s = new ServicioAutenticacion(new ApiFalsa(), new Sesion());
        s.login("admin", "clave");
        s.cerrarSesion();
        assertFalse(s.haySesion());
    }

    @Test
    public void serviciosDeDominioHeredanLasReglasDeSesion() {
        Sesion sesion = new Sesion();
        ServicioDePrueba s = new ServicioDePrueba(falso, sesion);

        ExcepcionServicio sinSesion = assertThrows(ExcepcionServicio.class, s::listar);
        assertEquals("AUTH-002", sinSesion.getCodigo());
        assertTrue(sinSesion.exigeLogin());
        assertEquals("sin sesion no se consulta nada", 0, llamadas);

        sesion.setToken("T1");
        respuesta = new RespuestaHttp(200, "{\"estado\":\"OK\",\"codigo\":\"200\",\"mensaje\":\"ok\",\"elementos\":[\"a\",\"b\"]}");
        assertEquals(Arrays.asList("a", "b"), s.listar().getElementos());
        assertEquals("GET /items/Longitud%20m%C3%A9trica null T1", peticiones.get(0));

        respuesta = new RespuestaHttp(401, "{\"estado\":\"ERROR\",\"codigo\":\"AUTH-003\",\"mensaje\":\"Sesión vencida\"}");
        assertTrue(assertThrows(ExcepcionServicio.class, s::listar).exigeLogin());
        assertNull("un token vencido se descarta", sesion.getToken());

        sesion.setToken("T2");
        respuesta = new RespuestaHttp(401, "no es json");
        assertTrue("un 401 sin codigo tambien exige login", assertThrows(ExcepcionServicio.class, s::listar).exigeLogin());
        assertNull(sesion.getToken());

        sesion.setToken("T3");
        respuesta = new RespuestaHttp(404, "{\"estado\":\"ERROR\",\"codigo\":\"CAT-001\",\"mensaje\":\"No existe\"}");
        ExcepcionServicio negocio = assertThrows(ExcepcionServicio.class, s::listar);
        assertFalse(negocio.exigeLogin());
        assertEquals("un error de negocio conserva la sesion", "T3", sesion.getToken());
    }
}
