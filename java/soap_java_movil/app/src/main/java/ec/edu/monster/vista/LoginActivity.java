package ec.edu.monster.vista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ec.edu.monster.databinding.ActivityLoginBinding;

/**
 * Pantalla de acceso, para los proyectos que definen autenticación (docs/PROJECT.md). Pasiva: solo dibuja el formulario
 * (Usuario · Clave · Iniciar sesión · indicador · mensaje) con los textos de {@link Textos}; la clave es un campo enmascarado.
 * La plantilla no trae login (ARQUITECTURA_CLIENTE.md §1): el proyecto la conecta a su controlador ({@code ControladorBase}) y
 * a su sesión (en {@code modelo}).
 *
 * DOMINIO: para usarla, hacerla la actividad de entrada en AndroidManifest.xml, pedir su controlador a la fábrica,
 * observar su estado (mostrar {@code mensaje}, deshabilitar los controles y mostrar {@code progreso} mientras esté ocupado,
 * conservar el usuario y limpiar la clave tras un error) y pasar a la pantalla siguiente cuando el estado lo indique.
 *
 * @author: Frederick Tipán
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.txtTitulo.setText(Textos.NOMBRE_APLICACION);
        binding.txtUsuario.setHint(Textos.USUARIO);
        binding.txtClave.setHint(Textos.CLAVE);
        binding.btnIniciarSesion.setText(Textos.INICIAR_SESION);
        // TODO: controlador = new ViewModelProvider(this, app.getFabrica()).get(LoginControlador.class); observar su estado
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
