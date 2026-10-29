package ec.edu.monster;

import ec.edu.monster.api.ConfiguracionApi;
import ec.edu.monster.vista.InicioVista;
import ec.edu.monster.vista.Pantalla;
import ec.edu.monster.vista.VentanaPrincipal;
import ec.edu.monster.vista.estilo.EstilosUi;
import javax.swing.SwingUtilities;

/**
 * Punto de arranque y de composición: crea el cliente del servicio, los servicios, las vistas y los controladores,
 * y decide la navegación entre pantallas (los controladores avisan con callbacks; aquí se decide a dónde ir).
 *
 * DOMINIO: al agregar una pantalla, crear aquí su vista, su servicio y su controlador, registrarla en
 * {@link Pantalla} y conectar la navegación. Si el proyecto tiene login, la pantalla inicial pasa a ser
 * {@code LoginVista} (README §4).
 */
public class Main {

    public static void main(String[] args) {
        // TODO: cliente = XClienteApi.crear();   (README §4)
        //       crear los servicios con el cliente (extienden ServicioBase)
        System.out.println("Plantilla lista. API: " + ConfiguracionApi.urlBase());
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        EstilosUi.instalarLookAndFeel();   // antes de crear cualquier componente

        VentanaPrincipal ventana = new VentanaPrincipal();
        ventana.agregar(Pantalla.INICIO, new InicioVista());
        // TODO: por cada pantalla del dominio: vista, controlador (recibe servicio y vista) y ventana.agregar(...)

        ventana.mostrar(Pantalla.INICIO);
        ventana.setVisible(true);
    }
}
