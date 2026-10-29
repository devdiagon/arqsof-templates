package ec.edu.monster.controlador;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.Textos;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingWorker;

/**
 * Ejecuta una llamada al servicio fuera del hilo de eventos (para no congelar la ventana) y entrega
 * el resultado, o el error, de vuelta en el hilo de eventos. Es lo único que usa SwingWorker.
 */
public final class EjecutorAsincrono {

    private EjecutorAsincrono() {
    }

    /**
     * @param tarea       trabajo a ejecutar en segundo plano (llama al servicio)
     * @param alTerminar  se ejecuta en el hilo de eventos con el resultado
     * @param alFallar    se ejecuta en el hilo de eventos si la tarea lanzó una excepción
     */
    public static <T> void ejecutar(Supplier<T> tarea, Consumer<T> alTerminar, Consumer<ExcepcionServicio> alFallar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return tarea.get();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (ExecutionException e) {
                    alFallar.accept(comoExcepcionServicio(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(comoExcepcionServicio(e));
                }
            }
        }.execute();
    }

    private static ExcepcionServicio comoExcepcionServicio(Throwable causa) {
        if (causa instanceof ExcepcionServicio negocio) {
            return negocio;
        }
        return new ExcepcionServicio("ERROR", Textos.ERROR_INESPERADO, causa);
    }
}
