package ec.edu.monster.controlador;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.servicio.ServicioAutenticacion;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Controlador del login (compartido). Sobrevive a la rotacion; la red corre en un hilo de fondo y la vista solo
 * observa el estado. Muestra al usuario el mensaje, nunca el codigo de error.
 *
 * Este es tambien el MOLDE de los controladores del dominio: estado inmutable en un LiveData, ejecutor inyectado,
 * ExcepcionServicio -> mensaje, ExcepcionServicio.exigeLogin() -> volver al login.
 *
 * @author: Frederick Tipán
 */
public class LoginControlador extends ViewModel {

    /** Estado inmutable que la vista dibuja completo cada vez que cambia. */
    public static final class Estado {
        public final boolean cargando;
        public final boolean autenticado;
        /** Mensaje de error para mostrar, o null. */
        public final String mensaje;

        Estado(boolean cargando, boolean autenticado, String mensaje) {
            this.cargando = cargando;
            this.autenticado = autenticado;
            this.mensaje = mensaje;
        }
    }

    private final ServicioAutenticacion servicio;
    private final Executor ejecutor;
    private final MutableLiveData<Estado> estado = new MutableLiveData<>(new Estado(false, false, null));

    public LoginControlador(ServicioAutenticacion servicio, Executor ejecutor) {
        this.servicio = servicio;
        this.ejecutor = ejecutor;
        if (servicio.haySesion()) {
            estado.setValue(new Estado(false, true, null));
        }
    }

    public LiveData<Estado> getEstado() {
        return estado;
    }

    public void iniciarSesion(String usuario, String contrasenia) {
        Estado actual = estado.getValue();
        if (actual != null && actual.cargando) {
            return;
        }
        estado.setValue(new Estado(true, false, null));
        ejecutor.execute(() -> {
            Estado resultado;
            try {
                servicio.login(usuario, contrasenia);
                resultado = new Estado(false, true, null);
            } catch (ExcepcionServicio e) {
                resultado = new Estado(false, false, e.getMessage());
            } catch (RuntimeException e) {
                resultado = new Estado(false, false, "Ocurrió un error inesperado");
            }
            estado.postValue(resultado);
        });
    }

    /** Muestra un aviso proveniente de otra pantalla (p. ej. sesion expirada) sin iniciar login. */
    public void mostrarMensaje(String mensaje) {
        Estado actual = estado.getValue();
        if (actual == null || !actual.cargando) {
            estado.setValue(new Estado(false, false, mensaje));
        }
    }

    @Override
    protected void onCleared() {
        if (ejecutor instanceof ExecutorService) {
            ((ExecutorService) ejecutor).shutdownNow();
        }
    }
}
