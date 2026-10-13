package ec.edu.monster.controlador;

import androidx.lifecycle.ViewModel;

import ec.edu.monster.servicio.ServicioAutenticacion;

/**
 * DOMINIO (ejemplo): controlador de la pantalla de inicio. Solo conoce el estado de la sesion.
 * Las pantallas reales siguen el molde de LoginControlador (estado inmutable + LiveData + ejecutor).
 *
 * @author: Frederick Tipán
 */
public class InicioControlador extends ViewModel {

    private final ServicioAutenticacion servicio;

    public InicioControlador(ServicioAutenticacion servicio) {
        this.servicio = servicio;
    }

    public boolean haySesion() {
        return servicio.haySesion();
    }

    public void cerrarSesion() {
        servicio.cerrarSesion();
    }
}
