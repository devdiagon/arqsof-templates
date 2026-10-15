package ec.edu.monster.controlador;

import ec.edu.monster.excepciones.ExcepcionServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Caso de uso compartido: acceso. {@code GET /login} muestra el formulario, {@code POST /login} autentica y
 * {@code POST /logout} cierra la sesión. Tras autenticar redirige a {@link ContextoWeb#PANTALLA_INICIAL}.
 */
@WebServlet(name = "LoginControlador", urlPatterns = {"/login", "/logout"})
public class LoginControlador extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if ("/logout".equals(peticion.getServletPath())) {
            ContextoWeb.redirigir(peticion, respuesta, "/login");
            return;
        }
        if (ContextoWeb.autenticacion(peticion).haySesion()) {
            ContextoWeb.redirigir(peticion, respuesta, ContextoWeb.PANTALLA_INICIAL);
            return;
        }
        HttpSession http = peticion.getSession();
        Object aviso = http.getAttribute(ContextoWeb.ATRIBUTO_AVISO);
        if (aviso != null) {
            http.removeAttribute(ContextoWeb.ATRIBUTO_AVISO);
            peticion.setAttribute("error", aviso);
        }
        ContextoWeb.mostrar(peticion, respuesta, "login");
    }

    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        if ("/logout".equals(peticion.getServletPath())) {
            HttpSession http = peticion.getSession(false);
            if (http != null) {
                http.invalidate();
            }
            ContextoWeb.redirigir(peticion, respuesta, "/login");
            return;
        }
        String usuario = texto(peticion.getParameter("usuario"));
        String contrasenia = texto(peticion.getParameter("contrasenia"));
        try {
            ContextoWeb.autenticacion(peticion).login(usuario, contrasenia);
        } catch (ExcepcionServicio e) {
            peticion.setAttribute("error", e.getMessage());   // solo el mensaje, nunca el código
            peticion.setAttribute("usuario", usuario);        // la contraseña nunca se devuelve a la página
            ContextoWeb.mostrar(peticion, respuesta, "login");
            return;
        }
        // Nuevo identificador tras autenticar: evita la fijación de sesión.
        peticion.changeSessionId();
        ContextoWeb.redirigir(peticion, respuesta, ContextoWeb.PANTALLA_INICIAL);
    }

    private static String texto(String s) {
        return s == null ? "" : s.trim();
    }
}
