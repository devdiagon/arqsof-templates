package ec.edu.monster.controlador;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Crea los controladores (ViewModel) inyectándoles los servicios compartidos de la aplicación y un ejecutor propio
 * ({@code Executors.newSingleThreadExecutor()}: lo cierra {@code ControladorBase.onCleared}).
 *
 * DOMINIO: por cada controlador nuevo, recibir el servicio en el constructor y agregar un "if" en create(...):
 * <pre>
 * if (clase.isAssignableFrom(ItemsControlador.class)) {
 *     return (T) new ItemsControlador(itemsServicio, Executors.newSingleThreadExecutor());
 * }
 * </pre>
 * Si el controlador necesita un dato de la pantalla (p. ej. la categoría elegida), crear un método que devuelva su
 * propia ViewModelProvider.Factory.
 *
 * @author: Frederick Tipán
 */
public class FabricaControladores implements ViewModelProvider.Factory {

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> clase) {
        throw new IllegalArgumentException("Controlador desconocido: " + clase.getName());
    }
}
