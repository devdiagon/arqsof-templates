package ec.edu.monster.controlador;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ec.edu.monster.servicio.ServicioAutenticacion;

import java.util.concurrent.Executors;

/**
 * Crea los controladores (ViewModel) inyectandoles los servicios compartidos de la aplicacion.
 *
 * DOMINIO: por cada controlador nuevo agregar un "if" en create(...). Si el controlador necesita un dato de la
 * pantalla (p. ej. la categoria elegida), crear un metodo que devuelva su propia ViewModelProvider.Factory.
 *
 * @author: Frederick Tipán
 */
public class FabricaControladores implements ViewModelProvider.Factory {

    private final ServicioAutenticacion autenticacion;

    public FabricaControladores(ServicioAutenticacion autenticacion) {
        this.autenticacion = autenticacion;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> clase) {
        if (clase.isAssignableFrom(LoginControlador.class)) {
            return (T) new LoginControlador(autenticacion, Executors.newSingleThreadExecutor());
        }
        if (clase.isAssignableFrom(InicioControlador.class)) {
            return (T) new InicioControlador(autenticacion);
        }
        throw new IllegalArgumentException("Controlador desconocido: " + clase.getName());
    }
}
