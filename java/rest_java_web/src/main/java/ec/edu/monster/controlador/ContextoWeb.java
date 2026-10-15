package ec.edu.monster.controlador;

import ec.edu.monster.api.ClienteApi;
import ec.edu.monster.api.ClienteApiHttp;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ServicioAutenticacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Puente entre el contenedor web y el cliente. La {@link Sesion} (token) vive en la sesión HTTP de cada usuario
 * (nunca en un bean compartido: lo verían todos los usuarios) y los servicios se arman por petición con ella,
 * sobre el transporte compartido ({@link #api()}).
 *
 * DOMINIO: agregar aquí un método por cada servicio nuevo, con el molde de {@link #autenticacion}.
 */
public final class ContextoWeb {

    /** Nombre del atributo de la sesión HTTP que guarda la {@link Sesion}. */
    public static final String ATRIBUTO_SESION = "sesion";
    /** Mensaje de una sola lectura para el login (p. ej. «Sesión expirada»). */
    static final String ATRIBUTO_AVISO = "aviso";
    /** DOMINIO: ruta (relativa al contexto) a la que se llega tras autenticarse. */
    static final String PANTALLA_INICIAL = "/inicio";
    private static final String VISTAS = "/WEB-INF/vistas/";

    /** Un solo transporte para toda la aplicación (no guarda estado de usuario; {@code HttpClient} admite llamadas concurrentes). */
    private static final ClienteApi API = new ClienteApiHttp();

    private ContextoWeb() {
    }

    /** La sesión del usuario actual; se crea (sin token) la primera vez. */
    public static synchronized Sesion sesion(HttpServletRequest peticion) {
        HttpSession http = peticion.getSession();
        Sesion sesion = (Sesion) http.getAttribute(ATRIBUTO_SESION);
        if (sesion == null) {
            sesion = new Sesion();
            http.setAttribute(ATRIBUTO_SESION, sesion);
        }
        return sesion;
    }

    /** Transporte único de la aplicación; los servicios lo reciben por constructor. */
    static ClienteApi api() {
        return API;
    }

    static ServicioAutenticacion autenticacion(HttpServletRequest peticion) {
        return new ServicioAutenticacion(api(), sesion(peticion));
    }

    /** Muestra {@code /WEB-INF/vistas/<vista>.jsp}. Los datos se pasan como atributos de la petición. */
    static void mostrar(HttpServletRequest peticion, HttpServletResponse respuesta, String vista)
            throws ServletException, IOException {
        peticion.getRequestDispatcher(VISTAS + vista + ".jsp").forward(peticion, respuesta);
    }

    static void redirigir(HttpServletRequest peticion, HttpServletResponse respuesta, String ruta) throws IOException {
        respuesta.sendRedirect(peticion.getContextPath() + ruta);
    }

    /** Sesión vencida o inválida (AUTH-002 / AUTH-003): vuelve al login mostrando el mensaje del servidor. */
    static void volverAlLogin(HttpServletRequest peticion, HttpServletResponse respuesta, String mensaje)
            throws IOException {
        peticion.getSession().setAttribute(ATRIBUTO_AVISO, mensaje);
        redirigir(peticion, respuesta, "/login");
    }
}
