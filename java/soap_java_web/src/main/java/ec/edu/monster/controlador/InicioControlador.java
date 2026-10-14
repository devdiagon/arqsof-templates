package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DOMINIO (ejemplo): controlador de una pantalla protegida; se reemplaza por los del dominio. Su ruta debe estar
 * también en {@code FiltroAcceso} (así sin token se redirige al login antes de llegar aquí).
 *
 * Molde para una operación del servidor (ver README §4):
 * <pre>
 * try {
 *     peticion.setAttribute("items", ContextoWeb.items(peticion).items());
 * } catch (ExcepcionServicio e) {
 *     if (e.exigeLogin()) {                                   // AUTH-002 / AUTH-003
 *         ContextoWeb.volverAlLogin(peticion, respuesta, e.getMessage());
 *         return;
 *     }
 *     peticion.setAttribute("error", e.getMessage());         // solo el mensaje
 * }
 * ContextoWeb.mostrar(peticion, respuesta, "items");          // /WEB-INF/vistas/items.jsp
 * </pre>
 */
@WebServlet(name = "InicioControlador", urlPatterns = "/inicio")
public class InicioControlador extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws ServletException, IOException {
        ContextoWeb.mostrar(peticion, respuesta, "inicio");
    }
}
