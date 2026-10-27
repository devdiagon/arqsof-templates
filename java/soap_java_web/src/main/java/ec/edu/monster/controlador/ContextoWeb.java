package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Puente entre el contenedor web y el cliente: muestra las vistas y redirige. Es también el lugar donde se compone el
 * cliente (el equivalente de {@code Main} en los clientes de escritorio y consola).
 *
 * DOMINIO: al agregar un servicio del servidor, crear aquí su cliente <b>una sola vez</b> con
 * {@link ec.edu.monster.ws.ClientePerezoso} y un método que arme el servicio, p. ej.
 * <pre>
 * private static final XCliente CLIENTE_X = ClientePerezoso.de(XCliente.class,
 *         () -&gt; new XClienteWS(ConfiguracionServicio.configurar(new WSX_Service().getWSXPort())));
 *
 * static XServicio x(HttpServletRequest peticion) { return new XServicio(CLIENTE_X); }
 * </pre>
 * Si el proyecto tiene login, el estado del empleado (credenciales o sesión) vive <b>solo en la sesión HTTP</b> de cada
 * usuario, nunca en un campo estático ni en un bean de aplicación: lo verían todos los usuarios. En ese caso el
 * contexto de cada empleado (su sesión y los servicios que la reciben) se crea por sesión HTTP y se guarda en ella.
 */
public final class ContextoWeb {

    /** DOMINIO: ruta (relativa al contexto) de la pantalla inicial. */
    static final String PANTALLA_INICIAL = "/inicio";
    private static final String VISTAS = "/WEB-INF/vistas/";

    private ContextoWeb() {
    }

    /** Muestra {@code /WEB-INF/vistas/<vista>.jsp}. Los datos se pasan como atributos de la petición. */
    static void mostrar(HttpServletRequest peticion, HttpServletResponse respuesta, String vista)
            throws ServletException, IOException {
        peticion.getRequestDispatcher(VISTAS + vista + ".jsp").forward(peticion, respuesta);
    }

    /** Redirige a una ruta relativa al contexto (patrón Post/Redirect/Get). */
    static void redirigir(HttpServletRequest peticion, HttpServletResponse respuesta, String ruta) throws IOException {
        respuesta.sendRedirect(peticion.getContextPath() + ruta);
    }
}
