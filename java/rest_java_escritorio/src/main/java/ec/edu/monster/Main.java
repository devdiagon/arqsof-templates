package ec.edu.monster;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.ClienteApiHttp;
import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.InicioVista;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.vista.Pantalla;
import ec.edu.monster.vista.VentanaPrincipal;
import ec.edu.monster.vista.estilo.EstilosUi;
import javax.swing.SwingUtilities;

/**
 * Punto de arranque y de composición: crea la sesión, el transporte, los servicios, las vistas y los controladores,
 * y decide la navegación entre pantallas (los controladores avisan con callbacks; aquí se decide a dónde ir).
 *
 * DOMINIO: al agregar una pantalla, crear aquí su vista, su servicio y su controlador, registrarla en
 * {@link Pantalla} y conectar la navegación.
 */
public class Main {

    public static void main(String[] args) {
        // No hay conexión previa que comprobar: si el servidor está apagado, el error se muestra al iniciar sesión.
        SwingUtilities.invokeLater(Main::iniciar);
    }

    private static void iniciar() {
        EstilosUi.instalarLookAndFeel();   // antes de crear cualquier componente

        ClienteApi api = new ClienteApiHttp();
        Sesion sesion = new Sesion();   // una sola para toda la aplicación: la comparten todos los servicios
        ServicioAutenticacion autenticacion = new ServicioAutenticacion(api, sesion);
        VentanaPrincipal ventana = new VentanaPrincipal();
        LoginVista loginVista = new LoginVista();
        InicioVista inicioVista = new InicioVista();
        ventana.agregar(Pantalla.LOGIN, loginVista);
        ventana.agregar(Pantalla.INICIO, inicioVista);

        Runnable irAlLogin = () -> {
            loginVista.ocultarMensaje();
            ventana.mostrar(Pantalla.LOGIN);
            loginVista.enfocarUsuario();
        };
        new LoginControlador(autenticacion, loginVista, () -> ventana.mostrar(Pantalla.INICIO));
        new InicioControlador(autenticacion, inicioVista, irAlLogin);

        ventana.mostrar(Pantalla.LOGIN);
        ventana.setVisible(true);
        loginVista.enfocarUsuario();
    }
}
