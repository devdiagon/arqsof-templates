package ec.edu.monster.vista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ec.edu.monster.AplicacionCliente;
import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.databinding.ActivityInicioBinding;

/**
 * DOMINIO (ejemplo): pantalla a la que se llega tras el login. Muestra el patron de toda pantalla protegida:
 * pedir su controlador a la fabrica, volver al login si no hay sesion, inflar con ViewBinding y delegar eventos.
 * Se reemplaza por las pantallas del dominio.
 *
 * @author: Frederick Tipán
 */
public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AplicacionCliente app = (AplicacionCliente) getApplication();
        InicioControlador controlador = new ViewModelProvider(this, app.getFabrica()).get(InicioControlador.class);
        if (!controlador.haySesion()) {
            Navegacion.irALogin(this, null);
            return;
        }

        ActivityInicioBinding binding = ActivityInicioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnCerrarSesion.setOnClickListener(v -> {
            controlador.cerrarSesion();
            Navegacion.irALogin(this, null);
        });
    }
}
