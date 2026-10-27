package ec.edu.monster;

import android.app.Application;

import ec.edu.monster.controlador.FabricaControladores;

/**
 * Punto de composición: crea UNA instancia de cada cliente SOAP y de cada servicio por proceso y los comparte con los
 * controladores a través de la fábrica. Si el proyecto tiene login, aquí vive también su única sesión (en memoria): si
 * Android mata el proceso se pierde y la app vuelve al login (comportamiento esperado).
 *
 * DOMINIO: crear aquí los clientes y los servicios y pasarlos a la fábrica:
 * <pre>
 * XCliente cliente = new XClienteWS(getString(R.string.url_servicio));
 * XServicio servicio = new XServicio(cliente);
 * fabrica = new FabricaControladores(servicio);
 * </pre>
 *
 * @author: Frederick Tipán
 */
public class AplicacionCliente extends Application {

    private FabricaControladores fabrica;

    @Override
    public void onCreate() {
        super.onCreate();
        // TODO: clientes (XClienteWS con su URL de res/values/config.xml) y servicios del dominio (README §4)
        fabrica = new FabricaControladores();
    }

    public FabricaControladores getFabrica() {
        return fabrica;
    }
}
