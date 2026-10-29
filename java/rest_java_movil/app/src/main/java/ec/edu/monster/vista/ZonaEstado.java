package ec.edu.monster.vista;

import android.view.View;

import ec.edu.monster.R;
import ec.edu.monster.databinding.ZonaEstadoBinding;

/**
 * Zona de estado (docs/PANTALLAS.md §3): un solo lugar que muestra, de una en una, el indicador de ocupado con su
 * texto, un error o un éxito; oculto cuando no hay nada que mostrar. Solo dibuja lo que el estado del controlador dice.
 */
final class ZonaEstado {

    private final ZonaEstadoBinding binding;

    ZonaEstado(ZonaEstadoBinding binding) {
        this.binding = binding;
    }

    /**
     * @param ocupado si hay una llamada en curso: {@code mensaje} es entonces el texto del indicador
     * @param mensaje texto del error o del éxito, o {@code null} si no hay nada que mostrar
     * @param error   si {@code mensaje} es un error (barra roja) o un éxito (barra verde)
     */
    void mostrar(boolean ocupado, String mensaje, boolean error) {
        binding.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        binding.txtOcupado.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        binding.txtOcupado.setText(ocupado ? mensaje : null);

        boolean hayMensaje = !ocupado && mensaje != null;
        binding.txtMensaje.setVisibility(hayMensaje ? View.VISIBLE : View.GONE);
        if (hayMensaje) {
            binding.txtMensaje.setBackgroundResource(error ? R.drawable.mensaje_error_fondo : R.drawable.mensaje_exito_fondo);
            binding.txtMensaje.setText(mensaje);
        } else {
            binding.txtMensaje.setText(null);
        }
    }
}
