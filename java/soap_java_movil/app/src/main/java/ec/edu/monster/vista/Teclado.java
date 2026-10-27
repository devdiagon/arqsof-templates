package ec.edu.monster.vista;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Al enviar un formulario se oculta el teclado para que la zona de estado (bajo los botones) quede a la vista: con el
 * teclado abierto taparía el indicador, el error o el resultado.
 */
final class Teclado {

    private Teclado() {
    }

    static void ocultar(Activity actividad) {
        View enfocada = actividad.getCurrentFocus();
        InputMethodManager manager = (InputMethodManager) actividad.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (enfocada != null && manager != null) {
            manager.hideSoftInputFromWindow(enfocada.getWindowToken(), 0);
        }
    }
}
