package ec.edu.monster.controlador;

import static org.junit.jupiter.api.Assertions.*;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.InicioVista;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class InicioControladorTest {

    @Test
    void cerrarSesionDescartaElTokenYAvisa() throws Exception {
        Sesion sesion = new Sesion("T1");
        ServicioAutenticacion servicio = new ServicioAutenticacion((u, c) -> null, sesion);
        AtomicInteger avisos = new AtomicInteger();
        InicioVista[] vista = new InicioVista[1];
        SwingUtilities.invokeAndWait(() -> {
            vista[0] = new InicioVista();
            new InicioControlador(servicio, vista[0], avisos::incrementAndGet);
            vista[0].getBotonCerrarSesion().doClick();
        });

        assertFalse(servicio.haySesion());
        assertEquals(1, avisos.get());
    }
}
