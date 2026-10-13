package ec.edu.monster.vista;

import android.app.Activity;
import android.content.Intent;

/**
 * Navegacion comun entre pantallas.
 *
 * @author: Frederick Tipán
 */
final class Navegacion {

    private Navegacion() {
    }

    /** Vuelve al login descartando todas las pantallas abiertas; el aviso es opcional (p. ej. sesion expirada). */
    static void irALogin(Activity origen, String aviso) {
        Intent intent = new Intent(origen, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (aviso != null) {
            intent.putExtra(LoginActivity.EXTRA_MENSAJE, aviso);
        }
        origen.startActivity(intent);
        origen.finish();
    }
}
