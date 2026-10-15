package ec.edu.monster.filtro;

import ec.edu.monster.controlador.ContextoWeb;
import ec.edu.monster.modelo.Sesion;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Las pantallas protegidas solo se ven con token: sin sesión iniciada se redirige al login. Además no se guardan
 * en caché, para que «Atrás» tras cerrar sesión no las muestre.
 *
 * DOMINIO: agregar a {@code urlPatterns} la ruta de cada controlador protegido (todo menos /login y /logout).
 * Este filtro solo comprueba que haya token; si el servidor ya lo venció, lo detecta el controlador (AUTH-003).
 */
@WebFilter(filterName = "FiltroAcceso", urlPatterns = {"/inicio"})
public class FiltroAcceso implements Filter {

    @Override
    public void doFilter(ServletRequest peticion, ServletResponse respuesta, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) peticion;
        HttpServletResponse salida = (HttpServletResponse) respuesta;
        HttpSession sesionHttp = http.getSession(false);
        Sesion sesion = sesionHttp == null ? null : (Sesion) sesionHttp.getAttribute(ContextoWeb.ATRIBUTO_SESION);
        if (sesion == null || sesion.getToken() == null) {
            salida.sendRedirect(http.getContextPath() + "/login");
            return;
        }
        salida.setHeader("Cache-Control", "no-store");
        cadena.doFilter(peticion, respuesta);
    }
}
