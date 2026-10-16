package ec.edu.monster;

import android.app.Application;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.ClienteApiHttp;
import ec.edu.monster.controlador.FabricaControladores;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;

/**
 * Punto de composicion: crea UNA Sesion y UN transporte HTTP por proceso y los comparte con todos los servicios.
 * Si Android mata el proceso la sesion se pierde y la app vuelve al login (comportamiento esperado).
 *
 * DOMINIO: crear aqui los servicios de dominio (new ServicioXxx(api, sesion)) y pasarlos a la fabrica.
 *
 * @author: Frederick Tipán
 */
public class AplicacionCliente extends Application {

    private FabricaControladores fabrica;

    @Override
    public void onCreate() {
        super.onCreate();
        Sesion sesion = new Sesion();
        ClienteApi api = new ClienteApiHttp();
        ServicioAutenticacion autenticacion = new ServicioAutenticacion(api, sesion);
        fabrica = new FabricaControladores(autenticacion);
    }

    public FabricaControladores getFabrica() {
        return fabrica;
    }
}
