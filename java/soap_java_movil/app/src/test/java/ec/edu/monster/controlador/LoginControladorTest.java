package ec.edu.monster.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.ws.ContratoFalso;
import ec.edu.monster.ws.ContratoWS;

import org.junit.Rule;
import org.junit.Test;

/** Sin emulador ni servidor: ejecutor sincrono y contrato simulado. */
public class LoginControladorTest {

    @Rule
    public InstantTaskExecutorRule reglaLiveData = new InstantTaskExecutorRule();

    private static LoginControlador controlador(ContratoWS contrato, Sesion sesion) {
        return new LoginControlador(new ServicioAutenticacion(contrato, sesion), Runnable::run);
    }

    @Test
    public void estadoInicialEsInactivo() {
        LoginControlador.Estado e = controlador(new ContratoFalso(), new Sesion()).getEstado().getValue();
        assertFalse(e.cargando);
        assertFalse(e.autenticado);
        assertNull(e.mensaje);
    }

    @Test
    public void loginCorrectoAutentica() {
        LoginControlador c = controlador(new ContratoFalso(), new Sesion());
        c.iniciarSesion("admin", "clave");
        LoginControlador.Estado e = c.getEstado().getValue();
        assertTrue(e.autenticado);
        assertFalse(e.cargando);
        assertNull(e.mensaje);
    }

    @Test
    public void loginIncorrectoMuestraSoloElMensaje() {
        LoginControlador c = controlador(new ContratoFalso(), new Sesion());
        c.iniciarSesion("otro", "x");
        LoginControlador.Estado e = c.getEstado().getValue();
        assertFalse(e.autenticado);
        assertFalse(e.cargando);
        assertEquals("Usuario o contrasenia incorrectos", e.mensaje);
    }

    @Test
    public void camposVaciosMuestranMensajeConTilde() {
        LoginControlador c = controlador(new ContratoFalso(), new Sesion());
        c.iniciarSesion("", "");
        assertEquals("Ingrese usuario y contraseña", c.getEstado().getValue().mensaje);
    }

    @Test
    public void errorInesperadoNoCierraLaApp() {
        ContratoFalso roto = new ContratoFalso() {
            @Override
            public ec.edu.monster.dto.RespuestaLoginDto login(String u, String c) {
                throw new IllegalStateException("fallo interno");
            }
        };
        LoginControlador c = controlador(roto, new Sesion());
        c.iniciarSesion("admin", "x");
        assertEquals("Ocurrió un error inesperado", c.getEstado().getValue().mensaje);
    }

    @Test
    public void conSesionPreviaArrancaAutenticado() {
        assertTrue(controlador(new ContratoFalso(), new Sesion("T1")).getEstado().getValue().autenticado);
    }

    @Test
    public void mostrarMensajeMuestraElAvisoSinAutenticar() {
        LoginControlador c = controlador(new ContratoFalso(), new Sesion());
        c.mostrarMensaje("Sesión expirada, ingrese nuevamente");
        assertEquals("Sesión expirada, ingrese nuevamente", c.getEstado().getValue().mensaje);
        assertFalse(c.getEstado().getValue().autenticado);
    }
}
