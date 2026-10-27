package ec.edu.monster.controlador;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.Textos;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Molde de los controladores de pantalla (ARQUITECTURA_CLIENTE.md §4.6, familia con estado): el estado inmutable se observa
 * con {@code LiveData}, la llamada al servicio se hace en segundo plano, la pantalla queda «ocupada» mientras tanto, una
 * segunda acción se ignora y los errores se muestran como mensaje (nunca el código). No conoce la pantalla siguiente:
 * la navegación es un dato más del estado ({@code D}) que consume la actividad.
 *
 * Cada controlador valida los campos vacíos antes de llamar (en el orden de la pantalla; {@link #rechazar}) y usa
 * {@link #ejecutar}. Un proyecto con login sobrescribe {@link #manejar} para tratar el código de sesión no válida de su
 * servidor (borrar la sesión y pedir volver al acceso); aquí todo error se muestra con su mensaje y se conservan los datos.
 *
 * @param <D> datos de la pantalla (lo escrito, el resultado, el destino de navegación...); puede ser {@code Void}.
 */
public abstract class ControladorBase<D> extends ViewModel {

    /**
     * Estado inmutable que la vista dibuja completo cada vez que cambia. Mínimo exigido por la arquitectura: ocupado,
     * mensaje (con indicación de si es error o éxito) y datos. Mientras está ocupado, {@code mensaje} es el texto del indicador.
     */
    public static final class Estado<D> {
        public final boolean ocupado;
        /** Texto del indicador (si está ocupado), mensaje de éxito o de error, o null. */
        public final String mensaje;
        /** true si {@code mensaje} es un error. */
        public final boolean error;
        public final D datos;

        Estado(boolean ocupado, String mensaje, boolean error, D datos) {
            this.ocupado = ocupado;
            this.mensaje = mensaje;
            this.error = error;
            this.datos = datos;
        }
    }

    private final Executor ejecutor;
    private final MutableLiveData<Estado<D>> estado;

    protected ControladorBase(Executor ejecutor, D datosIniciales) {
        this.ejecutor = ejecutor;
        this.estado = new MutableLiveData<>(libre(datosIniciales));
    }

    public LiveData<Estado<D>> getEstado() {
        return estado;
    }

    protected static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    /** Estado sin mensaje. */
    protected final Estado<D> libre(D datos) {
        return new Estado<>(false, null, false, datos);
    }

    protected final Estado<D> exito(D datos, String mensaje) {
        return new Estado<>(false, mensaje, false, datos);
    }

    protected final Estado<D> fallo(D datos, String mensaje) {
        return new Estado<>(false, mensaje, true, datos);
    }

    /** Muestra un error de validación sin llamar al servicio y conserva los datos. Solo desde el hilo principal. */
    protected final void rechazar(String mensaje) {
        estado.setValue(fallo(estado.getValue().datos, mensaje));
    }

    /**
     * Ejecuta {@code tarea} en segundo plano y publica el estado que devuelve {@code alExito}; mientras tanto el estado es
     * «ocupado» con {@code textoOcupado}. Se ignora si ya hay una en curso. Solo desde el hilo principal.
     */
    protected final <T> void ejecutar(String textoOcupado, Supplier<T> tarea, Function<T, Estado<D>> alExito) {
        Estado<D> actual = estado.getValue();
        if (actual.ocupado) {
            return;
        }
        D datos = actual.datos;
        estado.setValue(new Estado<>(true, textoOcupado, false, datos));
        ejecutor.execute(() -> {
            Estado<D> resultado;
            try {
                resultado = alExito.apply(tarea.get());
            } catch (ExcepcionServicio e) {
                resultado = manejar(e, datos);
            } catch (RuntimeException e) {
                resultado = fallo(datos, Textos.ERROR_INESPERADO);
            }
            estado.postValue(resultado);
        });
    }

    /** Estado ante un error del servicio: su mensaje, nunca su código, y los datos intactos. */
    protected Estado<D> manejar(ExcepcionServicio e, D datos) {
        return fallo(datos, e.getMessage());
    }

    @Override
    protected void onCleared() {
        if (ejecutor instanceof ExecutorService) {
            ((ExecutorService) ejecutor).shutdownNow();
        }
    }
}
