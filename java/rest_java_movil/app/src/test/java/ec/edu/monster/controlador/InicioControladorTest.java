package ec.edu.monster.controlador;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.api.ApiFalsa;
import org.junit.Test;

public class InicioControladorTest {

    @Test
    public void conSesionLaPantallaPuedeMostrarse() {
        InicioControlador c = new InicioControlador(new ServicioAutenticacion(new ApiFalsa(), new Sesion("T1")));
        assertTrue(c.haySesion());
    }

    @Test
    public void sinSesionNoPuedeMostrarse() {
        InicioControlador c = new InicioControlador(new ServicioAutenticacion(new ApiFalsa(), new Sesion()));
        assertFalse(c.haySesion());
    }

    @Test
    public void cerrarSesionDescartaElToken() {
        InicioControlador c = new InicioControlador(new ServicioAutenticacion(new ApiFalsa(), new Sesion("T1")));
        c.cerrarSesion();
        assertFalse(c.haySesion());
    }
}
