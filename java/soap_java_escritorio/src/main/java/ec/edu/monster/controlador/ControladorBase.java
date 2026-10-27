package ec.edu.monster.controlador;

import ec.edu.monster.excepciones.ExcepcionServicio;
import ec.edu.monster.vista.VistaBase;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Molde de los controladores de pantalla (ARQUITECTURA_CLIENTE.md §4.6, familia con estado): la llamada al servicio se
 * hace en segundo plano, la pantalla queda «ocupada» mientras tanto, una segunda acción se ignora y los errores se
 * muestran como mensaje (nunca el código). No conoce la pantalla siguiente: la navegación llega como callbacks.
 *
 * Cada controlador valida los campos vacíos antes de llamar (en el orden de la pantalla) y usa {@link #ejecutar}. Un proyecto
 * con login sobrescribe {@link #manejar} para tratar el código de sesión no válida de su servidor (borrar la sesión y volver
 * al acceso); aquí todo error se muestra con su mensaje.
 */
abstract class ControladorBase {

    private final VistaBase vista;
    private boolean ocupado;

    ControladorBase(VistaBase vista) {
        this.vista = vista;
    }

    static boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    boolean estaOcupado() {
        return ocupado;
    }

    /** Ejecuta {@code tarea} fuera del hilo de eventos; {@code alExito} y los errores vuelven en él. Se ignora si ya hay una en curso. */
    <T> void ejecutar(String textoOcupado, Supplier<T> tarea, Consumer<T> alExito) {
        if (ocupado) {
            return;
        }
        ocupado = true;
        vista.setOcupado(true, textoOcupado);
        EjecutorAsincrono.ejecutar(tarea,
                resultado -> {
                    liberar();
                    alExito.accept(resultado);
                },
                error -> {
                    liberar();
                    manejar(error);
                });
    }

    /** Muestra el mensaje del error, nunca su código. */
    void manejar(ExcepcionServicio e) {
        vista.mostrarError(e.getMessage());
    }

    private void liberar() {
        ocupado = false;
        vista.setOcupado(false, null);
    }
}
