package ec.edu.monster.vista;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ec.edu.monster.databinding.ActivityLoginBinding;

/**
 * Pantalla de acceso, para los proyectos que definen autenticación (docs/PROJECT.md). Pasiva: solo dibuja el formulario
 * (imagen · título · Usuario · Clave · Iniciar sesión · zona de estado) con los textos de {@link Textos}; la clave es un campo
 * enmascarado. La plantilla no trae login (ARQUITECTURA_CLIENTE.md §1): el proyecto la conecta a su controlador
 * ({@code ControladorBase}) y a su sesión (en {@code modelo}); ver docs/PANTALLAS.md §5.1.
 *
 * DOMINIO: para usarla, hacerla la actividad de entrada en AndroidManifest.xml, pedir su controlador a la fábrica, observar
 * su estado (mostrar el estado en {@link ZonaEstado}, deshabilitar los controles mientras esté ocupado, ocultar el teclado con
 * {@link Teclado} al enviar, conservar el usuario y limpiar la clave tras un error) y pasar a la pantalla siguiente cuando el
 * estado lo indique.
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
        binding.txtTitulo.setText(Textos.TITULO_LOGIN);
        binding.lblUsuario.setText(Textos.USUARIO);
        binding.lblClave.setText(Textos.CLAVE);
        binding.btnIniciarSesion.setText(Textos.INICIAR_SESION);
        // TODO: controlador = new ViewModelProvider(this, app.getFabrica()).get(LoginControlador.class); observar su estado
        //       y dibujarlo: new ZonaEstado(binding.zonaEstado).mostrar(estado.ocupado, estado.mensaje, estado.error)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
