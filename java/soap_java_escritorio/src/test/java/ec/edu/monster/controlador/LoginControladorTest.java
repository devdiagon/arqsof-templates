package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.dto.RespuestaLoginDto;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.ws.ClienteServicio;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class LoginControladorTest {

    private final AtomicInteger llamadasAlServidor = new AtomicInteger();
    private final AtomicInteger autenticaciones = new AtomicInteger();
    private LoginVista vista;
    private ServicioAutenticacion servicio;

    /** Doble del contrato: acepta solo el usuario «admin». */
    private void preparar() throws Exception {
        ClienteServicio falso = (ClienteServicio) Proxy.newProxyInstance(
                ClienteServicio.class.getClassLoader(), new Class<?>[]{ClienteServicio.class},
                (proxy, m, args) -> {
                    llamadasAlServidor.incrementAndGet();
                    RespuestaLoginDto r = new RespuestaLoginDto();
                    if ("admin".equals(args[0])) {
                        r.setEstado("OK");
                        r.setCodigo("200");
                        r.setMensaje("ok");
                        r.setToken("T1");
                    } else {
                        r.setEstado("ERROR");
                        r.setCodigo("AUTH-001");
                        r.setMensaje("Usuario o contrasenia incorrectos");
                    }
                    return r;
                });
        servicio = new ServicioAutenticacion(falso, new Sesion());
        SwingUtilities.invokeAndWait(() -> {
            vista = new LoginVista();
            new LoginControlador(servicio, vista, autenticaciones::incrementAndGet);
        });
    }

    private void escribirYEntrar(String usuario, String contrasenia) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            vista.getCampoUsuario().setText(usuario);
            vista.getCampoContrasenia().setText(contrasenia);
            vista.getBotonEntrar().doClick();
        });
    }

    private static void esperar(BooleanSupplier condicion) throws Exception {
        long limite = System.currentTimeMillis() + 5000;
        while (!condicion.getAsBoolean()) {
            assertTrue(System.currentTimeMillis() < limite, "tiempo de espera agotado");
            Thread.sleep(10);
        }
        SwingUtilities.invokeAndWait(() -> { });
    }

    @Test
    void camposVaciosNoLlamanAlServidor() throws Exception {
        preparar();
        escribirYEntrar("", "");

        assertEquals("Ingrese usuario y contraseña", vista.getTextoMensaje());
        assertEquals(0, llamadasAlServidor.get());
        assertEquals(0, autenticaciones.get());
    }

    @Test
    void loginCorrectoIniciaSesionYAvisa() throws Exception {
        preparar();
        escribirYEntrar("admin", "clave");
        esperar(() -> autenticaciones.get() == 1);

        assertTrue(servicio.haySesion());
        assertEquals("Sesión iniciada", vista.getTextoMensaje());
        assertEquals("", vista.getContrasenia(), "la contraseña no debe quedar en el campo");
        assertTrue(vista.getBotonEntrar().isEnabled());
    }

    @Test
    void loginIncorrectoMuestraSoloElMensajeDelServidor() throws Exception {
        preparar();
        escribirYEntrar("otro", "x");
        esperar(() -> !vista.getTextoMensaje().isEmpty());

        assertEquals("Usuario o contrasenia incorrectos", vista.getTextoMensaje());
        assertFalse(vista.getTextoMensaje().contains("AUTH"), "no se muestra el código de error");
        assertFalse(servicio.haySesion());
        assertEquals(0, autenticaciones.get());
        assertEquals("", vista.getContrasenia());
        assertTrue(vista.getBotonEntrar().isEnabled(), "los controles se reactivan al terminar");
    }
}
