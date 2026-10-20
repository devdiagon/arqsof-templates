package ec.edu.monster;

import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import ec.edu.monster.vista.InicioVista;
import ec.edu.monster.vista.LoginVista;
import ec.edu.monster.vista.Pantalla;
import ec.edu.monster.vista.Textos;
import ec.edu.monster.vista.VentanaPrincipal;
import ec.edu.monster.vista.estilo.EstilosUi;
import ec.edu.monster.ws.ClienteWS;
import ec.edu.monster.ws.ClienteServicio;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Punto de arranque y de composición: crea la sesión, los servicios, las vistas y los controladores,
 * y decide la navegación entre pantallas (los controladores avisan con callbacks; aquí se decide a dónde ir).
 *
 * DOMINIO: al agregar una pantalla, crear aquí su vista, su servicio y su controlador, registrarla en
 * {@link Pantalla} y conectar la navegación.
 */
public class Main {

    public static void main(String[] args) {
        ClienteServicio contrato;
        try {
            // Crea el proxy (usa el WSDL del classpath, no hay descarga) antes de abrir la ventana.
            contrato = ClienteWS.crear();
        } catch (RuntimeException e) {
            SwingUtilities.invokeLater(() -> {
                EstilosUi.instalarLookAndFeel();
                JOptionPane.showMessageDialog(null,
                        "No fue posible conectar con el servidor.\nVerifique que esté encendido e inténtelo de nuevo.",
                        Textos.NOMBRE_APLICACION, JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            });
            return;
        }
        ClienteServicio servicioWs = contrato;
        SwingUtilities.invokeLater(() -> iniciar(servicioWs));
    }

    private static void iniciar(ClienteServicio contrato) {
        EstilosUi.instalarLookAndFeel();   // antes de crear cualquier componente

        Sesion sesion = new Sesion();   // una sola para toda la aplicación: la comparten todos los servicios
        ServicioAutenticacion autenticacion = new ServicioAutenticacion(contrato, sesion);
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
