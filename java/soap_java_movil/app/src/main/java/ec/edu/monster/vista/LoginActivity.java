package ec.edu.monster.vista;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ec.edu.monster.AplicacionCliente;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.databinding.ActivityLoginBinding;

/**
 * Pantalla de login (compartida entre dominios). Solo dibuja el estado del controlador y reenvia los eventos;
 * no conoce ws ni dto.
 *
 * DOMINIO: cambiar INICIO por la pantalla a la que se entra tras autenticar.
 *
 * @author: Frederick Tipán
 */
public class LoginActivity extends AppCompatActivity {

    /** Aviso opcional para mostrar al abrir (p. ej. «Sesión expirada»). */
    public static final String EXTRA_MENSAJE = "ec.edu.monster.MENSAJE";

    /** DOMINIO: pantalla de destino tras un login correcto. */
    private static final Class<?> INICIO = InicioActivity.class;

    private ActivityLoginBinding binding;
    private LoginControlador controlador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AplicacionCliente app = (AplicacionCliente) getApplication();
        controlador = new ViewModelProvider(this, app.getFabrica()).get(LoginControlador.class);

        binding.btnIniciarSesion.setOnClickListener(v -> enviar());
        binding.txtContrasenia.setOnEditorActionListener((v, accion, evento) -> {
            if (accion == EditorInfo.IME_ACTION_DONE) {
                enviar();
                return true;
            }
            return false;
        });

        controlador.getEstado().observe(this, estado -> {
            binding.progreso.setVisibility(estado.cargando ? View.VISIBLE : View.GONE);
            binding.btnIniciarSesion.setEnabled(!estado.cargando);
            binding.txtUsuario.setEnabled(!estado.cargando);
            binding.txtContrasenia.setEnabled(!estado.cargando);
            binding.txtMensaje.setVisibility(estado.mensaje == null ? View.GONE : View.VISIBLE);
            binding.txtMensaje.setText(estado.mensaje);
            if (estado.autenticado) {
                startActivity(new Intent(this, INICIO));
                finish();
            }
        });

        String aviso = getIntent().getStringExtra(EXTRA_MENSAJE);
        if (savedInstanceState == null && aviso != null) {
            controlador.mostrarMensaje(aviso);
        }
    }

    private void enviar() {
        controlador.iniciarSesion(
                binding.txtUsuario.getText().toString().trim(),
                binding.txtContrasenia.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
