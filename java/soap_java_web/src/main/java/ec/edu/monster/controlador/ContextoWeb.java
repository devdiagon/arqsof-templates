package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Puente entre el contenedor web y el cliente: muestra las vistas, redirige y lleva datos de una petición a la siguiente. Es
 * también el lugar donde se compone el cliente (el equivalente de {@code Main} en los clientes de escritorio y consola).
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
 *
 * <b>Resultado de una sola lectura (flash).</b> Un {@code POST} que cambia datos no devuelve la página: guarda su resultado
 * (o el error y lo escrito) con {@link #guardarFlash} y redirige a un {@code GET}, que lo recupera con {@link #cargarFlash} y lo
 * borra. Así recargar la página no repite la operación (Post/Redirect/Get).
 */
public final class ContextoWeb {

    private static final String ATRIBUTO_FLASH = "flash";
    private static final String ATRIBUTO_AVISO = "aviso";

    /** DOMINIO: ruta (relativa al contexto) de la pantalla inicial. */
    static final String PANTALLA_INICIAL = "/inicio";
    private static final String VISTAS = "/WEB-INF/vistas/";

    private ContextoWeb() {
    }

    // ---- Navegación ----

    /** Muestra {@code /WEB-INF/vistas/<vista>.jsp}. Los datos se pasan como atributos de la petición. */
    static void mostrar(HttpServletRequest peticion, HttpServletResponse respuesta, String vista)
            throws ServletException, IOException {
        peticion.getRequestDispatcher(VISTAS + vista + ".jsp").forward(peticion, respuesta);
    }

    /** Redirige a una ruta relativa al contexto (patrón Post/Redirect/Get). */
    static void redirigir(HttpServletRequest peticion, HttpServletResponse respuesta, String ruta) throws IOException {
        respuesta.sendRedirect(peticion.getContextPath() + ruta);
    }

    // ---- Resultado de una sola lectura ----

    /** Guarda en la sesión lo que la siguiente petición {@code GET} mostrará una sola vez. Los valores deben ser serializables. */
    static void guardarFlash(HttpServletRequest peticion, Map<String, Object> datos) {
        peticion.getSession().setAttribute(ATRIBUTO_FLASH, new HashMap<>(datos));
    }

    /** Pasa a atributos de la petición lo guardado por {@link #guardarFlash} y lo borra de la sesión. */
    @SuppressWarnings("unchecked")
    static void cargarFlash(HttpServletRequest peticion) {
        HttpSession http = peticion.getSession(false);
        if (http == null) {
            return;
        }
        Map<String, Object> datos = (Map<String, Object>) http.getAttribute(ATRIBUTO_FLASH);
        if (datos != null) {
            http.removeAttribute(ATRIBUTO_FLASH);
            datos.forEach(peticion::setAttribute);
        }
    }

    /** Aviso de una sola lectura para la pantalla de acceso (p. ej. sesión no válida). */
    static void avisar(HttpServletRequest peticion, String mensaje) {
        peticion.getSession().setAttribute(ATRIBUTO_AVISO, mensaje);
    }

    /** Devuelve y borra el aviso pendiente, o {@code null}. */
    static String tomarAviso(HttpServletRequest peticion) {
        HttpSession http = peticion.getSession(false);
        if (http == null) {
            return null;
        }
        String aviso = (String) http.getAttribute(ATRIBUTO_AVISO);
        http.removeAttribute(ATRIBUTO_AVISO);
        return aviso;
    }

    // ---- Sesión ----

    /**
     * Invalida la sesión HTTP y borra su cookie. Sin borrar la cookie, el navegador la sigue enviando y el siguiente acceso
     * se tomaría por una sesión vencida (el filtro de acceso mostraría el aviso de sesión no válida).
     */
    static void cerrarSesion(HttpServletRequest peticion, HttpServletResponse respuesta) {
        HttpSession http = peticion.getSession(false);
        if (http != null) {
            http.invalidate();
        }
        String nombre = peticion.getServletContext().getSessionCookieConfig().getName();
        Cookie cookie = new Cookie(nombre == null ? "JSESSIONID" : nombre, "");
        cookie.setMaxAge(0);
        cookie.setPath(peticion.getContextPath().isEmpty() ? "/" : peticion.getContextPath());
        cookie.setHttpOnly(true);
        respuesta.addCookie(cookie);
    }
}
