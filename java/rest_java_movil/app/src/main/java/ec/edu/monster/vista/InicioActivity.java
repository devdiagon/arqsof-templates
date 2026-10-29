package ec.edu.monster.vista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ec.edu.monster.databinding.ActivityInicioBinding;

/**
 * DOMINIO (ejemplo): pantalla inicial de la plantilla (punto de enganche: el proyecto la reemplaza por su login o su menú).
 * Muestra el patrón de toda pantalla: inflar con ViewBinding, poner los textos desde {@link Textos} y delegar los eventos
 * en su controlador (pedido a la fábrica con {@code new ViewModelProvider(this, app.getFabrica())}); esta no tiene
 * ninguno. No importa {@code api} ni {@code dto}. Se reemplaza por las pantallas del dominio (README §4 y
 * docs/CLIENT_STYLES_JAVA.md §5).
 *
 * @author: Frederick Tipán
 */
public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityInicioBinding binding = ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.txtTitulo.setText(Textos.INICIO);
        binding.txtMensaje.setText(Textos.MENSAJE_INICIO);
    }
}
