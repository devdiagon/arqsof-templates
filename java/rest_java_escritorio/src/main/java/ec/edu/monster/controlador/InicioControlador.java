package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.InicioVista;

/**
 * DOMINIO (ejemplo): controlador de la pantalla de inicio. Es el molde de un controlador de pantalla protegida:
 * recibe servicio y vista, registra los eventos de la vista y avisa con callbacks a dónde navegar.
 * Para una operación del servidor se usa {@link EjecutorAsincrono} (ver README §4).
 */
public class InicioControlador {

    public InicioControlador(ServicioAutenticacion servicio, InicioVista vista, Runnable alCerrarSesion) {
        vista.alCerrarSesion(() -> {
            servicio.cerrarSesion();
            alCerrarSesion.run();
        });
    }
}
