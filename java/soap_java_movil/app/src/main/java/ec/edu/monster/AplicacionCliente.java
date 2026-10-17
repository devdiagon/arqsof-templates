package ec.edu.monster;

import android.app.Application;

import ec.edu.monster.controlador.FabricaControladores;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.ws.ClienteSoapHttp;
import ec.edu.monster.ws.ContratoWS;

/**
 * Punto de composicion: crea UNA Sesion y UN contrato SOAP por proceso y los comparte con todos los servicios.
 * Si Android mata el proceso la sesion se pierde y la app vuelve al login (comportamiento esperado).
 *
 * DOMINIO: crear aqui los servicios de dominio (new ServicioXxx(ws, sesion)) y pasarlos a la fabrica.
 *
 * @author: Frederick Tipán
 */
public class AplicacionCliente extends Application {

    private FabricaControladores fabrica;

    @Override
    public void onCreate() {
        super.onCreate();
        Sesion sesion = new Sesion();
        ContratoWS ws = new ClienteSoapHttp(getString(R.string.url_servicio));
        ServicioAutenticacion autenticacion = new ServicioAutenticacion(ws, sesion);
        fabrica = new FabricaControladores(autenticacion);
    }

    public FabricaControladores getFabrica() {
        return fabrica;
    }
}
