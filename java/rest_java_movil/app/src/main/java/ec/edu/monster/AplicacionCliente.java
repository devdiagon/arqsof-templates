package ec.edu.monster;

import android.app.Application;

import ec.edu.monster.api.ConfiguracionApi;
import ec.edu.monster.controlador.FabricaControladores;

/**
 * Punto de composición: fija la URL base de la API (de {@code res/values/config.xml}), crea UNA instancia de cada cliente
 * REST y de cada servicio por proceso y los comparte con los controladores a través de la fábrica. Si el proyecto tiene
 * login, aquí vive también su única sesión (en memoria): si Android mata el proceso se pierde y la app vuelve al login
 * (comportamiento esperado).
 *
 * DOMINIO: crear aquí los clientes y los servicios y pasarlos a la fábrica:
 * <pre>
 * XCliente cliente = XClienteApi.crear();
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
        ConfiguracionApi.fijarUrlBase(getString(R.string.url_base_api));
        // TODO: clientes (XClienteApi.crear()) y servicios del dominio (README §4)
        fabrica = new FabricaControladores();
    }

    public FabricaControladores getFabrica() {
        return fabrica;
    }
}
